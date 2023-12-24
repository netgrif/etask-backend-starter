# eTask Backend Starter

[![License](https://img.shields.io/badge/license-NETGRIF%20Community%20License-green)](https://netgrif.com/license)
[![Java](https://img.shields.io/badge/Java-11-red)](https://openjdk.java.net/projects/jdk/11/)
[![NAE 6.3.1](https://img.shields.io/badge/NAE-6.3.1-0aa8ff)](https://github.com/netgrif/application-engine)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/netgrif/etask-backend-starter?sort=semver&display_name=tag)](https://github.com/netgrif/etask-backend-starter/releases)

eTask is a process-based application build
with [Netgrif Application Engine (NAE)](https://github.com/netgrif/application-engine).
eTask project is a quick way to start working with NAE and [Petriflow](https://github.com/netgrif/petriflow) processes
without a need to set up project from scratch.

eTask is implemented with [Spring Boot framework](https://spring.io/) so the developers can take full advantages of
concepts like Dependency Injection
and Inversion of Control to interact with NAE modules and service beans.

## Requirements

The application has some requirements for runtime environment. The following table is summary of requirements to
run and use the application:

| Name                                                   | Version | Description                                                     | Recommendation                                                         |
|--------------------------------------------------------|---------|-----------------------------------------------------------------|------------------------------------------------------------------------|
| [Java](https://openjdk.java.net/)                      | 11+     | Java Development Kit                                            | [OpenJDK 11](https://openjdk.java.net/install/)                        |
| [Redis](https://redis.io/)                             | 6+      | Key-value in-memory database used for user sessions and caching | [Redis 6.2.6](https://redis.io/download)                               |
| [MongoDB](https://www.mongodb.com/)                    | 6+      | Main document store database                                    | [MongoDB 6](https://www.mongodb.com/docs/v6.0/installation/)           |
| [Elasticsearch](https://www.elastic.co/elasticsearch/) | 7.17+   | Index database used for better application search               | [Elasticsearch 7.17.3](https://www.elastic.co/downloads/elasticsearch) |

If you are planning on developing docker container based solution you can use
included [docker-compose](docker-compose.yml)
configuration to run all necessary databases.

### Generate certificates

To enable all functionality of the application like Public view (anonymous access to cases and tasks) it is
recommended to generate RSA certificate pair (private and public) to sign and verify JWT tokens used by the application.

A quick way is to run following command:

```shell
 mkdir -p src/main/resources/certificates && cd src/main/resources/certificates && openssl genrsa -out keypair.pem 4096 && openssl rsa -in keypair.pem -pubout -out public.crt && openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in keypair.pem -out private.der && cd ../../../..
```

Certificates are generated into folder src/main/resources/certificates from where are loaded by default
You can customize the path for private RSA certificate with environment variable _JWT_SIGN_CERT_.

## Installation

eTask can be used as a starting project for your NAE application, or it can be run as is and deploy Petriflow
processes at runtime.

### Starter project

This project can be used as a base to your NAE application. Before you start coding please consider doing following
steps to personalize project:

1. Rename root java package
2. Edit maven project attributes in pom.xml, mainly groupId and artifactId
3. Generate security certificates for token encryption

As it is Java [Maven](https://maven.apache.org/) project it is assumed that you have some experience with Java
programming language.

### Run as a jar

eTask is distributed as a standalone executable jar archive with every release. You can download it as a [release
artifact](https://github.com/netgrif/etask-backend-starter/releases/latest), extract it from the zip, generate
certificates, and run it.

```shell
wget -O etask.zip https://github.com/netgrif/etask-backend-starter/releases/latest
unzip etask.zip
cd etask
cd src/main/resources/certificates && openssl genrsa -out keypair.pem 4096 && openssl rsa -in keypair.pem -pubout -out public.crt && openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in keypair.pem -out private.der && cd ../../../..
java -jar app-exec.jar
```

The application listens on port 8080 and is connecting to locally run databases. To configure it use environment
variables.

### Run as a container

eTask is also published as [Docker image](https://hub.docker.com/r/netgrif/etask-backend) available via Docker hub. You
can run it directly with docker:

```shell
docker pull netgrif/etask-backend:latest
docker run -p 8080:8080 netgrif/etask-backend:latest
```

or with docker-compose file:

```yaml
version: "3.3"
services:
  mongo:
    image: mongo:6
    ports:
      - "27017:27017"
    networks:
      - etask-network
  elastic:
    image: elasticsearch:7.17.4
    environment:
      - cluster.name=elasticsearch
      - discovery.type=single-node
      - http.host=0.0.0.0
      - xpack.security.enabled=false
      - transport.host=0.0.0.0
    ports:
      - "9200:9200"
      - "9300:9300"
    networks:
      - etask-network
  redis:
    image: redis:6
    ports:
      - "6379:6379"
    networks:
      - etask-network
  etask:
    image: netgrif/etask-backend:latest
    ports:
      - "8080:8080"
    networks:
      - etask-network
    depends_on:
      mongo:
        condition: service_healthy
      elastic:
        condition: service_healthy
      redis:
        condition: service_healthy
networks:
  etask-network:
    driver: bridge
```

## Configuration

eTask can be configured with environment variables or with application.properties file if run locally from the
repository.
By default, the application expects that databases are run locally, for deployment you are encouraged to change it.
The application can be configured with these variables:

| Variable                     | Type                | Default                                                                                                                               | Description                                                                                                                                                                                                                              |
|------------------------------|---------------------|---------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MAX_UPLOAD_FILE_SIZE         | String              | 50MB                                                                                                                                  | Maximum size of uploaded file.                                                                                                                                                                                                           |
| MONGO_URI                    | String              | mongodb://localhost:27017                                                                                                             | URI for MongoDB. Can be mongodb+srv connection scheme. If MongoDB requires authentication you must supply it the URI. <br/>For more info see [official documentation](https://www.mongodb.com/docs/manual/reference/connection-string/). |
| DATABASE_NAME                | String              | etask                                                                                                                                 | Name od the database. The name is used for MongoDB, Elasticsearch, and Redis to keep consistent naming across all databases.                                                                                                             |
| ELASTIC_URL                  | String              | localhost                                                                                                                             | Hostname for Elasticsearch without protocol and port.                                                                                                                                                                                    |
| ELASTIC_PORT                 | Number              | 9300                                                                                                                                  | TCP communication port.                                                                                                                                                                                                                  |
| ELASTIC_SEARCHPORT           | Number              | 9200                                                                                                                                  | REST API port for search requests.                                                                                                                                                                                                       |
| ELASTIC_EXECUTORS            | Number              | 500                                                                                                                                   | Number of threads for execution of indexing of app resources (like tasks and cases).                                                                                                                                                     |
| MAIL_FROM                    | String              | etask@example.com                                                                                                                     | Email from which is the application sending emails.                                                                                                                                                                                      |
| MAIL_DOMAIN_APP_PORT         | Number              | 4200                                                                                                                                  | Port of the application that is used to resolve redirect links in the emails. If it is not imply, leave blank.                                                                                                                           |
| MAIL_DOMAIN_APP              | String              | localhost                                                                                                                             | Hostname of the application that is used to resolve redirect links in the emails.                                                                                                                                                        |
| MAIL_SSL_APP_ENABLED         | Boolean             | false                                                                                                                                 | Value to determine protocol to use for redirect link in the emails. True = HTTPS, False = HTTP                                                                                                                                           |
| MAIL_HOST                    | String              |                                                                                                                                       | Url to email server                                                                                                                                                                                                                      |
| MAIL_USERNAME                | String              |                                                                                                                                       | Username for authentication to email server. If it is not required leaved it blank.                                                                                                                                                      |
| MAIL_PASSWORD                | String              |                                                                                                                                       | Password for authentication to email server. If it is not required leaved it blank.                                                                                                                                                      |
| MAIL_PORT                    | Number              | 25                                                                                                                                    | Communication port for email server.                                                                                                                                                                                                     |
| MAIL_DEBUG_ENABLED           | Boolean             | false                                                                                                                                 | Enable/Disable debug logging for email communication.                                                                                                                                                                                    |
| MAIL_AUTH_ENABLED            | Boolean             | false                                                                                                                                 | Enable/Disable authentication for connecting to email server.                                                                                                                                                                            |
| MAIL_TLS_ENABLED             | Boolean             | true                                                                                                                                  | Enable/Disable TLS encryption for communication with email server.                                                                                                                                                                       |
| MAIL_PROTOCOL                | String              | smtp                                                                                                                                  | Protocol used for communicating with email server.                                                                                                                                                                                       |
| REDIS_HOST                   | String              | localhost                                                                                                                             | Hostname for Redis database without port and protocol.                                                                                                                                                                                   |
| REDIS_PORT                   | Number              | 6379                                                                                                                                  | Port for Redis.                                                                                                                                                                                                                          |
| DATABASE_ENCRYPTION_PASSWORD | String              | password                                                                                                                              | Secret to use in encryption algorithm to encrypt process instance values.                                                                                                                                                                |
| OPEN_REGISTRATION            | Boolean             | true                                                                                                                                  | Enable/Disable self registration of new users.                                                                                                                                                                                           |
| REGISTRATION_TOKEN_VALIDITY  | Number              | 3                                                                                                                                     | Number of days for registration verification token is valid.                                                                                                                                                                             |
| MINIMAL_PASSWORD_LENGTH      | Number              | 8                                                                                                                                     | Minimal length of user's password. Length is determine with stripped of blank characters.                                                                                                                                                |
| CSRF_ENABLED                 | Boolean             | false                                                                                                                                 | Enable/Disable CRSF protection. It is recommended if frontend and backend of the app is accessed from the same domain.                                                                                                                   |
| LIMITS_LOGIN_ATTEMPTS        | Number              | 50                                                                                                                                    | Number of login attempts before blocking the credentials for authenticating.                                                                                                                                                             |
| LIMITS_LOGIN_BAN             | Number              | 1                                                                                                                                     | Number of time units for blocking to login into the app. Default time unit are days.                                                                                                                                                     |
| JWT_VALIDITY                 | Number              | 900000                                                                                                                                | Number of seconds for JWT token to expire. Default value equal cca 10 days.                                                                                                                                                              |
| JWT_SIGN_ALGORITHM           | String              | RSA                                                                                                                                   | Algorithm to use to sign JWT tokens.                                                                                                                                                                                                     |
| JWT_SIGN_CERT                | File resource       | file:src/main/resources/certificates/private.der                                                                                      | File path to private key for signing JWT tokens.                                                                                                                                                                                         |
| NO_AUTH_PATHS                | List of paths       | /api/auth/signup,/api/auth/token/verify,<br/>/api/auth/reset,/api/auth/recover,/api/public/**,<br/>/v3/api-docs/public,/manage/health | List of paths to be available even without authentication.                                                                                                                                                                               |
| POSTAL_CODES_ENABLED         | Boolean             | false                                                                                                                                 | Enable/Disable import of list of postal codes.                                                                                                                                                                                           |
| POSTAL_CODES_FILE            | String              | postal_codes.csv                                                                                                                      | Name of file in folder `src/main/resources` that contains postal codes to import.                                                                                                                                                        |
| ADMIN_PASSWORD               | String              | password                                                                                                                              | Password for super admin account with login super@netgrif.com.                                                                                                                                                                           |
| STORAGE_PATH                 | String              | storage                                                                                                                               | Path to folder that will be used as the application file storage. For larger deployments it is recommended to point to network attached drive.                                                                                           |
| PDF_DEFAULT_NAME             | String              | generated_pdf.pdf                                                                                                                     | Default name of a generated pdf file.                                                                                                                                                                                                    |
| AUTH_PROVIDERS               | List of class names | NetgrifBasicAuthenticationProvider                                                                                                    | List of names of authentication providers classes to use for verifying authentication of application's users.                                                                                                                            |
| LDAP_ENABLED                 | Boolean             | false                                                                                                                                 | Enable/Disable LDAP connector.                                                                                                                                                                                                           |
| LDAP_URL                     | String              | ldap://localhost:389                                                                                                                  | Url for LDAP server to connect. Url must include protocol (ldap or ldaps) and port (i.e. 389) of the LDAP server.                                                                                                                        |
| LDAP_USERNAME                | String              |                                                                                                                                       | Username of technical account that is used for communication with LDAP server.                                                                                                                                                           |
| LDAP_PASSWORD                | String              |                                                                                                                                       | Password of technical account that si used for communication with LDAP server.                                                                                                                                                           |
| LDAP_BASE                    | String              |                                                                                                                                       | Path in LDAP tree for the search base of users.                                                                                                                                                                                          |
| IMPERSONATION_ENABLED        | Boolean             | true                                                                                                                                  | Enable/Disable user impersonation functionality.                                                                                                                                                                                         |
| GROUPS_DEFAULT_ENABLED       | Boolean             | true                                                                                                                                  | Enable/Disable user groups functionality.                                                                                                                                                                                                |
| GROUPS_SYSTEM_ENABLED        | Boolean             | true                                                                                                                                  | Enable/Disable creation of default system user group.                                                                                                                                                                                    |
| ACTIONS_RUNNER_CACHE_SIZE    | Number              | 500                                                                                                                                   | Size of cache for process actions.                                                                                                                                                                                                       |
| FUNCTIONS_RUNNER_CACHE_SIZE  | Number              | 500                                                                                                                                   | Size of cache for process functions. For both function scopes are created separate cache of the same size.                                                                                                                               |
| EXPRESSION_RUNNER_CACHE_SIZE | Number              | 500                                                                                                                                   | Size of cache for process expressions.                                                                                                                                                                                                   |
| ACTIONS_EXTRA_IMPORTS        | List of classes     | com.netgrif.application.engine.petrinet.domain.I18nString                                                                             | Java classes to be automatically imported to process actions. Classes must be specified with package.                                                                                                                                    |
| MAIL_HEALTH_ENABLED          | Boolean             | false                                                                                                                                 | Enable/Disable health check for email server connection.                                                                                                                                                                                 |

### User creation

eTask offers a quick way to create local users at application startup. It is a convenient way to create admin or
technical accounts to access the application without need of writing a code or registration process.
Setting property `etask.users.<user>.<user property>`, a user is created at startup. If user with the specified
email already exists it is not created again. `<user>` is a unique identifier of a user properties. Its name is not used
in user creation process. Options for `<user properties>` are defined in tables below:

| Property                       | Type            | Description                                                                                                                                                                        |
|--------------------------------|-----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| etask.users.<user>.email       | String          | Email of the user. Must be valid email address. Must be unique across whole system.                                                                                                |
| etask.users.<user>.name        | String          | Name of the user. If it consists of more words, the word before first space character is consider first name and other as a surname.                                               |
| etask.users.<user>.password    | String          | Password of the user. Password has to fulfill validation defined in property MINIMAL_PASSWORD_LENGTH.                                                                              |
| etask.users.<user>.authorities | List of strings | List of authorities that are assigned to user. System has already defined these authorities: ROLE_USER, ROLE_ADMIN. If a unknown authority is defined it is automatically created. |

If you cloned the repository you can simply add new properties
to [application.properties](src/main/resources/application.properties) file.

If you are running the application as a jar archive you can simply specify properties as the application arguments like
`java -jar app-exec.jar --etask.users.<user>.email="some@email.com" ...`.

If you are running the application as a container you can provide properties as environment variables.

## Frontend app

To run complete eTask application there is also [eTask frontend](https://github.com/netgrif/etask-frontend-starter) web
app. eTask frontend is an Angular SPA application that can be run on any web server or as a docker container.
You can use it as a starting point for your NAE application or run it as is.

## Further help

If you need any help with the project you can write us a help request as
an [issue](https://github.com/netgrif/etask-backend-starter/issues) or engage in
a [discussion](https://github.com/netgrif/etask-backend-starter/discussions) in
the repository.
