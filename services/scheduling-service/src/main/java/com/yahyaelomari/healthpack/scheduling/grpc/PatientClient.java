package com.yahyaelomari.healthpack.scheduling.grpc;

import com.yahyaelomari.healthpack.proto.patient.GetPatientRequest;
import com.yahyaelomari.healthpack.proto.patient.GetPatientResponse;
import com.yahyaelomari.healthpack.proto.patient.PatientLookupServiceGrpc;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

// "patient" channel name matches spring.grpc.client.channel.patient.target
// in application.properties.
@Component
public class PatientClient {

    private final PatientLookupServiceGrpc.PatientLookupServiceBlockingStub stub;

    public PatientClient(GrpcChannelFactory channels) {
        this.stub = PatientLookupServiceGrpc.newBlockingStub(channels.createChannel("patient"));
    }

    public boolean exists(UUID patientId) {
        GetPatientRequest request = GetPatientRequest.newBuilder().setId(patientId.toString()).build();
        GetPatientResponse response = stub.getPatient(request);
        return response.getFound();
    }
}
