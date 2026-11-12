# Proyecto-api
Simple web API application with microservices for products and users that are stored in PostgreSQL, the app uses JWT for user authentication.

| Service | Port | Description |
|---|---|---|
| `postgres` | 5432 | PostgreSQL 17, holds one database per service: `products_db` and `users_db` |
| `product-service` | 8080 | CRUD of products, comes with 3 products preloaded for testing. GET is public, the rest requires JWT |
| `user-service` | 8081 | CRUD of users, passwords are stored as BCrypt hashes. Endpoints protected with JWT |
| `auth-service` | 8082 | Registers users and issues JWTs signed with RSA (RS256) on login |

`auth-service` talks to `user-service` through `/internal/users` endpoints that require the `X-Internal-Key` header.

# Security
The tokens are JWTs signed with **RS256** (RSA + SHA-256), following the OAuth 2.0 Bearer Token standard:

- `auth-service` signs the tokens with an RSA **private key** that only it knows
- `auth-service` publishes the matching **public key** at `GET /.well-known/jwks.json` (JWK Set format)
- `product-service` and `user-service` download the public key from that URL and use it to verify the signature, they can verify tokens but can't create them

The private key is read from the PEM file set in `JWT_PRIVATE_KEY_PATH`. If no file is set, `auth-service` generates a temporary key on startup, which is fine for local development but every restart invalidates the issued tokens. Generate a key with

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
```

`*.pem` files are ignored by git, never commit the private key.

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
INTERNAL_API_KEY=<random-key> DB_USERNAME=<user> DB_PASSWORD=<password> docker compose up --build
```

# How to run with Kubernetes

The manifests are in the [k8s/](k8s/) folder, one file per service, all inside the `proyecto-api` namespace:

| File | Content |
|---|---|
| `namespace.yaml` | `proyecto-api` namespace |
| `secrets.yaml` | Database credentials and `INTERNAL_API_KEY` (development values) |
| `postgres.yaml` | PostgreSQL with a 1Gi persistent volume and the script that creates the databases |
| `user-service.yaml`, `auth-service.yaml`, `product-service.yaml` | Deployment + Service of each microservice |
| `kustomization.yaml` | Lists the files so everything is applied with one command |

You need a local cluster, for example the one included in Docker Desktop (Settings > Kubernetes > Enable Kubernetes).

1. Build the images, Docker Desktop's cluster uses the local images directly

```bash
docker compose build
```

2. Deploy everything

```bash
kubectl apply -k k8s/
```

3. Create the RSA private key used to sign the JWTs and restart `auth-service` so it loads it

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
kubectl create secret generic jwt-private-key -n proyecto-api --from-file=private.pem=private.pem
kubectl rollout restart deployment/auth-service -n proyecto-api
```

4. Check that all the pods are `Running` and `READY 1/1` (it takes around a minute)

```bash
kubectl get pods -n proyecto-api
```

The services are exposed with NodePort:

| Service | URL |
|---|---|
| `product-service` | `http://localhost:30080/api/products` |
| `user-service` | `http://localhost:30081/api/users` |
| `auth-service` | `http://localhost:30082/api/auth` |

If your cluster doesn't expose NodePorts on `localhost` (minikube, kind), use port forwarding instead, for example `kubectl port-forward -n proyecto-api service/auth-service 8082:8082`

Delete everything, including the database data and the RSA key (deleting the namespace deletes everything inside it)

```bash
kubectl delete -k k8s/
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