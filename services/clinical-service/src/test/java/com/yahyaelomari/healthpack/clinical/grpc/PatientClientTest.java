package com.yahyaelomari.healthpack.clinical.grpc;

import com.yahyaelomari.healthpack.clinical.support.AbstractIntegrationTest;
import com.yahyaelomari.healthpack.proto.patient.GetPatientRequest;
import com.yahyaelomari.healthpack.proto.patient.GetPatientResponse;
import com.yahyaelomari.healthpack.proto.patient.PatientLookupServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * No mocking of PatientClient itself here - a fake PatientLookupService runs
 * in-process and this proves the stub built in PatientClient's constructor
 * can really reach it over gRPC, not just that the Java wiring compiles.
 *
 * <p>The security exclude matches patient-service's real config (see
 * PatientGrpcService's javadoc): FakePatientLookupService below is a
 * BindableService, so without it Spring would demand a JWT this client
 * never sends.
 *
 * <p>Extends AbstractIntegrationTest even though this test never touches the
 * database: {@code @SpringBootTest} boots the whole application context,
 * including the real JPA/DataSource auto-configuration, so it still needs a
 * real Postgres to connect to.
 */
@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.grpc.server.autoconfigure.security.GrpcServerSecurityAutoConfiguration,"
        + "org.springframework.boot.grpc.server.autoconfigure.security.GrpcServerOAuth2ResourceServerAutoConfiguration")
@AutoConfigureTestGrpcTransport
class PatientClientTest extends AbstractIntegrationTest {

    @Autowired
    private PatientClient patientClient;

    @Test
    void existsIsTrueWhenTheFakeServerKnowsThePatient() {
        assertThat(patientClient.exists(FakePatientLookupService.KNOWN_ID)).isTrue();
    }

    @Test
    void existsIsFalseWhenTheFakeServerDoesNotKnowThePatient() {
        assertThat(patientClient.exists(UUID.randomUUID())).isFalse();
    }

    @TestConfiguration
    static class FakeServerConfig {

        @Bean
        FakePatientLookupService fakePatientLookupService() {
            return new FakePatientLookupService();
        }
    }

    @GrpcService
    static class FakePatientLookupService extends PatientLookupServiceGrpc.PatientLookupServiceImplBase {

        static final UUID KNOWN_ID = UUID.randomUUID();

        @Override
        public void getPatient(GetPatientRequest request, StreamObserver<GetPatientResponse> responseObserver) {
            boolean found = request.getId().equals(KNOWN_ID.toString());
            responseObserver.onNext(GetPatientResponse.newBuilder().setFound(found).build());
            responseObserver.onCompleted();
        }
    }
}
