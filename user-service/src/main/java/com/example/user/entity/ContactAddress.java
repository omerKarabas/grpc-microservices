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
public class ContactAddress {

    @Column(length = 200)
    private String streetAddress;
    
    @Column(nullable = false, length = 100)
    private String cityName;
    
    @Column(length = 100)
    private String stateName;
    
    @Column(length = 20)
    private String postalCode;
    
    @Column(nullable = false, length = 100)
    private String countryName;

}
