package com.yahyaelomari.healthpack.scheduling.service;

import com.yahyaelomari.healthpack.scheduling.api.dto.RegisterPractitionerRequest;
import com.yahyaelomari.healthpack.scheduling.domain.Practitioner;
import com.yahyaelomari.healthpack.scheduling.domain.Specialty;
import com.yahyaelomari.healthpack.scheduling.exception.PractitionerNotFoundException;
import com.yahyaelomari.healthpack.scheduling.mapper.PractitionerMapper;
import com.yahyaelomari.healthpack.scheduling.repository.PractitionerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PractitionerServiceTest {

    @Mock
    private PractitionerRepository practitionerRepository;

    @Mock
    private PractitionerMapper practitionerMapper;

    @InjectMocks
    private PractitionerService practitionerService;

    @Test
    void registerSavesAPractitionerWithTheGivenNpi() {
        when(practitionerRepository.save(any(Practitioner.class))).thenAnswer(call -> call.getArgument(0));

        practitionerService.register(
                new RegisterPractitionerRequest("1234567890", "Dr. Jamie Doe", Specialty.CARDIOLOGY));

        verify(practitionerRepository).save(argThat(p -> p.getNpi().equals("1234567890")));
    }

    @Test
    void findByIdThrowsWhenThePractitionerDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(practitionerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> practitionerService.findById(id))
                .isInstanceOf(PractitionerNotFoundException.class);
    }
}
