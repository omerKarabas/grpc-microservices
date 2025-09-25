package com.example.common.config;

import com.example.common.interceptors.GlobalExceptionInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Configuration class for gRPC global exception handling
 * Note: Manual registration may be required in each service module
 * since @GrpcGlobalServerInterceptor annotation is not available in current gRPC version
 */
@Configuration
@Slf4j
public class GrpcExceptionHandlingConfig {

    @Autowired(required = false)
    private GlobalExceptionInterceptor globalExceptionInterceptor;

    @PostConstruct
    public void init() {
        if (globalExceptionInterceptor != null) {
            log.info("GlobalExceptionInterceptor is configured and ready to handle exceptions");
        } else {
            log.warn("GlobalExceptionInterceptor is not properly configured. " +
                    "Please register it manually in your gRPC server configuration.");
        }
    }

    /**
     * Get the global exception interceptor instance
     * Services should register this interceptor manually in their gRPC server configuration
     *
     * @return GlobalExceptionInterceptor instance
     */
    public GlobalExceptionInterceptor getGlobalExceptionInterceptor() {
        return globalExceptionInterceptor;
    }
}
