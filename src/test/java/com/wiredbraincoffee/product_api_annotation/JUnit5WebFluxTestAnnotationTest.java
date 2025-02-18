package com.wiredbraincoffee.product_api_annotation;

import com.wiredbraincoffee.product_api_annotation.controller.ProductController;
import com.wiredbraincoffee.product_api_annotation.model.Product;
import com.wiredbraincoffee.product_api_annotation.model.ProductEvent;
import com.wiredbraincoffee.product_api_annotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
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

@WebFluxTest(ProductController.class)
public class JUnit5WebFluxTestAnnotationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private CommandLineRunner commandLineRunner;

    private List<Product> expectedList;

    @BeforeEach
    void beforeEach(){
        this.expectedList = List.of(
                new Product("1", "Big Latte", 2.99)
        );
    }


    @Test
    void testGetAllProducts(){
        when(productRepository.findAll())
                .thenReturn(Flux.fromIterable(this.expectedList));

            webTestClient
                    .get()
                    .uri("/products")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBodyList(Product.class)
                    .isEqualTo(expectedList);
    }


    @Test
    void testProductInvalidIdNotFound(){
        String id = "aaa";
        when(productRepository.findById(id))
                .thenReturn(Mono.empty());

        webTestClient
                .get()
                .uri("product/{id}",id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }


    @Test
    void testProductIdFound(){
        Product expectedProduct = this.expectedList.get(0);

        when(productRepository.findById(expectedProduct.getId()))
                .thenReturn(Mono.just(expectedProduct));

        webTestClient
                .get()
                .uri("/products/{id}",expectedProduct.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Product.class)
                .isEqualTo(expectedProduct);
    }


    @Test
    void testProductEvents() {
        ProductEvent expectedEvent =
                new ProductEvent(0L, "ProductEvent");

        FluxExchangeResult<ProductEvent> result =
                webTestClient.get().uri("/products/events")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
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
