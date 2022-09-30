package com.jsolution.service.impl;

import com.jsolution.pagination.PageSupport;
import com.jsolution.repository.IGenericRepository;
import com.jsolution.service.ICRUD;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

public abstract class CRUDImpl<T, ID> implements ICRUD<T, ID> {

    protected abstract IGenericRepository<T, ID> getRepository();
    //Se desea un metodo que se comporte diferente a la implementación

    @Override
    public Mono<T> save(T t) {return getRepository().save(t);}

    @Override
    public Mono<T> update(T t) {
        return getRepository().save(t);
    }

    @Override
    public Flux<T> findAll() {
        return getRepository().findAll();
    }

    @Override
    public Mono<T> findById(ID id) {
        return getRepository().findById(id);
    }

    @Override
    public Mono<Void> delete(ID id) {
        return getRepository().deleteById(id);
    }

    public Mono<PageSupport<T>> getPage(Pageable page){
        return getRepository().findAll()
                .collectList()
                .map(list -> new PageSupport<>(
                        list.stream()
                                .skip(page.getPageNumber() * page.getPageSize())
                                .limit(page.getPageSize())
                                .collect(Collectors.toList()),
                        page.getPageNumber(), page.getPageSize(), list.size())
                );
    }
}
