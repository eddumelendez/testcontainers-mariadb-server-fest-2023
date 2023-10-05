package org.example.mariadbserverfest2023;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MariadbServerFest2023Application {

	public static void main(String[] args) {
		SpringApplication.run(MariadbServerFest2023Application.class, args);
	}


	record FullProfile(String username, String email, String socialNetwork, String socialNetworkUrl) {

	}

	record Networks(String username, List<Social> social) {

	}

	record Social(String name, String url) {

	}

}
