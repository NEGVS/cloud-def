package com.example.userservice.grpc;

import com.example.commonproto.UserRequest;
import com.example.commonproto.UserResponse;
import com.example.commonproto.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GRpcService;

@GRpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    @Override
    public void getUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        UserResponse response = UserResponse.newBuilder()
                .setUserId(request.getUserId())
                .setName("Alice")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
