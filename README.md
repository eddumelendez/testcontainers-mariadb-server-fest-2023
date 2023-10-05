# MariaDB Server Fest 2023

Demonstrate how to use MariaDB with [Testcontainers](https://testcontainers.com/). 
[MariaDB](https://hub.docker.com/_/mariadb)server and [Maxscale](https://hub.docker.com/r/mariadb/maxscale)
are started via Testcontainers for testing and local development.

## Prerequisites

* Java 17
* Docker
* Testcontainers Desktop

## Running the tests

```bash
./mvnw verify
```

## Running the application

```bash
./mvnw spring-boot:test-run
```

Once the application starts, you can see the following logs:

```
FullProfile[username=eddumelendez, email=eddu@example.io, socialNetwork=github, socialNetworkUrl=https://github.com/eddumelendez]
FullProfile[username=eddumelendez, email=eddu@example.io, socialNetwork=twitter, socialNetworkUrl=https://twitter.com/eddumelendez]
```

### Access to MariaDB and Maxscale

First, install [Testcontainers Desktop](https://testcontainers.com/desktop/), go to 
`Services > Open Config Localtion` and create the following files

`maxscale.toml`

```toml
ports = [
  {local-port = 8989, container-port = 8989},
  {local-port = 17017, container-port = 17017},
]

selector.image-names = ["mariadb/maxscale"]
```

`mariadb.toml`

```toml
ports = [
  {local-port = 3306, container-port = 3306},
]

selector.image-names = ["mariadb"]
```

Now, you can access to MariaDB using default port (3306)

* Database: `test`
* User: `test`
* Password: `test`

Open `http://localhost:8989` to access to Maxscale web interface.

You can also use your MongoDB client using port `17017`.
