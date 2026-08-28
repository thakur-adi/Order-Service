package dev.aditya.orderservice.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//This class only getting used in AuthFilter so that user details can be saved from http headers and shared with Payment Service as customer details.
public class User {
    private Long userId;
    private String userName;
    private String email;
    private String phoneNumber;
}
