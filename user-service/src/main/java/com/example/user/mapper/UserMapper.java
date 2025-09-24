package com.example.user.mapper;

import com.example.common.CommonProto.Address;
import com.example.common.CommonProto.User;
import com.example.user.UserProto.CreateUserRequest;
import com.example.user.UserProto.UpdateUserRequest;
import com.example.user.entity.ContactAddress;
import com.example.user.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserMapper {

    public User toProto(UserEntity userEntity) {
        if (userEntity == null) return null;

        User.Builder userBuilder = User.newBuilder()
                .setId(userEntity.getUserId())
                .setName(userEntity.getFullName())
                .setEmail(userEntity.getEmailAddress())
                .setPhone(userEntity.getPhoneNumber());

        if (userEntity.getContactAddress() != null) {
            Address address = mapToAddressProto(userEntity.getContactAddress());
            userBuilder.setAddress(address);
        }

        return userBuilder.build();
    }


    public ContactAddress mapToContactAddress(Address addressProto) {
        if (addressProto == null) return null;

        return ContactAddress.builder()
                .streetAddress(addressProto.getStreet())
                .cityName(addressProto.getCity())
                .stateName(addressProto.getState())
                .postalCode(addressProto.getZipCode())
                .countryName(addressProto.getCountry())
                .build();
    }

    public UserEntity mapToUserEntity(CreateUserRequest createRequest) {
        if (createRequest == null) return null;

        return UserEntity.builder()
                .fullName(createRequest.getName())
                .emailAddress(createRequest.getEmail())
                .phoneNumber(createRequest.getPhone())
                .contactAddress(createRequest.hasAddress() ? mapToContactAddress(createRequest.getAddress()) : null)
                .build();
    }

    public void updateUserEntity(UserEntity existingUser, UpdateUserRequest updateRequest) {
        if (existingUser == null || updateRequest == null) return;

        existingUser.setFullName(updateRequest.getName());
        existingUser.setEmailAddress(updateRequest.getEmail());
        existingUser.setPhoneNumber(updateRequest.getPhone());
        
        if (updateRequest.hasAddress()) {
            existingUser.setContactAddress(mapToContactAddress(updateRequest.getAddress()));
        }
    }
    
    private Address mapToAddressProto(ContactAddress contactAddress) {
        return Address.newBuilder()
                .setStreet(contactAddress.getStreetAddress())
                .setCity(contactAddress.getCityName())
                .setState(contactAddress.getStateName())
                .setZipCode(contactAddress.getPostalCode())
                .setCountry(contactAddress.getCountryName())
                .build();
    }
}
