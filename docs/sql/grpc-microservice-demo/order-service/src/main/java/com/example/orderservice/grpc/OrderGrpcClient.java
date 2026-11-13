package com.example.orderservice.grpc;

import com.example.commonproto.UserRequest;
import com.example.commonproto.UserResponse;
import com.example.commonproto.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class OrderGrpcClient {
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    @PostConstruct
    public void callUser() {
        UserResponse response = userStub.getUser(UserRequest.newBuilder().setUserId("1001").build());
        System.out.println("订单服务收到用户信息: " + response.getName());
    }
}
