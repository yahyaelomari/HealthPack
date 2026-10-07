package com.yahyaelomari.healthpack.clinical.grpc;
import com.yahyaelomari.healthpack.proto.patient.GetPatientRequest;
import com.yahyaelomari.healthpack.proto.patient.GetPatientResponse;
import com.yahyaelomari.healthpack.proto.patient.PatientLookupServiceGrpc;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PatientClient {

    private final PatientLookupServiceGrpc.PatientLookupServiceBlockingStub stub;

    public PatientClient(GrpcChannelFactory channelFactory) {
        this.stub= PatientLookupServiceGrpc.newBlockingStub(channelFactory.createChannel("patient"));
    }

    public boolean exists(UUID patientId) {
        GetPatientRequest request = GetPatientRequest.newBuilder().setId(patientId.toString()).build();
        GetPatientResponse response = stub.getPatient(request);
        return response.getFound();
    }
}
