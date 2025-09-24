package com.example.user.service;

import com.example.common.CommonProto.*;
import com.example.common.ResponseBuilder;
import com.example.common.util.StreamResponseHandler;
import com.example.user.UserProto.*;
import com.example.user.entity.UserEntity;
import com.example.user.mapper.UserMapper;
import com.example.user.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@GrpcService
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends com.example.user.UserServiceGrpc.UserServiceImplBase {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public void createUser(CreateUserRequest createRequest, StreamObserver<CreateUserResponse> responseObserver) {
        log.info("Creating user: {}", createRequest.getEmail());
        
        try {
            if (userRepository.existsByEmailAddress(createRequest.getEmail())) {
                StreamResponseHandler.respond(responseObserver, CreateUserResponse.newBuilder()
                        .setResponse(ResponseBuilder.error("User with email already exists", "USER_ALREADY_EXISTS"))
                        .build());
                return;
            }
            
            UserEntity newUser = userMapper.mapToUserEntity(createRequest);
            
            UserEntity savedUser = userRepository.save(newUser);
            User userProto = userMapper.toProto(savedUser);
            
            StreamResponseHandler.respond(responseObserver, CreateUserResponse.newBuilder()
                    .setResponse(ResponseBuilder.success("User created successfully"))
                    .setUser(userProto)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error creating user", e);
            StreamResponseHandler.respond(responseObserver, CreateUserResponse.newBuilder()
                    .setResponse(ResponseBuilder.error("Failed to create user: " + e.getMessage(), "USER_CREATE_ERROR"))
                    .build());
        }
    }

    @Override
    public void getUser(GetUserRequest getUserRequest, StreamObserver<GetUserResponse> responseObserver) {
        log.info("Fetching user: {}", getUserRequest.getUserId());
        
        try {
            Optional<UserEntity> foundUser = userRepository.findById(getUserRequest.getUserId());
            
            if (foundUser.isEmpty()) {
                StreamResponseHandler.respond(responseObserver, GetUserResponse.newBuilder()
                        .setResponse(ResponseBuilder.error("User not found", "USER_NOT_FOUND"))
                        .build());
                return;
            }
            
            User userProto = userMapper.toProto(foundUser.get());
            StreamResponseHandler.respond(responseObserver, GetUserResponse.newBuilder()
                    .setResponse(ResponseBuilder.success("User found"))
                    .setUser(userProto)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error fetching user", e);
            StreamResponseHandler.respond(responseObserver, GetUserResponse.newBuilder()
                    .setResponse(ResponseBuilder.error("Failed to fetch user: " + e.getMessage(), "USER_FETCH_ERROR"))
                    .build());
        }
    }

    @Override
    public void updateUser(UpdateUserRequest updateRequest, StreamObserver<UpdateUserResponse> responseObserver) {
        log.info("Updating user: {}", updateRequest.getUserId());
        
        try {
            Optional<UserEntity> existingUserOpt = userRepository.findById(updateRequest.getUserId());
            
            if (existingUserOpt.isEmpty()) {
                StreamResponseHandler.respond(responseObserver, UpdateUserResponse.newBuilder()
                        .setResponse(ResponseBuilder.error("User to update not found", "USER_NOT_FOUND"))
                        .build());
                return;
            }
            
            UserEntity existingUser = existingUserOpt.get();
            userMapper.updateUserEntity(existingUser, updateRequest);
            
            UserEntity updatedUser = userRepository.save(existingUser);
            User userProto = userMapper.toProto(updatedUser);
            
            StreamResponseHandler.respond(responseObserver, UpdateUserResponse.newBuilder()
                    .setResponse(ResponseBuilder.success("User updated successfully"))
                    .setUser(userProto)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error updating user", e);
            StreamResponseHandler.respond(responseObserver, UpdateUserResponse.newBuilder()
                    .setResponse(ResponseBuilder.error("Failed to update user: " + e.getMessage(), "USER_UPDATE_ERROR"))
                    .build());
        }
    }

    @Override
    public void deleteUser(DeleteUserRequest deleteRequest, StreamObserver<DeleteUserResponse> responseObserver) {
        log.info("Deleting user: {}", deleteRequest.getUserId());
        
        try {
            if (!userRepository.existsById(deleteRequest.getUserId())) {
                StreamResponseHandler.respond(responseObserver, DeleteUserResponse.newBuilder()
                        .setResponse(ResponseBuilder.error("User to delete not found", "USER_NOT_FOUND"))
                        .build());
                return;
            }
            
            userRepository.deleteById(deleteRequest.getUserId());
            StreamResponseHandler.respond(responseObserver, DeleteUserResponse.newBuilder()
                    .setResponse(ResponseBuilder.success("User deleted successfully"))
                    .build());
                    
        } catch (Exception e) {
            log.error("Error deleting user", e);
            StreamResponseHandler.respond(responseObserver, DeleteUserResponse.newBuilder()
                    .setResponse(ResponseBuilder.error("Failed to delete user: " + e.getMessage(), "USER_DELETE_ERROR"))
                    .build());
        }
    }

    @Override
    public void validateUser(ValidateUserRequest validationRequest, StreamObserver<ValidateUserResponse> responseObserver) {
        log.info("Validating user: {}", validationRequest.getUserId());
        
        try {
            Optional<UserEntity> foundUser = userRepository.findById(validationRequest.getUserId());
            
            if (foundUser.isEmpty()) {
                StreamResponseHandler.respond(responseObserver, ValidateUserResponse.newBuilder()
                        .setIsValid(false)
                        .setErrorMessage("User not found")
                        .build());
                return;
            }
            
            User userProto = userMapper.toProto(foundUser.get());
            StreamResponseHandler.respond(responseObserver, ValidateUserResponse.newBuilder()
                    .setIsValid(true)
                    .setUser(userProto)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error validating user", e);
            StreamResponseHandler.respond(responseObserver, ValidateUserResponse.newBuilder()
                    .setIsValid(false)
                    .setErrorMessage("Validation error: " + e.getMessage())
                    .build());
        }
    }
    
}