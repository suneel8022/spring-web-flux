package com.wiredbraincoffee.product_api_annotation;

import com.wiredbraincoffee.product_api_annotation.model.Product;
import com.wiredbraincoffee.product_api_annotation.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ProductApiAnnotationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductApiAnnotationApplication.class, args);
	}


	@Bean
	CommandLineRunner init(ProductRepository productRepository){
		return args -> {
			Flux<Product> productFlux = Flux.just(
					new Product(null,"Big Latte", 99.9),
					new Product(null,"Espresso", 59.9),
					new Product(null,"Green Tea", 59.9)
			)
//					.flatMap(product -> productRepository.save(product))
					.flatMap(productRepository::save);


			productFlux
					.thenMany(productRepository.findAll())
					.subscribe(System.out::println);

		};
	}

}
