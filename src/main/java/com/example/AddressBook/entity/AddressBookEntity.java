package com.example.AddressBook.entity;
import jakarta.persistence.*;
import lombok.Data;

    @Entity
    @Data  // Lombok generates getters, setters, toString, equals, and hashCode
    public class AddressBookEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        private String name;
        private String phone;
        private String address;
    }

