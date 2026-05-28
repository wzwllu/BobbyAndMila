# PMP - Project Management System

A Spring Boot-based project management system built with Java.

## Tech Stack

- **Java 17**
- **Spring Boot 2.7.18**
- **Spring Data JPA**
- **H2 Database** (in-memory file-based storage)
- **Maven** (build tool)

## Project Structure

```
pmp/
├── src/main/java/com/pmp/    # Source code
├── src/main/resources/       # Configuration files
├── data/                     # Database files (H2)
├── docs/                     # Documentation
└── pom.xml                   # Maven dependencies
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Access H2 Console

Once the application is running, you can access the H2 database console:

- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:./data/pmp`
- **Username**: `sa`
- **Password**: (leave empty)

## Configuration

Application configuration is in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:./data/pmp
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
```

## Development Notes

- The H2 database file is stored in `./data/pmp.mv.db`
- DDL auto-update is enabled for development (consider disabling for production)
- The project uses Spring Security for authentication

## Troubleshooting

### Maven Build Issues

If you encounter Maven dependency resolution errors (403 status), check:
- Network connectivity to your Maven repository
- Repository authentication settings in `~/.m2/settings.xml`
- Alternative Maven mirrors may be required

### Database Access

If H2 console is not accessible, verify:
- Application is running on port 8080
- H2 console is enabled in `application.yml`
- Correct JDBC URL is used in H2 console settings

## License

This project is proprietary and confidential.
