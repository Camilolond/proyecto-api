# Proyecto-api

# How to run
Copy the repository to a local folder

```bash
git clone https://github.com/Camilolond/proyecto-api.git
```
Run the project

```bash
mvn spring-boot:run
```

# API endpoints
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
Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method POST -Body $body -ContentType "application/json"
```

PUT update product
```bash
$body = '{"name":"Gaming Laptop","description":"High-performance gaming laptop","price":1500.00,"stock":5}'
```

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/products/1" -Method PUT -Body $body -ContentType "application/json"
```

DELETE product by ID
```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/products/2" -Method DELETE
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
  -H "Content-Type: application/json" \
  -d '{"name":"Monitor","description":"1440p 240hz Monitor","price":399.99,"stock":15}'
```

PUT update product
```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Gaming Laptop","description":"High-performance gaming laptop","price":1500.00,"stock":5}'
```

DELETE product by ID
```bash
curl -X DELETE http://localhost:8080/api/products/2
```