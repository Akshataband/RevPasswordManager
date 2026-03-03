# RevPasswordManager Backend
Secure Spring Boot REST API for RevPasswordManager with JWT authentication, TOTP-based 2FA, encrypted password vault management, and backup support.

Alternative shorter version:

Spring Boot REST API with JWT security, 2FA, encrypted password storage, and vault management features.

---

## Tech Stack

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate
- MySQL
- BCrypt Password Encoder
- Maven

---

## Features

### Authentication

- User registration
- Login with JWT token
- Password hashing using BCrypt
- Account lock after multiple failed attempts
- Token blacklist on logout

### Two-Factor Authentication (2FA)

- TOTP-based verification
- QR code generation
- OTP validation
- Enable and disable 2FA

### Password Vault Management

- Add password entry
- Update password entry
- Delete password entry
- Retrieve password by ID
- Retrieve all passwords for a user
- Search passwords
- Filter and pagination support
- Mark and unmark favorite passwords
- Master password verification before viewing sensitive data

### Security Features

- Stateless JWT authentication
- Custom JwtFilter
- SecurityContext-based authorization
- Token blacklist validation
- Account locking mechanism
- Security question verification
- Password strength validation

### Encrypted Backup

- Export vault as encrypted file
- Import encrypted backup file
- Master password required for export and import

---

## Architecture

The backend follows a layered architecture:

Controller → Service → Repository → Database

Security Flow:

Client → JwtFilter → SecurityContext → Controller

Authentication is stateless and fully JWT-based.

---

## Project Structure

```
src/main/java/com/revpasswordmanager
 ├── controller
 ├── service
 ├── repository
 ├── entity
 ├── dto
 ├── security
 ├── exception
 └── util
```

### Layer Responsibilities

- controller – Handles HTTP requests and responses
- service – Contains business logic and validation
- repository – Data access layer using Spring Data JPA
- entity – JPA entity definitions
- dto – Request and response models
- security – JWT filter and security configuration
- exception – Global exception handling
- util – Helper and utility classes

---

## Database Configuration

The application uses MySQL.

Update `application.properties`:

```
spring.datasource.url=jdbc:mysql://localhost:3306/revpasswordmanager
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

Make sure the database exists before starting the application.

---

## Running the Application

1. Clone the repository  
   `git clone <backend-repo-url>`

2. Navigate to project directory  
   `cd backend`

3. Build the project  
   `mvn clean install`

4. Run the application  
   `mvn spring-boot:run`

Or run the jar file:

```
java -jar target/revpasswordmanager-0.0.1-SNAPSHOT.jar
```

Application runs at:  
http://localhost:8080

---

## API Base URL

```
http://localhost:8080/api
```

---

## Main Endpoint Groups

### Authentication

- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/logout

### Two-Factor Authentication

- POST /api/2fa/enable
- POST /api/2fa/verify
- POST /api/2fa/disable

### Password Vault

- POST /api/passwords
- GET /api/passwords
- GET /api/passwords/{id}
- PUT /api/passwords/{id}
- DELETE /api/passwords/{id}
- GET /api/passwords/search
- GET /api/passwords/favorites

### Backup

- POST /api/backup/export
- POST /api/backup/import

---

## Security Implementation

- Stateless JWT authentication
- Custom JWT filter for request validation
- BCrypt password hashing
- Token blacklist stored in database
- Role-based authorization ready for extension
- Account lock after failed login attempts

---

## Future Improvements

- Refresh token implementation
- HttpOnly cookie-based token storage
- Redis-based token blacklist
- Rate limiting
- Role-based access control
- Docker containerization
- CI/CD pipeline integration

---

## Learning Outcomes

This project demonstrates:

- Secure authentication design
- Layered backend architecture
- Stateless JWT security implementation
- Full-stack integration with Angular
- Advanced password management features
