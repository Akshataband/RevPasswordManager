📘 RevPasswordManager – README
🔐 RevPasswordManager

RevPasswordManager is a secure full-stack password management system built using Spring Boot (Backend) and Angular (Frontend).
It provides encrypted password storage, JWT authentication, 2FA security, encrypted backup, and security audit features.

🚀 Tech Stack
Backend

Java 21

Spring Boot

Spring Security

JWT Authentication

JPA / Hibernate

MySQL

BCrypt Password Encoder

Frontend

Angular (Standalone Components)

TypeScript

Angular Router

HTTP Interceptors

FormsModule / ReactiveForms

🏗 Architecture

The backend follows a layered architecture:

Controller → Service → Repository → Database

Security layer:

Client → JwtFilter → SecurityContext → Controller

Authentication is stateless (JWT-based).

🔑 Features
👤 Authentication

User Registration

Login with JWT

Account lock after failed attempts

Password hashing using BCrypt

🔐 Two-Factor Authentication (2FA)

TOTP-based authentication

QR code generation

OTP verification

Enable / Disable 2FA

🔒 Vault Management

Add Password

Edit Password

Delete Password

View Password (requires master password)

Favorite passwords

Search, Filter, Pagination

🛡 Security Features

Master password verification

JWT authentication

Token blacklist on logout

Account locking mechanism

Security questions

Password strength analysis

💾 Encrypted Backup

Export vault as encrypted .enc file

Import encrypted backup

Master password required for export/import

🔐 JWT Token Flow

User logs in.

Backend validates credentials.

JWT token is generated.

Token stored in browser localStorage.

Angular interceptor attaches token to every request.

JwtFilter validates token before controller execution.

Logout adds token to blacklist table.

🗂 Database Design

Main entities:

users

password_entries

security_question

verification_code

blacklisted_token

Relationships:

One user → Many password entries

One user → Many security questions

One user → Many verification codes

⚙️ How to Run the Project
Backend
git clone <backend-repo>
cd backend
mvn clean install
mvn spring-boot:run

Update application.properties with your MySQL credentials.

Frontend
cd frontend
npm install
ng serve

App runs at:

http://localhost:4200

Backend runs at:

http://localhost:8080
🔒 Security Implementation Details

Stateless JWT authentication

BCrypt password hashing

Custom JwtFilter

Blacklisted token validation

TOTP-based 2FA

Encrypted backup export

📈 Future Improvements

Refresh token implementation

HttpOnly cookie-based token storage

Role-based authorization

Rate limiting

Redis token blacklist

Docker containerization

CI/CD pipeline

🎯 Learning Outcomes

This project demonstrates:

Secure authentication design

Full-stack integration

Layered backend architecture

Stateless security implementation

Advanced password management features

