package com.example.AddressBook.dto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
// Lombok automatically generates getters, setters, toString, etc.
@AllArgsConstructor
@NoArgsConstructor
public class AddressBookDTO {

    @NotEmpty(message = "Name is required")
    @Pattern(regexp = "^[A-Z][a-zA-Z\\s]{2,}$", message = "Name must start with a capital letter and have at least 3 characters")
    private String name;
    @NotEmpty(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @NotEmpty(message = "Address cannot be empty")
    private String address;
}