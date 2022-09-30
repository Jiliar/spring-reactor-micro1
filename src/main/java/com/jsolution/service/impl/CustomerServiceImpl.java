package com.jsolution.service.impl;

import com.jsolution.model.Customer;
import com.jsolution.repository.ICustomerRepository;
import com.jsolution.repository.IGenericRepository;
import com.jsolution.service.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl extends CRUDImpl<Customer, String> implements ICustomerService{

    @Autowired
    private ICustomerRepository CustomerRepository;

    @Override
    protected IGenericRepository getRepository() {
        return CustomerRepository;
    }
}
