package org.example.mariadbserverfest2023;

import java.io.IOException;
import java.util.List;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.assertj.core.groups.Tuple;
import org.example.mariadbserverfest2023.MariadbServerFest2023Application.FullProfile;
import org.example.mariadbserverfest2023.MariadbServerFest2023Application.Networks;
import org.example.mariadbserverfest2023.MariadbServerFest2023Application.Social;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class MariadbServerFest2023ApplicationTests {

	static Network network = Network.newNetwork();

	@Container
	@ServiceConnection
	static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.1")
				.withCopyFileToContainer(MountableFile.forClasspathResource("add_maxscale_user.sql"), "/docker-entrypoint-initdb.d/add_maxscale_user.sql")
				.withNetwork(network)
				.withNetworkAliases("mdb");

	@Container
	static GenericContainer<?> maxscale = new GenericContainer("mariadb/maxscale:23.08")
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
				.withExposedPorts(17017)
				.waitingFor(Wait.forLogMessage(".*started.*", 1))
				.withNetwork(network)
				.withCopyFileToContainer(MountableFile.forClasspathResource("maxscale.cnf"), "/etc/maxscale.cnf");

	@DynamicPropertySource
	static void mongoProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.host", maxscale::getHost);
		registry.add("spring.data.mongodb.port", () -> maxscale.getMappedPort(17017));
	}

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private FullProfileRepository fullProfileRepository;

	@Test
	void contextLoads() {
		this.mongoTemplate.save(new Networks("eddumelendez",
				List.of(new Social("github", "https://github.com/eddumelendez"),
						new Social("twitter", "https://twitter.com/eddumelendez"))));

		List<FullProfile> result = this.fullProfileRepository.query();
		assertThat(result).hasSize(2);
		assertThat(result).extracting("socialNetwork", "socialNetworkUrl")
				.containsExactlyInAnyOrder(
						Tuple.tuple("github", "https://github.com/eddumelendez"),
						Tuple.tuple("twitter", "https://twitter.com/eddumelendez"));
	}

}
