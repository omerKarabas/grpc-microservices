package com.example.user.service;

import com.example.common.CommonProto;
import com.example.common.ResponseBuilder;
import com.example.common.exception.DuplicateResourceException;
import com.example.common.exception.ResourceNotFoundException;
import com.example.user.UserProto.*;
import com.example.user.constants.UserErrorCode;
import com.example.user.entity.User;
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

        // Check for duplicate email
        if (existsUserByEmail(createRequest.getEmail())) {
            throw new DuplicateResourceException(
                UserErrorCode.USER_ALREADY_EXISTS,
                String.format("User with email '%s' already exists", createRequest.getEmail())
            );
        }

        // Map request to entity and save
        User newUser = userMapper.mapToUserEntity(createRequest);
        User savedUser = saveUser(newUser);
        CommonProto.User userProto = userMapper.toProto(savedUser);

        // Return success response
        CreateUserResponse response = CreateUserResponse.newBuilder()
                .setResponse(ResponseBuilder.success("User created successfully"))
                .setUser(userProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUser(GetUserRequest getUserRequest, StreamObserver<GetUserResponse> responseObserver) {
        log.info("Fetching user: {}", getUserRequest.getUserId());

        Optional<User> foundUser = findUserById(getUserRequest.getUserId());

        if (foundUser.isEmpty()) {
            throw new ResourceNotFoundException(
                UserErrorCode.USER_NOT_FOUND,
                String.format("User with ID '%s' not found", getUserRequest.getUserId())
            );
        }

        CommonProto.User userProto = userMapper.toProto(foundUser.get());
        GetUserResponse response = GetUserResponse.newBuilder()
                .setResponse(ResponseBuilder.success("User found"))
                .setUser(userProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateUser(UpdateUserRequest updateRequest, StreamObserver<UpdateUserResponse> responseObserver) {
        log.info("Updating user: {}", updateRequest.getUserId());

        Optional<User> existingUserOpt = findUserById(updateRequest.getUserId());

        if (existingUserOpt.isEmpty()) {
            throw new ResourceNotFoundException(
                UserErrorCode.USER_TO_UPDATE_NOT_FOUND,
                String.format("User with ID '%s' not found", updateRequest.getUserId())
            );
        }

        User existingUser = existingUserOpt.get();
        userMapper.updateUserEntity(existingUser, updateRequest);

        User updatedUser = saveUser(existingUser);
        CommonProto.User userProto = userMapper.toProto(updatedUser);

        UpdateUserResponse response = UpdateUserResponse.newBuilder()
                .setResponse(ResponseBuilder.success("User updated successfully"))
                .setUser(userProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteUser(DeleteUserRequest deleteRequest, StreamObserver<DeleteUserResponse> responseObserver) {
        log.info("Deleting user: {}", deleteRequest.getUserId());

        if (!existsUserById(deleteRequest.getUserId())) {
            throw new ResourceNotFoundException(
                UserErrorCode.USER_TO_DELETE_NOT_FOUND,
                String.format("User with ID '%s' not found", deleteRequest.getUserId())
            );
        }

        userRepository.deleteById(deleteRequest.getUserId());
        DeleteUserResponse response = DeleteUserResponse.newBuilder()
                .setResponse(ResponseBuilder.success("User deleted successfully"))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void validateUser(ValidateUserRequest validationRequest, StreamObserver<ValidateUserResponse> responseObserver) {
        log.info("Validating user: {}", validationRequest.getUserId());

        Optional<User> foundUser = findUserById(validationRequest.getUserId());

        if (foundUser.isEmpty()) {
            throw new ResourceNotFoundException(
                UserErrorCode.USER_NOT_FOUND,
                String.format("User with ID '%s' not found", validationRequest.getUserId())
            );
        }

        CommonProto.User userProto = userMapper.toProto(foundUser.get());
        ValidateUserResponse response = ValidateUserResponse.newBuilder()
                .setIsValid(true)
                .setUser(userProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    private User saveUser(User user) {
        log.debug("Saving user with ID: {}", user.getUserId());
        return userRepository.save(user);
    }

    private Optional<User> findUserById(Long userId) {
        log.debug("Finding user by ID: {}", userId);
        return userRepository.findById(userId);
    }

    private boolean existsUserByEmail(String email) {
        log.debug("Checking if user exists by email: {}", email);
        return userRepository.existsByEmailAddress(email);
    }
    
    private boolean existsUserById(Long userId) {
        log.debug("Checking if user exists by ID: {}", userId);
        return userRepository.existsById(userId);
    }
    
}