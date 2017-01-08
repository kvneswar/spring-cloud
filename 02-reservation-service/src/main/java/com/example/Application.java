package com.example;

import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableBinding(ReservationChannels.class)
@EnableDiscoveryClient
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

@RestController
@RefreshScope
class MessageRestController{
	
	private final String value;
	
	@Autowired
	public MessageRestController(@Value("${message}") String value){
		this.value = value;
	}
	
	@GetMapping("/message")
	public String getMessage(){
		System.out.println("Got the Request !!!");
		return this.value;
	}
	
}

@Component
class SampleDataCLR implements CommandLineRunner{

	@Autowired
	private ReservationRepository reservationRepository;
	
	@Override
	public void run(String... arg0) throws Exception {
		
		Stream.of("Eswar", "Lakshmi")
			.forEach(name -> reservationRepository.save(new Reservation(name)));;
		
		reservationRepository.findAll().forEach(System.out::println);
	}
	
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long>{
	
}

@Entity
class Reservation{
	
	@Id
	@GeneratedValue
	private Long id;
	private String reservationName;
	
	public Reservation(String reservationName) {
		super();
		this.reservationName = reservationName;
	}
	
	public Reservation() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}

	@Override
	public String toString() {
		return "Reservation [id=" + id + ", reservationName=" + reservationName + "]";
	}

}


interface ReservationChannels{
	
	@Input
	SubscribableChannel input();
}

@MessageEndpoint
class ReservationProcessor{
	
	private final ReservationRepository reservationRepository;
	
	@Autowired
	public ReservationProcessor(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@ServiceActivator(inputChannel="input")
	public void onNewReservation(String reservationName){
		this.reservationRepository.save(new Reservation(reservationName));
	}
}
