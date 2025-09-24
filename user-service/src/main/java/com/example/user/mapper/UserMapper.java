package com.example.user.mapper;

import com.example.common.CommonProto;
import com.example.common.CommonProto.Address;
import com.example.user.UserProto.CreateUserRequest;
import com.example.user.UserProto.UpdateUserRequest;
import com.example.user.entity.ContactAddress;
import com.example.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public CommonProto.User toProto(User user) {
        if (user == null) return null;

        CommonProto.User.Builder userBuilder = CommonProto.User.newBuilder()
                .setId(user.getUserId())
                .setName(user.getFullName())
                .setEmail(user.getEmailAddress())
                .setPhone(user.getPhoneNumber());

        if (user.getContactAddress() != null) {
            Address address = mapToAddressProto(user.getContactAddress());
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

    public User mapToUserEntity(CreateUserRequest createRequest) {
        if (createRequest == null) return null;

        return User.builder()
                .fullName(createRequest.getName())
                .emailAddress(createRequest.getEmail())
                .phoneNumber(createRequest.getPhone())
                .contactAddress(createRequest.hasAddress() ? mapToContactAddress(createRequest.getAddress()) : null)
                .build();
    }

    public void updateUserEntity(User existingUser, UpdateUserRequest updateRequest) {
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
