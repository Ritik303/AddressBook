package com.example.AddressBook.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordDTO {
    private String email;
    private String newPassword;
}