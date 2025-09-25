package com.example.user;

import com.example.common.config.GrpcExceptionHandlingConfig;
import com.example.common.interceptors.GlobalExceptionInterceptor;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.user", "com.example.common"})
@Slf4j
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    /**
     * Configure gRPC server with global exception interceptor
     * This ensures all exceptions are properly handled and converted to gRPC status
     */
    @Bean
    public GrpcServerConfigurer grpcServerConfigurer(GrpcExceptionHandlingConfig exceptionConfig) {
        return serverBuilder -> {
            GlobalExceptionInterceptor interceptor = exceptionConfig.getGlobalExceptionInterceptor();
            if (interceptor != null) {
                serverBuilder.intercept(interceptor);
                log.info("GlobalExceptionInterceptor registered successfully");
            } else {
                log.warn("GlobalExceptionInterceptor is not available - exceptions may not be handled properly");
            }
        };
    }
}