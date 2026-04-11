# Lost and Found System

A full-stack web and mobile application for reporting and recovering lost items, built with Spring Boot, Next.js, and Jetpack Compose.

## Project Description
The Lost and Found System is a centralized platform designed to help university students and everyday residents manage lost and found items. The platform allows users to register, log in, report lost items, report found items, and view item locations on a map through a clean, modern interface.

Phase 1 implements the core authentication layer: secure user registration and login with JWT-based stateless authentication. Passwords are hashed before storage. The frontend is a responsive Next.js application that communicates with the Spring Boot REST API, with protected routes that redirect unauthenticated users to the login page. In addition, an Android mobile application allows user access on the go.

## Technologies Used

### Backend
* **Spring Boot 3** - Java-based REST API framework
* **Spring Security** - Authentication and authorization
* **Spring Data JPA** - Database interaction and ORM
* **MySQL** - Relational database
* **JWT (JSON Web Tokens)** - Stateless authentication mechanism
* **Maven** - Dependency management and build tool

### Frontend (Web)
* **React 19** - JavaScript library for building user interfaces
* **Next.js 16** - React framework for web applications
* **Tailwind CSS 4** - Utility-first CSS framework
* **React Leaflet** - Map viewing components integration
* **Lucide React** - UI icons

### Mobile App (Android)
* **Kotlin** - Primary programming language
* **Jetpack Compose** - Modern UI toolkit for native Android
* **XML Layouts** - For base layout structure
* **Retrofit** - Type-safe HTTP client for API requests

### Development Tools
* **Visual Studio Code** - Code editor
* **Android Studio** - Mobile app IDE
* **Git / GitHub** - Version control

## Prerequisites
Before running this application, ensure you have the following installed:

* Java Development Kit (JDK) 17 or higher
* Maven 3.6 or higher
* Node.js 20 or higher
* npm 9 or higher
* MySQL Server
* Android Studio (for mobile app)

## Project Structure
```
IT342_Tiongzon_LostAndFound/
├── backend/              # Spring Boot application
│   └── src/main/java/edu/cit/tiongzon/lostandfound/
│       ├── Controller/   # REST controllers
│       ├── Dto/          # Data transfer objects
│       ├── Entity/       # JPA entities
│       ├── Repository/   # Spring Data repositories
│       ├── Security/     # JWT, filters, SecurityConfig
│       └── Service/      # Business logic
├── web/                  # Next.js frontend application
│   └── lostandfound/
│       ├── app/          # Next.js app router pages
│       ├── components/   # Reusable components (MapViewer, etc.)
│       └── lib/          # Utilities
├── mobile/               # Android mobile application
│   └── app/src/main/
│       ├── java/         # Kotlin source code, Retrofit API
│       └── res/          # UI resources
├── docs/                 # Documentation
└── README.md             # This file
```

## Steps to Run Backend

### 1. Configure Database
Configure your MySQL instance and ensure a database named `authdb` is created. Valid credentials (e.g., root/empty password) should match what is specified in `application.properties`.

### 2. Configure Environment Variables (Optional)
The backend loads configuration from `application.properties`. You can override properties by setting them as environment variables, or updating the application properties to match your local setup.

### 3. Run the Spring Boot Application
From the `backend/lostandfound` directory:
```bash
cd backend/lostandfound
mvn spring-boot:run
```
The backend server will start on `http://localhost:8080/api`.

### 4. Verify Backend is Running
You should see console output indicating that your Spring Boot application has started successfully.

## Steps to Run Web App

### 1. Install Dependencies
Navigate to the web directory and install npm packages:
```bash
cd web/lostandfound
npm install
```

### 2. Start Development Server
```bash
npm run dev
```
The web application will start on `http://localhost:3000` (or another port if specified by Next.js).

### 3. Access the Application
Open your browser and navigate to the local URL. You will see the login page if unauthenticated.

## API Endpoints

### Register User
`POST /api/auth/register`
**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

### Login User
`POST /api/auth/login`
**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "user": { ... },
    "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

## Configuration

### Backend
Configuration is loaded from `backend/lostandfound/src/main/resources/application.properties`:
* **Database**: MySQL via `jdbc:mysql://localhost:3306/authdb`
* **JWT Expiration**: 24 hours (86400000 ms)

### Frontend
The frontend connects to the backend endpoints properly structured to call `/api/auth/*` and item endpoints.

## Security Features
* **Password Encryption** -- All passwords are encrypted before storage.
* **JWT Authentication** -- Stateless authentication using signed JSON Web Tokens.
* **Protected Routes** -- Frontend routes guarded by token presence.
* **Stateless Sessions** -- No server-side session; all state carried in the JWT.

## Testing Checklist
* **User Registration**
  * Valid registration returns success response.
  * Duplicate email returns handled error message.
* **User Login**
  * Valid credentials redirect to dashboard/map.
  * Invalid credentials show error block.
  * JWT token and user data stored securely.
* **Protected Routes**
  * Dashboard/home accessible with valid token.
  * Redirects to login without token.
  * Sign-out clears token and redirects to login.

## Future Enhancements
* Email verification for user registration.
* Advanced map-based radius search for lost and found items.
* Item matching algorithms based on categorical descriptors.
* In-app user-to-user messaging for arranging item handoffs.

## License
This project is developed for educational purposes as part of IT342 coursework.
