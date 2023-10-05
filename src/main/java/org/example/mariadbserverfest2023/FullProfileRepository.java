package org.example.mariadbserverfest2023;

import java.util.List;

import org.example.mariadbserverfest2023.MariadbServerFest2023Application.FullProfile;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FullProfileRepository {

	private final JdbcTemplate jdbcTemplate;

	public FullProfileRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<FullProfile> query() {
		var query = """
					select p.username, p.email, n.social_name, n.social_url
					from networks nw
					join json_table(nw.doc, '$' COLUMNS (
					    username VARCHAR(255) PATH '$.username',
					    nested path '$.social[*]' COLUMNS (
					    	social_name VARCHAR(255) PATH '$.name',
					    	social_url VARCHAR(255) PATH '$.url'
					    )
					  )
					) AS n
					join profiles p using(username)
					""";
		return this.jdbcTemplate.query(query, (rs, rowNum) -> new FullProfile(rs.getString("username"), rs.getString("email"), rs.getString("social_name"), rs.getString("social_url")));
	}

}
