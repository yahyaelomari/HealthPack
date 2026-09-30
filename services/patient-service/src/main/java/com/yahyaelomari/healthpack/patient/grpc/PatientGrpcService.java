package com.yahyaelomari.healthpack.patient.grpc;

import com.yahyaelomari.healthpack.patient.domain.Patient;
import com.yahyaelomari.healthpack.patient.repository.PatientRepository;
import com.yahyaelomari.healthpack.proto.patient.GetPatientRequest;
import com.yahyaelomari.healthpack.proto.patient.GetPatientResponse;
import com.yahyaelomari.healthpack.proto.patient.PatientLookupServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

/**
 * Internal only, same reasoning as {@code patient.proto}: other services
 * check a patientId is real before acting on it, nobody outside the cluster
 * calls this.
 *
 * <p>Not authenticated yet — {@code application.properties} excludes the
 * gRPC security auto-configuration because no service-to-service credential
 * exists to satisfy it (Keycloak's {@code healthpack-services} client would
 * need a client-credentials flow wired up, which is future work, not part
 * of this feature). Anyone who can reach the gRPC port can call this.
 */
@GrpcService
@RequiredArgsConstructor
public class PatientGrpcService extends PatientLookupServiceGrpc.PatientLookupServiceImplBase {

    private final PatientRepository patientRepository;

    @Override
    public void getPatient(GetPatientRequest request, StreamObserver<GetPatientResponse> responseObserver) {
        GetPatientResponse response = patientRepository.findById(UUID.fromString(request.getId()))
                .map(this::toFoundResponse)
                .orElseGet(() -> GetPatientResponse.newBuilder().setFound(false).build());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private GetPatientResponse toFoundResponse(Patient patient) {
        return GetPatientResponse.newBuilder()
                .setFound(true)
                .setId(patient.getId().toString())
                .setMrn(patient.getMrn())
                .setFirstName(patient.getFirstName())
                .setLastName(patient.getLastName())
                .build();
    }
}
