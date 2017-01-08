package com.example;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.hateoas.Resources;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@EnableResourceServer
@EnableBinding(ReservationChannels.class) // For RabbitMQ
@IntegrationComponentScan //RabbitMQ
@EnableFeignClients //Client side load balancing.
@EnableZuulProxy //It will route the requests to the actual service.
@EnableCircuitBreaker
@EnableDiscoveryClient
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


}

@FeignClient("reservation-service")
interface ReservationReader{

	@RequestMapping(method=RequestMethod.GET, value="reservations")
	Resources<Reservation> read();
	
	@RequestMapping(method=RequestMethod.GET, value="message")
	String message();
}

@MessagingGateway
interface ReservationWriter{
	
	@Gateway(requestChannel="output")
	void write(String reservationName);
}

class Reservation{
	
	private String reservationName;

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}
}

@RestController
@RequestMapping("/reservations")
class ReservationApiGateway{

	private final ReservationReader reservationReader;
	private final ReservationWriter reservationWriter;
	
	@Autowired
	public ReservationApiGateway(ReservationReader reservationReader,
			ReservationWriter reservationWriter) {
		this.reservationReader = reservationReader;
		this.reservationWriter = reservationWriter;
	}

	@GetMapping("/names")
	public Collection<String> names(){
		return this.reservationReader.read().getContent().stream().map(
				r -> r.getReservationName()).collect(Collectors.toList());
	}
	
	@PostMapping
	public void write(@RequestBody Reservation reservation){
		this.reservationWriter.write(reservation.getReservationName());
	}
	
	
	public String fallback(){
		return "fallback";
	}
	
	@HystrixCommand(fallbackMethod="fallback")
	@RequestMapping(method=RequestMethod.GET, value="/message")
	public String message(){
		return this.reservationReader.message();
	}
	
	
}


interface ReservationChannels{
	
	@Output
	MessageChannel output();
}
