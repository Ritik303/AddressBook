package com.example.AddressBook.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data// Lombok generates getters, setters, toString, equals, and hashCode
@Table(name = "address_book")
@NoArgsConstructor
@AllArgsConstructor
public class AddressBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String phone;
    private String address;
}