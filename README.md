# Document Management and Q&A System

A comprehensive Spring Boot backend application for document management and basic Q&A functionality.

## Features

- User authentication with JWT
- Role-based access control (Admin, Editor, Viewer)
- Document upload and management
- Document content extraction and processing
- Full-text search for Q&A
- Asynchronous document processing
- Batch processing for large document uploads
- Caching for frequently accessed data
- Message queuing for decoupling ingestion tasks

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL for document storage with full-text search
- Redis for caching
- RabbitMQ for message queuing
- Apache Tika for document content extraction
- Spring Batch for batch processing
- Docker for containerization
- JUnit and Mockito for testing

## Project Structure

The application follows a clean architecture with the following components:

- **Controller Layer**: REST endpoints
- **Service Layer**: Business logic
- **Repository Layer**: Data access
- **Model/Entity Layer**: Database entities
- **DTO Layer**: Data transfer objects
- **Config Layer**: Application configuration
- **Security Layer**: Authentication and authorization
- **Exception Handling**: Global exception handling
- **Batch Processing**: Document processing in batches

## Getting Started

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL (if running locally)
- Redis (if running locally)
- RabbitMQ (if running locally)

### Running with Docker

The easiest way to run the application is using Docker Compose:

\`\`\`bash
# Clone the repository
git clone https://github.com/yourusername/document-management-system.git
cd document-management-system

# Build and start the containers
docker-compose up -d
\`\`\`

This will start the following containers:
- The Spring Boot application
- PostgreSQL database
- Redis cache
- RabbitMQ message broker

The application will be accessible at http://localhost:8080/api

### Running Locally

If you prefer to run the application locally:

1. Make sure you have PostgreSQL, Redis, and RabbitMQ running
2. Configure the application.yml file with your database credentials
3. Run the application:

\`\`\`bash
./mvnw spring-boot:run
\`\`\`

## API Documentation

Once the application is running, you can access the Swagger UI at:
http://localhost:8080/api/swagger-ui.html

The API documentation is also available in JSON format at:
http://localhost:8080/api/api-docs

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate and get JWT token

### Documents

- `POST /api/documents` - Upload a new document
- `GET /api/documents/{id}` - Get document by ID
- `GET /api/documents` - Get documents with filtering and pagination
- `DELETE /api/documents/{id}` - Delete a document
- `POST /api/documents/process-batch` - Trigger batch processing of pending documents

### Q&A

- `POST /api/qa/ask` - Ask a question and get relevant document snippets

### Admin

- `GET /api/admin/users` - Get all users
- `GET /api/admin/users/{id}` - Get user by ID
- `PUT /api/admin/users/{id}/roles` - Update user roles

## Testing

The application includes comprehensive unit tests for all components. To run the tests:

\`\`\`bash
./mvnw test
\`\`\`

To generate a test coverage report:

\`\`\`bash
./mvnw verify
\`\`\`

The coverage report will be available in the `target/site/jacoco` directory.

## Database Schema

The application uses the following main entities:

- **User**: Stores user information and roles
- **Document**: Stores document metadata and content
- **DocumentChunk**: Stores document content in chunks for efficient retrieval

## Security

The application implements the following security measures:

- JWT-based authentication
- Role-based access control
- Password encryption with BCrypt
- HTTPS support (when deployed with proper SSL configuration)

## Performance Considerations

- Asynchronous document processing for better responsiveness
- Batch processing for large document uploads
- Caching frequently accessed data with Redis
- Message queuing with RabbitMQ for decoupling ingestion tasks
- Full-text search with PostgreSQL for efficient document retrieval

## Deployment

The application can be deployed to any cloud provider that supports Docker containers:

- AWS ECS or EC2
- Google Cloud Run or GKE
- Azure Container Instances or AKS

For production deployment, make sure to:
- Use a proper SSL certificate
- Configure proper database credentials
- Set up monitoring and logging
- Configure proper scaling policies

## License

This project is licensed under the MIT License - see the LICENSE file for details.
