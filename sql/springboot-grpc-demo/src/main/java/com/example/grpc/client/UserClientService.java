package com.example.grpc.client;

import com.example.grpc.UserRequest;
import com.example.grpc.UserResponse;
import com.example.grpc.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class UserClientService {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub stub;

    @PostConstruct
    public void testCall() {
        UserResponse response = stub.getUser(UserRequest.newBuilder()
                .setUserId("u123")
                .build());

        System.out.println("客户端收到: " + response.getName());
    }
}
