package com.example.driverservice;

import java.time.LocalDate;
import java.util.List;

import feign.Headers;
import feign.RequestLine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactivefeign.webclient.WebReactiveFeign;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@EnableEurekaClient
@SpringBootApplication
public class DriverServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DriverServiceApplication.class, args);
	}

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Car {
	private String name;
	private LocalDate releaseDate;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Driver {
	private String name;
	private List<Car> cars;

	public Driver(String name) {
		this.name = name;
	}
}

@RestController
class DriverController {
	private final WebClient.Builder carClient;

	public DriverController(WebClient.Builder carClient) {
		this.carClient = carClient;
	}

	/** */
	@GetMapping("/drivers")
	public Mono<Driver> getDriver() {
		/* Create instance of your API */
		CarServiceApi client =
			WebReactiveFeign  //WebClient based reactive feign
				//JettyReactiveFeign //Jetty http client based
				//Java11ReactiveFeign //Java 11 http client based
				.<CarServiceApi>builder()
				.target(CarServiceApi.class, "http://localhost:8080");

		/*
		Flux<Car> carFlux = carClient.build().get().uri("http://localhost:8080/cars").retrieve().bodyToFlux(Car.class)
				.filter(p -> p.getName().equals("ID. BUZZ"));
		*/

		Flux<Car> carFlux = client.getCars().filter(p -> p.getName().equals("ID. BUZZ"));

		Mono<Driver> driver = Mono.just(new Driver("John"));

		return driver.zipWith(carFlux.collectList()).map(r -> new Driver(r.getT1().getName(), r.getT2()));
	}
	/* */

	// @GetMapping("/drivers")
	public Flux<Car> getCars() {
		return carClient.build().get().uri("http://localhost:8080/cars").retrieve().bodyToFlux(Car.class)
				.filter(p -> p.getName().equals("ID. BUZZ"));
	}
}

@Component
@Headers({ "Accept: application/json" })
interface CarServiceApi {
	@RequestLine("GET /cars")
	Flux<Car> getCars();
}