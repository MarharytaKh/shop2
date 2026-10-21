package edu.arch.shop.customer.service;

import edu.arch.shop.customer.dto.CustomerRequest;
import edu.arch.shop.customer.dto.CustomerResponse;
import edu.arch.shop.customer.entity.Customer;
import edu.arch.shop.customer.repository.CustomerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse findById(Long id) {
        return toResponse(findCustomer(id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        validate(request);
        Customer customer = new Customer(
                request.name(),
                request.surname(),
                request.numberOfPurchases()
        );
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        validate(request);
        Customer customer = findCustomer(id);
        customer.setName(request.name());
        customer.setSurname(request.surname());
        customer.setNumberOfPurchases(request.numberOfPurchases());
        return toResponse(customerRepository.save(customer));
    }

    /**
     * Klienta, do którego należą zamówienia, nie można usunąć (klucz obcy orders.customer_id).
     * Zamiast błędu 500 zwracamy 409 Conflict. Serwis klienta nie zależy od modułu zamówień —
     * o naruszeniu relacji informuje baza.
     */
    @Transactional
    public void delete(Long id) {
        Customer customer = findCustomer(id);
        try {
            customerRepository.delete(customer);
            customerRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer has orders");
        }
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void validate(CustomerRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (request.name().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is too long");
        }
        if (request.surname() == null || request.surname().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Surname is required");
        }
        if (request.surname().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Surname is too long");
        }
        if (request.numberOfPurchases() == null || request.numberOfPurchases() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of purchases cannot be negative");
        }
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getSurname(),
                customer.getNumberOfPurchases()
        );
    }
}

