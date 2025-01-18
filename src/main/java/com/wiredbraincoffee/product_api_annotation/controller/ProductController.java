package com.wiredbraincoffee.product_api_annotation.controller;


import com.wiredbraincoffee.product_api_annotation.model.Product;
import com.wiredbraincoffee.product_api_annotation.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductController {

    private ProductRepository productRepository;

    public ProductController(ProductRepository productRepository){
        this.productRepository=productRepository;
    }


    @GetMapping
    public Flux<Product> getAllProducts(){
        // if u observe there is no subscribe -> Spring will call it for you at right time :)
        return productRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Product>> getProduct(@PathVariable String id){
        return productRepository.findById(id)
//                .map(product -> ResponseEntity.ok(product))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
