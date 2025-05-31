# URL Shortener Service

A full-featured, secure, and scalable URL Shortener backend built using **Spring Boot**. This application supports custom aliases, expiry, analytics, JWT-based authentication, and role-based authorization.

---

## ✨ Features

### 🔹 Core

* Generate shortened URLs for long links
* Support for **custom aliases**
* Optional **expiry timestamps**
* Redirects to original URL via GET /{shortCode}
* Redis caching for faster shortCode-to-originalUrl redirection

### 📊 Analytics

* Tracks **clicks per shortened URL**
* Captures and stores:

    * Browser
    * Operating System
    * Device Type
    * IP Address
    * Timestamp
    * Referrer
* Filtering and pagination supported
* Grouping clicks by **day** or **custom date range**
* View **top-clicked URLs** (admin-only)

### 🔒 Authentication & Authorization

* JWT-based authentication (stateless)
* Role-based access control (**ADMIN**, **USER**)
* Signup & Login with validation

### ⚙️ Technical

* Stateless, secure architecture
* Passwords hashed using BCrypt
* Custom exception handling
* Flyway-based schema migrations
* Redis integration for performance

---

## 🚀 Tech Stack

| Layer      | Tech                                 |
| ---------- | ------------------------------------ |
| Backend    | Java 17, Spring Boot 3.x             |
| Security   | Spring Security, JWT                 |
| Database   | MySQL, JPA/Hibernate                 |
| Migrations | Flyway                               |
| Caching    | Redis                                |
| Testing    | MockMvc, Mockito |

---

## 🔢 API Endpoints

### Auth APIs

* `POST /auth/signup`
* `POST /auth/login`

### URL Shortening APIs

* `POST /shorten` — shorten a URL *(auth required)*
* `GET /{shortCode}` — redirect to original URL
* `GET /shorten/{shortCode}` — get details *(auth required)*
* `DELETE /shorten/{shortCode}` — delete own URL *(auth required)*
* `GET /shorten/my` — list user's URLs *(auth required)*
* `GET /shorten/admin/urls` — list all URLs *(admin only)*
* `DELETE /shorten/admin/{shortCode}` — delete any URL *(admin only)*

### Analytics APIs

* `GET /analytics/{shortCode}/click-count` — total clicks *(auth required)*
* `GET /analytics/{shortCode}/click-events` — paginated/filterable click data *(auth required)*
* `GET /analytics/{shortCode}/click-events/daily` — daily grouped clicks *(auth required)*
* `GET /analytics/{shortCode}/click-events/range` — stats in date range *(auth required)*
* `GET /analytics/admin/top-clicked` — top clicked URLs *(admin only)*

---

## ✅ Testing

We use both unit and integration testing:

* **MockMvc tests** for all Controllers

---

## 🌟 Highlighted Design Decisions

* **Stateless JWT Auth**: Every secured endpoint requires token
* **Custom Exceptions**: `UserAlreadyExistsException`,`ExpiredShortCodeException`, `InvalidCredentialsException`, `ForbiddenOperationException` etc.
* **Service Layer Contracts**: Interfaces like `IShortUrlService`, `IAnalyticsService`, `IAuthService`
* **DTO Use**: Controllers return DTOs, services work with entities/params directly
* **User-Agent Parsing**: Stores browser, OS, device info for each click

---

## 💼 Folder Structure

```

├── advices
├── configs
├── controllers
├── dtos
├── exceptions
├── models
├── repositories
├── security
├── services
├── utils
└── test
```

---

## 🙏 Acknowledgements

* Spring Boot Official Docs
* JWT (jjwt)
* Flyway
* ua-parser Java for User-Agent parsing

---

## 🏆 Future Enhancements

* OAuth2 Login (Google, GitHub)
* URL preview on hover
* Click throttling & abuse protection
* Admin dashboard with metrics

---

#### Feel free to fork and contribute!
