RevPasswordManager Backend

A secure Spring Boot REST API for managing encrypted password vaults.
This project demonstrates a clean layered architecture using Controller, Service, and Repository patterns with Spring Security, JWT authentication, JPA/Hibernate, and MySQL.

Tech Stack
Java 21
Spring Boot
Spring Web MVC
Spring Security
JWT Authentication
Spring Data JPA
Hibernate
MySQL
BCrypt Password Encoder
Maven

Features

Authentication
User registration
Login with JWT
Password hashing using BCrypt
Account lock after multiple failed login attempts
Token blacklist on logout

Two-Factor Authentication (2FA)
TOTP-based verification
QR code generation
OTP validation
Enable and disable 2FA

Vault Management
Add password entry
Update password entry
Delete password entry
Get password by ID
Get all passwords for a user
Search passwords
Filter and pagination support
Mark and unmark favorite passwords
Master password verification before viewing sensitive data

Security Features
Stateless JWT authentication
Custom JwtFilter
SecurityContext-based authorization
Token blacklist validation
Account locking mechanism
Security question verification
Password strength validation

Encrypted Backup
Export vault as encrypted file
Import encrypted backup
Master password required for export and import

Project Structure

controller – Handles HTTP requests and returns responses
service – Contains business logic and validation
repository – Data access layer using Spring Data JPA
entity – JPA entity definitions
dto – Data transfer objects for request and response
security – JWT filter, security configuration, authentication logic
exception – Global exception handling and custom exceptions
util – Utility classes (encryption, OTP, helpers)

The application follows a clean separation of concerns to keep the code secure, maintainable, and scalable.

Database Configuration

The application uses MySQL. Update application.properties with your local database credentials.

Example:

spring.datasource.url=jdbc:mysql://localhost:3306/revpasswordmanager
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update

Make sure the database exists before starting the application.

Running the Application

Clone the project and run:

mvn spring-boot:run

Or build and run:

mvn clean install
java -jar target/revpasswordmanager-0.0.1-SNAPSHOT.jar

The application runs on:

http://localhost:8080

API Base URL

/api

Main Endpoint Groups

Authentication
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout

Two-Factor Authentication
POST /api/2fa/enable
POST /api/2fa/verify
POST /api/2fa/disable

Vault
POST /api/passwords
GET /api/passwords
GET /api/passwords/{id}
PUT /api/passwords/{id}
DELETE /api/passwords/{id}
GET /api/passwords/search
GET /api/passwords/favorites

Backup
POST /api/backup/export
POST /api/backup/import

Sample Request Body (Register)

{
"username": "akshata",
"email": "akshata@example.com
",
"password": "StrongPassword@123"
}

Notes

Passwords are hashed using BCrypt before storing.
JWT authentication is stateless and validated using a custom filter.
Blacklisted tokens are stored in the database and validated on each request.
Tables are auto-generated using ddl-auto=update.
Relationships are mapped using JPA annotations.

This backend can be extended further with refresh tokens, role-based authorization, Redis-based blacklist, rate limiting, Docker support, and CI/CD integration.
