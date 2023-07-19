# CRUD using Java 17, Spring Boot, and Postgres

This repository contains a simple CRUD application developed in Java 17, using the Spring Boot framework and Postgres database. The CRUD allows performing basic operations of creating, reading, updating, and deleting products.

## Requirements

Make sure you have the following tools installed in your development environment:

- Java 17
- Spring Boot
- Postgres

## Database Configuration

Before running the application, you need to configure the Postgres database. Follow the steps below:

1. Create a database in Postgres.
2. Open the `application.properties` file located in `src/main/resources`.
3. Modify the properties `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` according to your database settings.

## Running the Application

To run the application, follow the steps below:

1. Clone this repository to your development environment.
2. Navigate to the project's root directory.
3. Run the following command to compile and execute the application:

```shell
./mvnw spring-boot:run
```

1. The application will be available at http://localhost:8080.
## Endpoints
The API has the following endpoints:

- GET /products: Returns all registered products.
- GET /products/{id}: Returns the details of a specific product based on the provided ID.
- POST /products: Registers a new product based on the data provided in the request body.
- PUT /products/{id}: Updates an existing product based on the provided ID and the data provided in the request body.
- DELETE /products/{id}: Deletes a product based on the provided ID.
