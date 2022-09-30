package com.jsolution.repository;

import com.jsolution.model.Dish;
import org.springframework.stereotype.Repository;

@Repository
public interface IDishRepository extends IGenericRepository<Dish, String> {
}
