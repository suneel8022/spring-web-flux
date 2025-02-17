package com.wiredbraincoffee.product_api_annotation;

import com.wiredbraincoffee.product_api_annotation.controller.ProductController;
import com.wiredbraincoffee.product_api_annotation.model.Product;
import com.wiredbraincoffee.product_api_annotation.model.ProductEvent;
import com.wiredbraincoffee.product_api_annotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class JUnit5ApplicationContextTest {

    private WebTestClient webTestClient;

    private List<Product> expectedList;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void beforeEach(){
        this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .configureClient()
                .baseUrl("/products")
                .build();

        this.expectedList = productRepository.findAll().collectList().block();
    }


    @Test
    void testGetAllProducts(){
        webTestClient.get()
                .uri("")        // try putting '/'
                .exchange()  // perform the request
                .expectStatus()  // tests the response(returns the status of the response)
                .isOk()
                .expectBodyList(Product.class)
                .isEqualTo(expectedList);
    }



    @Test
    void testProductInvalidIdNotFound(){
        webTestClient.get()
                .uri("/aaa")
                .exchange()
                .expectStatus()
                .isNotFound();
    }


    @Test
    void testProductIdFound(){
        Product expectedProduct = expectedList.get(0);
        webTestClient.get()
                .uri("/{id}", expectedProduct.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Product.class)
                .isEqualTo(expectedProduct);
    }


    @Test
    void testProductEvents(){
        ProductEvent expectedEvent =
                new ProductEvent(0L,"ProductEvent");

        FluxExchangeResult<ProductEvent> result =
                webTestClient
                        .get()
                        .uri("/events")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .returnResult(ProductEvent.class);

        StepVerifier.create(result.getResponseBody())
                .expectNext(expectedEvent)
                .expectNextCount(2)
                .consumeNextWith(event ->
                        assertEquals(Long.valueOf(3), event.getEventId()))
                .thenCancel()
                .verify();
    }






}
