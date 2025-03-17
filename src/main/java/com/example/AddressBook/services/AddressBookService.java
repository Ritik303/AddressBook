package com.example.AddressBook.services;

import com.example.AddressBook.Interfaces.IAddressBookService;
import com.example.AddressBook.Repository.AddressBookRepository;
import com.example.AddressBook.dto.AddressBookDTO;
import com.example.AddressBook.entity.AddressBook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AddressBookService implements IAddressBookService {

    @Autowired
    private AddressBookRepository addressBookRepository;

    @Override
    public List<AddressBook> getAllContacts() {
        return addressBookRepository.findAll();
    }

    @Override
    public Optional<AddressBook> getContactById(int id) {
        return addressBookRepository.findById(id);
    }

    @Override
    public AddressBook addContact(AddressBookDTO addressBookDTO) {
        AddressBook contact = new AddressBook();
        contact.setName(addressBookDTO.getName());
        contact.setPhone(addressBookDTO.getPhone());
        contact.setAddress(addressBookDTO.getAddress());
        return addressBookRepository.save(contact);
    }

    @Override
    public Optional<AddressBook> updateContact(int id, AddressBookDTO addressBookDTO) {
        return addressBookRepository.findById(id).map(contact -> {
            contact.setName(addressBookDTO.getName());
            contact.setPhone(addressBookDTO.getPhone());
            contact.setAddress(addressBookDTO.getAddress());
            return addressBookRepository.save(contact);
        });
    }

    @Override
    public boolean deleteContact(int id) {
        if (addressBookRepository.existsById(id)) {
            addressBookRepository.deleteById(id);
            return true;
        }
        return false;
    }
}