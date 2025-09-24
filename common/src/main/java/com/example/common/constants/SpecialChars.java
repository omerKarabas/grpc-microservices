package com.example.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Special characters used throughout the application
 */
@Getter
@RequiredArgsConstructor
public enum SpecialChars {
    
    COLON_SPACE(": ");
    
    private final String value;
    
    @Override
    public String toString() {
        return value;
    }
}
