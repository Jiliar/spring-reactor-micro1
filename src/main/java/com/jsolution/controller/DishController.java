package com.jsolution.controller;

import com.jsolution.model.Dish;
import com.jsolution.service.IDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;


@RestController
@RequestMapping("/dishes")
public class DishController {

    @Autowired
    private IDishService service;

    @GetMapping
    public Mono<ResponseEntity<Flux<Dish>>>findAll() {
        Flux<Dish> fx = service.findAll();
        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Dish>> findById(@PathVariable("id") String id){
        return service.findById(id)
                .map(e->ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Dish>> save(@RequestBody Dish dish, final ServerHttpRequest request){
        return service.save(dish)
                .map(e->ResponseEntity
                        .created(URI.create(request.getURI()
                                .toString()
                                .concat("/")
                                .concat(e.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Dish>> update(@PathVariable("id") String id, @RequestBody Dish dish){

        Mono<Dish> monoBody = Mono.just(dish);
        Mono<Dish> monoDB = service.findById(id);

        return monoDB.zipWith(monoBody, (bd, d)->{
            bd.setId(id);
            bd.setName(d.getName());
            bd.setPrice(d.getPrice());
            bd.setStatus(d.getStatus());
            return bd;
        })
                .flatMap(e->service.update(e)) //service::update
                .map(e -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable("id") String id){
        return service.findById(id)
                .flatMap(e->service.delete(e.getId())
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                        //.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    //private Dish dishHateoas;

    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel<Dish>> getHateoasById(@PathVariable("id") String id){
        Mono<Link> link1 = linkTo(methodOn(DishController.class).findById(id)).withSelfRel().toMono();
        // Practica con reactor-extra: Uniendo dos links
        Mono<Link> link2 = linkTo(methodOn(DishController.class).findAll()).withSelfRel().toMono();
        return link1
                .zipWith(link2)
                .map(function((lk1, lk2) -> Links.of(lk1, lk2)))
                .zipWith(service.findById(id), (lk3, dish) -> EntityModel.of(dish, lk3));

    }


}
