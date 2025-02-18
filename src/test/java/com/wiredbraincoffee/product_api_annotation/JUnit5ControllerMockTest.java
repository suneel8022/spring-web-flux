package com.wiredbraincoffee.product_api_annotation;

import com.wiredbraincoffee.product_api_annotation.controller.ProductController;
import com.wiredbraincoffee.product_api_annotation.model.Product;
import com.wiredbraincoffee.product_api_annotation.model.ProductEvent;
import com.wiredbraincoffee.product_api_annotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class JUnit5ControllerMockTest {

    private WebTestClient webTestClient;

    private List<Product> expectedList;

    @MockitoBean
    private ProductRepository productRepository;

    @BeforeEach
    void beforeEach(){
        this.webTestClient = WebTestClient
                .bindToController(new ProductController(productRepository))
                .configureClient()
                .baseUrl("/products")
                .build();

        this.expectedList = Arrays.asList(
                new Product("1","Big Latte", 99.9)
        );
    }


    @Test
    void testGetAllProducts(){
        // Mocking the behavior of productRepository.findAll() to return a Flux stream
        // containing the expected list of products. This ensures that when the test
        // calls findAll(), it gets predefined data instead of interacting with the real database
        when(productRepository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));
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
        String id = "aaa";
        when(productRepository.findById(id)).thenReturn(Mono.empty());    // Mocking that "aaa" is not found

        webTestClient.get()
                .uri("/aaa")
                .exchange()
                .expectStatus()
                .isNotFound();
    }


    @Test
    void testProductIdFound(){
        Product expectedProduct = this.expectedList.get(0);
        when(productRepository.findById(expectedProduct.getId()))
                .thenReturn(Mono.just(expectedProduct));  // Mocking that the product is found


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
