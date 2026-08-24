# 🔐 AuthPortal

A Spring Boot authentication & authorization service built with **Java 21**, **Spring Security**, **JWT**, **Google OAuth2**, and **Email OTP verification**.

---

## 🚀 Features

- **JWT Authentication**: Stateless token generation, validation, and claim extraction using JJWT.
- **Google OAuth2 Login**: Seamless social login integration with automatic user provisioning and JWT issuance.
- **Email OTP Verification**: Secure 6-digit OTP generation with 10-minute expiry, max attempt lockout (5 attempts), and constant-time validation to verify user email addresses.
- **Security & Authorization**: Custom `JwtFilter`, BCrypt password hashing, stateless session policy, and JSON-based 401 Unauthorized handling.
- **Unified API Response & Error Handling**: Standardized `GenericResponse<T>` wrapper and centralized `@RestControllerAdvice` exception handler for validation and authentication errors.
- **Database Seeding**: Automated initial user seeding via `CommandLineRunner` using configurable seed credentials.
- **Automated Test Suite**: Unit, web layer (`MockMvc`), repository (`@DataJpaTest`), filter, and full end-to-end integration tests.

---

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot (Spring Web MVC, Spring Security, Spring Security OAuth2 Client, Spring Data JPA, Spring Mail)
- **Database**: H2 (In-Memory SQL database)
- **Token / Crypto**: JJWT, BCrypt, SecureRandom
- **Validation**: Jakarta Validation (`@Valid`, `@Email`, `@NotBlank`, `@Pattern`, `@Size`)
- **Testing**: JUnit 5, Mockito, MockMvc, Spring Security Test
- **Build Tool**: Maven Wrapper (`./mvnw`)

---

## 📡 API Endpoints

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/register` | Register a new user and automatically receive a JWT | No |
| `POST` | `/login` | Authenticate with username & password to receive a JWT | No |
| `GET` | `/oauth2/authorization/google` | Initiate Google OAuth2 login flow | No |
| `POST` | `/auth/email-otp/send`<br>`/auth/getEmailOtp` | Generate and send a 6-digit OTP to the specified email | Yes (or valid user email) |
| `POST` | `/auth/verifyEmail` | Verify submitted 6-digit OTP and mark email as verified | Yes |
| `GET` | `/hello` | Test endpoint returning a generic success response | Yes (Bearer Token) |
| `GET` | `/user/details` | Retrieve details for the authenticated user | Yes (Bearer Token) |
| `GET` | `/h2-console` | H2 Database In-Memory Web Console | No |

---

## ⚙️ Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+ (or use the included `./mvnw`)
- Google Cloud OAuth2 credentials (optional, for Google login)
- Gmail App Password (for OTP mail delivery)

### Configuration

The application loads environment properties from `.env.local` (or system environment variables). Create a `.env.local` file in the root directory:

```properties
# JWT Configuration
JWT_SECRET=your_base64_or_secure_random_secret_string_min_256_bits
JWT_EXPIRATION_MS=2592000000

# Google OAuth2 Credentials
GOOGLE_OAUTH_CLIENT_ID=your_google_client_id
GOOGLE_OAUTH_CLIENT_SECRET=your_google_client_secret

# Gmail SMTP Configuration
GMAIL_ID=your-email@gmail.com
GMAIL_APP_PASSWORD=your-gmail-app-password

# Initial Data Seeding
SEED_USERNAME=admin
SEED_EMAIL=admin@example.com
SEED_PASSWORD=your_secure_seed_password
```

### Build and Run

```bash
# Build the application
./mvnw clean package

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.

---

## 🧪 Testing

The repository contains unit tests across all layers and end-to-end integration tests:

```bash
# Run the complete test suite
./mvnw test
```
