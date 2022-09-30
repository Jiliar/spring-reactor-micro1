package com.jsolution.repository;

import com.jsolution.model.Customer;
import org.springframework.stereotype.Repository;

@Repository
public interface ICustomerRepository extends IGenericRepository<Customer, String>{
}
