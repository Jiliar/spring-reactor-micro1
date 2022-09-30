package com.jsolution.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.jsolution.model.Customer;
import com.jsolution.pagination.PageSupport;
import com.jsolution.service.ICustomerService;
import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;


@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private ICustomerService service;

    @Value("${cloudinary.cloud_name}")
    private String cloud_name;

    @Value("${cloudinary.api_key}")
    private String api_key;

    @Value("${cloudinary.api_secret}")
    private String api_secret;

    @GetMapping
    public Mono<ResponseEntity<Flux<Customer>>>findAll() {
        Flux<Customer> fx = service.findAll();
        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fx));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Customer>> findById(@PathVariable("id") String id){
        return service.findById(id)
                .map(e->ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Customer>> save(@RequestBody Customer Customer, final ServerHttpRequest request){
        return service.save(Customer)
                .map(e->ResponseEntity
                        .created(URI.create(request.getURI()
                                .toString()
                                .concat("/")
                                .concat(e.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(e));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Customer>> update(@PathVariable("id") String id, @RequestBody Customer Customer){

        Mono<Customer> monoBody = Mono.just(Customer);
        Mono<Customer> monoDB = service.findById(id);

        return monoDB.zipWith(monoBody, (bd, d)->{
                    bd.setId(id);
                    bd.setNames(d.getNames());
                    bd.setLastnames(d.getLastnames());
                    bd.setBirthday(d.getBirthday());
                    bd.setUrl_picture(d.getUrl_picture());
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

    //private Customer CustomerHateoas;

    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel<Customer>> getHateoasById(@PathVariable("id") String id){
        Mono<Link> link1 = linkTo(methodOn(CustomerController.class).findById(id)).withSelfRel().toMono();
        //Practica No recomendada: Debe usar variable global y esta puede ser modificada desde otro metodo.
        /*return service.findById(id)
                .flatMap(Customer -> {
                    this.CustomerHateoas = Customer;
                    return link1;
                })
                .map(link -> EntityModel.of(CustomerHateoas, link));*/

        //Practica Intermedia: Se podria incurrir con callbacks dentro de otros callbacks
        /*return service.findById(id)
                .flatMap(Customer -> {
                    return link1.map(lk -> EntityModel.of(Customer, lk));
                });*/

        // Practica Mejorada
        /*return service.findById(id)
                .zipWith(link1, (Customer, lk)->EntityModel.of(Customer, lk));*/

        //Practica simplificada

        /*return service.findById(id)
                .zipWith(link1, EntityModel::of);*/

        // Practica con reactor-extra: Uniendo dos links
        Mono<Link> link2 = linkTo(methodOn(CustomerController.class).findAll()).withSelfRel().toMono();
        return link1
                .zipWith(link2)
                .map(function((lk1, lk2) -> Links.of(lk1, lk2)))
                .zipWith(service.findById(id), (lk3, Customer) -> EntityModel.of(Customer, lk3));

    }

    @PostMapping("/v1/upload/{id}")
    //Cloudinary no tiene metodos de procesamiento reactivo.
    public Mono<ResponseEntity<Customer>> uploadV1(@PathVariable("id") String id, @RequestPart("file")FilePart file) throws IOException {

        Cloudinary cloudinary =  new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloud_name,
                "api_key", api_key,
                "api_secret", api_secret
        ));

        File f = Files.createTempFile("temp", file.filename()).toFile();
        return file.transferTo(f)
                .then(service.findById(id)
                        .flatMap(c ->{
                            Map response;
                            try {
                                response = cloudinary.uploader().upload(f, ObjectUtils.asMap("resource_type", "auto"));
                                JSONObject json = new JSONObject(response);
                                String url = json.getString("url");
                                c.setUrl_picture(url);
                            }catch (IOException e){
                                throw new RuntimeException(e);
                            }
                            return service.update(c).thenReturn(ResponseEntity.ok().body(c));
                        }).defaultIfEmpty(ResponseEntity.notFound().build())
                );
    }


    @PostMapping("/v2/upload/{id}")
    public Mono<ResponseEntity<Customer>> uploadV2(@PathVariable("id") String id, @RequestPart("file")FilePart file) throws IOException {

        Cloudinary cloudinary =  new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloud_name,
                "api_key", api_key,
                "api_secret", api_secret
        ));

        return service.findById(id)
                      .flatMap(c ->{
                            try {
                                File f = Files.createTempFile("temp", file.filename()).toFile();
                                file.transferTo(f).block();

                                Map response = cloudinary.uploader().upload(f, ObjectUtils.asMap("resource_type", "auto"));
                                JSONObject json = new JSONObject(response);
                                String url = json.getString("url");

                                c.setUrl_picture(url);
                                return service.update(c).thenReturn(ResponseEntity.ok().body(c));
                            }catch (IOException e){
                                throw new RuntimeException(e);
                            }
                        }).defaultIfEmpty(ResponseEntity.notFound().build());

    }

    @GetMapping("/pageable")
    public Mono<ResponseEntity<PageSupport<Customer>>> getPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ){
        return service.getPage(PageRequest.of(page, size))
                .map(pag -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(pag))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
