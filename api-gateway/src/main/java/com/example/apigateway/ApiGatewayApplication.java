package com.example.apigateway;

import java.time.LocalDate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;

@EnableEurekaClient
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes().route("car-service", r -> r.path("/cars").uri("lb://car-service"))
				.route("driver-service", r -> r.path("/drivers/**").uri("lb://driver-service")).build();
	}

	@Bean
	@LoadBalanced
	public WebClient.Builder loadBalancedWebClientBuilder() {
		return WebClient.builder();
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Car {
	private String name;
	private LocalDate releaseDate;
}

@RestController
class FaveCarsController {
	private final WebClient.Builder carClient;
	private ReactiveCircuitBreakerFactory cbFactory;

	public FaveCarsController(WebClient.Builder carClient, ReactiveCircuitBreakerFactory cbFactory) {
		this.carClient = carClient;
		this.cbFactory = cbFactory;
	}

	@GetMapping("/fave-cars")
	public Flux<Car> faveCars() {
		return carClient.build().get().uri("lb://car-service/cars").retrieve().bodyToFlux(Car.class).transform(it -> {
			ReactiveCircuitBreaker cb = cbFactory.create("slow");
			return cb.run(it, throwable -> Flux.empty());
		}).filter(p -> p.getName().equals("ID. BUZZ"));
	}
}