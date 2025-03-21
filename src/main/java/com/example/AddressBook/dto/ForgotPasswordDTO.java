package com.example.AddressBook.dto;


import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor // ✅ Required for default constructor
@AllArgsConstructor // ✅ Generates constructor with all fields
public class ForgotPasswordDTO {
    private String email;
    private String newPassword;
}