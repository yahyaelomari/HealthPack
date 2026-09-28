package com.yahyaelomari.healthpack.scheduling.repository;

import com.yahyaelomari.healthpack.scheduling.domain.Appointment;
import com.yahyaelomari.healthpack.scheduling.domain.Practitioner;
import com.yahyaelomari.healthpack.scheduling.domain.Specialty;
import com.yahyaelomari.healthpack.scheduling.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class AppointmentRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PractitionerRepository practitionerRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void overlappingAppointmentsForTheSamePractitionerAreRejected() {
        Practitioner practitioner = practitionerRepository.saveAndFlush(newPractitioner());
        Instant start = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant end = start.plusSeconds(1800);
        appointmentRepository.saveAndFlush(newAppointment(practitioner.getId(), start, end));

        // Starts 15 minutes into the first booking — same practitioner, genuine overlap.
        Appointment overlapping = newAppointment(practitioner.getId(), start.plusSeconds(900), end.plusSeconds(900));

        assertThatThrownBy(() -> appointmentRepository.saveAndFlush(overlapping))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void theSameSlotIsFineForADifferentPractitioner() {
        Practitioner first = practitionerRepository.saveAndFlush(newPractitioner());
        Practitioner second = practitionerRepository.saveAndFlush(newPractitioner());
        Instant start = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant end = start.plusSeconds(1800);
        appointmentRepository.saveAndFlush(newAppointment(first.getId(), start, end));

        // No exception expected: different practitioner, identical time is fine.
        appointmentRepository.saveAndFlush(newAppointment(second.getId(), start, end));
    }

    @Test
    void aCancelledAppointmentNoLongerBlocksItsSlot() {
        Practitioner practitioner = practitionerRepository.saveAndFlush(newPractitioner());
        Instant start = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant end = start.plusSeconds(1800);
        Appointment first = appointmentRepository.saveAndFlush(newAppointment(practitioner.getId(), start, end));

        first.cancel("patient asked to reschedule");
        appointmentRepository.saveAndFlush(first);

        // No exception expected: cancelling freed the slot.
        appointmentRepository.saveAndFlush(newAppointment(practitioner.getId(), start, end));
    }

    /**
     * The actual proof the exclusion constraint holds under real concurrency,
     * not just sequentially — this is the test that matters most in this
     * service.
     *
     * <p>{@code NOT_SUPPORTED} is doing real work here, not decoration.
     * {@code @DataJpaTest} normally wraps the whole test method in one
     * transaction, bound to this thread, that rolls back at the end. Left in
     * place, the practitioner saved below would still be uncommitted when the
     * worker threads' own separate transactions tried to see it — every
     * booking would fail on the foreign key, not the constraint this test
     * exists to check. Suspending it makes the setup commit for real, exactly
     * like production.
     *
     * <p>Losing this race can surface two different ways, and both are
     * expected, not bugs: a clean {@code DataIntegrityViolationException}
     * when one insert loses against an already-committed row, or a genuine
     * Postgres deadlock ({@code CannotAcquireLockException}) when two
     * brand-new inserts race each other for the same GiST lock at once. A
     * plain unique constraint never deadlocks like this; an exclusion
     * constraint checked via GiST can. Either outcome means "this booking
     * did not succeed" — a real booking service would catch both and retry
     * or report the slot as taken, not treat the second as a server error.
     *
     * <p>20 concurrent attempts against Hikari's default pool size (10) was
     * the first thing tried here, and it cascaded into unrelated failures in
     * other test classes sharing this same Postgres container — the pool
     * was still recovering when they ran. 8 stays comfortably under that
     * ceiling while still being enough concurrency to hit the race reliably.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void exactlyOneOfManyConcurrentBookingsForTheSameSlotSucceeds() throws Exception {
        Practitioner practitioner = practitionerRepository.saveAndFlush(newPractitioner());
        Instant start = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant end = start.plusSeconds(1800);

        int attempts = 8;
        ExecutorService pool = Executors.newFixedThreadPool(attempts);

        try {
            List<Callable<Boolean>> bookings = IntStream.range(0, attempts)
                    .<Callable<Boolean>>mapToObj(i -> () -> {
                        try {
                            appointmentRepository.saveAndFlush(newAppointment(practitioner.getId(), start, end));
                            return true;
                        } catch (DataIntegrityViolationException | CannotAcquireLockException lostTheRace) {
                            return false;
                        }
                    })
                    .toList();

            List<Future<Boolean>> results = pool.invokeAll(bookings);
            long successCount = results.stream()
                    .map(this::getUnchecked)
                    .filter(Boolean::booleanValue)
                    .count();

            assertThat(successCount).isEqualTo(1);
        } finally {
            pool.shutdown();
        }
    }

    private boolean getUnchecked(Future<Boolean> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Practitioner newPractitioner() {
        return Practitioner.builder()
                .npi(UUID.randomUUID().toString().substring(0, 10))
                .fullName("Dr. Jamie Doe")
                .specialty(Specialty.GENERAL_PRACTICE)
                .build();
    }

    private Appointment newAppointment(UUID practitionerId, Instant start, Instant end) {
        return Appointment.builder()
                .practitionerId(practitionerId)
                .patientId(UUID.randomUUID())
                .slotStart(start)
                .slotEnd(end)
                .build();
    }
}
