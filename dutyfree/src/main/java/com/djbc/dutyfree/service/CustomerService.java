package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.Customer;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public Customer createCustomer(Customer customer) {
        // Validate email uniqueness
        if (customer.getEmail() != null && customerRepository.existsByEmail(customer.getEmail())) {
            throw new BadRequestException("Customer with email " + customer.getEmail() + " already exists");
        }

        // Validate phone uniqueness
        if (customer.getPhone() != null && customerRepository.existsByPhone(customer.getPhone())) {
            throw new BadRequestException("Customer with phone " + customer.getPhone() + " already exists");
        }

        customer.setActive(true);
        customer = customerRepository.save(customer);

        log.info("Customer created: {} {}", customer.getFirstName(), customer.getLastName());
        return customer;
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer customerData) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        // Validate email uniqueness (excluding current customer)
        if (customerData.getEmail() != null &&
                !customerData.getEmail().equals(customer.getEmail()) &&
                customerRepository.existsByEmail(customerData.getEmail())) {
            throw new BadRequestException("Customer with email " + customerData.getEmail() + " already exists");
        }

        // Validate phone uniqueness (excluding current customer)
        if (customerData.getPhone() != null &&
                !customerData.getPhone().equals(customer.getPhone()) &&
                customerRepository.existsByPhone(customerData.getPhone())) {
            throw new BadRequestException("Customer with phone " + customerData.getPhone() + " already exists");
        }

        customer.setFirstName(customerData.getFirstName());
        customer.setLastName(customerData.getLastName());
        customer.setEmail(customerData.getEmail());
        customer.setPhone(customerData.getPhone());
        customer.setDateOfBirth(customerData.getDateOfBirth());
        customer.setGender(customerData.getGender());
        customer.setNationality(customerData.getNationality());
        customer.setPassportNumber(customerData.getPassportNumber());
        customer.setAddress(customerData.getAddress());
        customer.setCity(customerData.getCity());
        customer.setCountry(customerData.getCountry());
        customer.setPostalCode(customerData.getPostalCode());
        customer.setBadgeNumber(customerData.getBadgeNumber());
        customer.setCompanyName(customerData.getCompanyName());
        customer.setIsVIP(customerData.getIsVIP());

        customer = customerRepository.save(customer);
        log.info("Customer updated: {}", id);

        return customer;
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
    }

    @Transactional(readOnly = true)
    public Customer getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "phone", phone));
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Customer> searchCustomers(String search) {
        return customerRepository.searchCustomers(search);
    }

    @Transactional(readOnly = true)
    public List<Customer> getVIPCustomers() {
        return customerRepository.findByIsVIPTrue();
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customer.setDeleted(true);
        customer.setActive(false);
        customerRepository.save(customer);

        log.info("Customer deleted: {}", id);
    }
}