package com.jsolution.service.impl;

import com.jsolution.model.Dish;
import com.jsolution.repository.IDishRepository;
import com.jsolution.repository.IGenericRepository;
import com.jsolution.service.IDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DishServiceImpl extends CRUDImpl<Dish, String> implements IDishService{

    @Autowired
    private IDishRepository dishRepository;


    @Override
    protected IGenericRepository getRepository() {
        return dishRepository;
    }
}
