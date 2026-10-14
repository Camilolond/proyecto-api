# Proyecto-api
Simple web API application with microservices for products and users that are stored in PostgreSQL, the app uses JWT for user authentication.

| Service | Port | Description |
|---|---|---|
| `postgres` | 5432 | PostgreSQL 17, holds one database per service: `products_db` and `users_db` |
| `product-service` | 8080 | CRUD of products, comes with 3 products preloaded for testing. GET is public, the rest requires JWT |
| `user-service` | 8081 | CRUD of users, passwords are stored as BCrypt hashes. Endpoints protected with JWT |
| `auth-service` | 8082 | Registers users and issues JWTs (HS256) on login |

`auth-service` talks to `user-service` through `/internal/users` endpoints that require the `X-Internal-Key` header. All the services share the `JWT_SECRET` used to sign and verify the tokens.

The tables are created automatically by Hibernate on startup. The databases are created by [database/init.sql](database/init.sql) the first time the PostgreSQL container starts.

The example data is only loaded when the tables are empty. `user-service` comes with 2 users preloaded for testing:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

# How to run
Copy the repository to a local folder

```bash
git clone https://github.com/Camilolond/proyecto-api.git
```

`product-service` and `user-service` need PostgreSQL running on `localhost:5432` (user `postgres`, password `postgres`) with the `products_db` and `users_db` databases. The easiest way is to start only the database with Docker

```bash
cd proyecto-api
docker compose up -d postgres
```

Navigate to the folder of the service and run it, for example

```bash
cd user-service
mvn spring-boot:run
```

`auth-service` needs `user-service` running to register and log in users

# How to run with Docker

Build and start all the services with Docker Compose from the root folder

```bash
docker compose up --build
```

To stop the services, the data is kept in the `postgres-data` volume

```bash
docker compose down
```

To stop the services and delete all the data

```bash
docker compose down -v
```

The project currently uses development defaults for the secrets and database credentials, however it is recommended to set your own when deploying the app in a real enviroment using the following command:

```bash
JWT_SECRET=<at-least-32-characters> INTERNAL_API_KEY=<random-key> DB_USERNAME=<user> DB_PASSWORD=<password> docker compose up --build
```

# Auth endpoints
## Bash

POST register user
```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"oscar","email":"oscar@example.com","password":"secret1"}'
```

POST login, returns `{"accessToken":"...","tokenType":"Bearer","expiresIn":3600}`
```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

GET claims of the current token
```bash
curl http://localhost:8082/api/auth/me -H "Authorization: Bearer $TOKEN"
```

## PowerShell (Windows)
POST login and save the token, then returns the login response json
```bash
  $body = '{"username":"admin","password":"admin123"}'
  $loginResponse = Invoke-RestMethod `
    -Uri "http://localhost:8082/api/auth/login" `
    -Method POST `
    -Body $body `
    -ContentType "application/json"
  $token = $loginResponse.accessToken
  $headers = @{ Authorization = "Bearer $token" }
  $loginResponse | ConvertTo-Json -Depth 10
```

# User endpoints
Every endpoint requires the `Authorization: Bearer <token>` header

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/users` | ADMIN |
| GET | `/api/users/me` | Any authenticated user |
| GET | `/api/users/{id}` | Owner or ADMIN |
| PUT | `/api/users/{id}` | Owner or ADMIN, body `{"email":"...","password":"..."}` (both optional) |
| DELETE | `/api/users/{id}` | ADMIN |

## Bash
```bash
curl http://localhost:8081/api/users -H "Authorization: Bearer $TOKEN"
curl http://localhost:8081/api/users/me -H "Authorization: Bearer $TOKEN"
curl -X PUT http://localhost:8081/api/users/2 \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"email":"new@example.com"}'
curl -X DELETE http://localhost:8081/api/users/2 -H "Authorization: Bearer $TOKEN"
```

## PowerShell (Windows)
```bash
Invoke-RestMethod -Uri "http://localhost:8081/api/users" -Headers $headers
Invoke-RestMethod -Uri "http://localhost:8081/api/users/me" -Headers $headers
```

# Product endpoints
GET endpoints are public, POST, PUT and DELETE require the `Authorization: Bearer <token>` header (see Auth endpoints to get a token)

## PowerShell (Windows)

GET all products
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/products"
```

GET product by ID
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/products/1"
```

POST create product
```bash
$body = '{"name":"Monitor","description":"1440p 240hz Monitor","price":399.99,"stock":15}'
```

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method POST -Headers $headers -Body $body -ContentType "application/json"
```

PUT update product
```bash
$body = '{"name":"Gaming Laptop","description":"High-performance gaming laptop","price":1500.00,"stock":5}'
```

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/products/1" -Method PUT -Headers $headers -Body $body -ContentType "application/json"
```

DELETE product by ID
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/products/2" -Method DELETE -Headers $headers
```

## Bash
GET all products
```bash
curl http://localhost:8080/api/products
```

GET product by ID
```bash
curl http://localhost:8080/api/products/1
```

POST create product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Monitor","description":"1440p 240hz Monitor","price":399.99,"stock":15}'
```

PUT update product
```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Gaming Laptop","description":"High-performance gaming laptop","price":1500.00,"stock":5}'
```

DELETE product by ID
```bash
curl -X DELETE http://localhost:8080/api/products/2 -H "Authorization: Bearer $TOKEN"
```