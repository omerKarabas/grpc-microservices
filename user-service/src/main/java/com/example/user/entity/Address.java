package com.example.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Address {

    @Column(length = 200)
    private String street;
    
    @Column(nullable = false, length = 100)
    private String city;
    
    @Column(length = 100)
    private String state;
    
    @Column(length = 20)
    private String zipCode;
    
    @Column(nullable = false, length = 100)
    private String country;

}
