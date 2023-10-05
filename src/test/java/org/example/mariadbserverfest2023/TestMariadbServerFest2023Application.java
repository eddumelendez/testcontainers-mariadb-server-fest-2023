package org.example.mariadbserverfest2023;

import java.io.IOException;
import java.util.List;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.example.mariadbserverfest2023.MariadbServerFest2023Application.Networks;
import org.example.mariadbserverfest2023.MariadbServerFest2023Application.Social;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;

@TestConfiguration(proxyBeanMethods = false)
public class TestMariadbServerFest2023Application {

	Network network = Network.newNetwork();

	@Bean
	@ServiceConnection
	MariaDBContainer<?> mariaDbContainer() {
		return new MariaDBContainer<>(DockerImageName.parse("mariadb:11.1"))
				.withCopyFileToContainer(MountableFile.forClasspathResource("add_maxscale_user.sql"), "/docker-entrypoint-initdb.d/add_maxscale_user.sql")
				.withNetwork(network)
				.withNetworkAliases("mdb");
	}

	@Bean
	GenericContainer<?> maxscale(DynamicPropertyRegistry registry) {
		GenericContainer<?> maxscale = new GenericContainer("mariadb/maxscale:23.08")
		{
			@Override
			protected void containerIsStarted(InspectContainerResponse containerInfo) {
				try {
					execInContainer("maxscale-restart");
				}  catch (IOException | InterruptedException e) {
					throw new RuntimeException("Error during restart", e);
				}
			}
		}
				.withExposedPorts(3306, 8989, 17017)
				.waitingFor(Wait.forLogMessage(".*started.*", 1))
				.withNetwork(network)
				.withCopyFileToContainer(MountableFile.forClasspathResource("maxscale.cnf"), "/etc/maxscale.cnf");
		registry.add("spring.data.mongodb.host", maxscale::getHost);
		registry.add("spring.data.mongodb.port", () -> maxscale.getMappedPort(17017));
		return maxscale;
	}

	public static void main(String[] args) {
		SpringApplication.from(MariadbServerFest2023Application::main).with(TestMariadbServerFest2023Application.class).run(args);
	}

	@Bean
	CommandLineRunner socialNetworks(MongoTemplate mongoTemplate, FullProfileRepository repository) {
		return args -> {
			mongoTemplate.save(new Networks("eddumelendez",
					List.of(new Social("github", "https://github.com/eddumelendez"),
							new Social("twitter", "https://twitter.com/eddumelendez"))));

			repository.query().forEach(System.out::println);
		};
	}

}
