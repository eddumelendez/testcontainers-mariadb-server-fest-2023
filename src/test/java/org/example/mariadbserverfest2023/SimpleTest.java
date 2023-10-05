package org.example.mariadbserverfest2023;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.MountableFile;

public class SimpleTest {

	@Test
	void test() throws SQLException {
		try (MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.1")
				.withCopyFileToContainer(MountableFile.forClasspathResource("db/migration/V1__profiles.sql"), "/docker-entrypoint-initdb.d/V1__profiles.sql")) {
			mariadb.start();
			Connection connection = DriverManager.getConnection(mariadb.getJdbcUrl(), mariadb.getUsername(), mariadb.getPassword());
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM profiles");
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();
			resultSet.next();
			Assertions.assertThat(resultSet.getString("username")).isEqualTo("eddumelendez");
		}
	}

}
