package com.example.grpc.service;

import com.example.grpc.UserRequest;
import com.example.grpc.UserResponse;
import com.example.grpc.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GRpcService;

@GRpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    @Override
    public void getUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        UserResponse response = UserResponse.newBuilder()
                .setUserId(request.getUserId())
                .setName("Andy")
                .setAge(30)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
