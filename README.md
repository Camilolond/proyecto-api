mvn spring-boot:run

# GET all products
Invoke-RestMethod -Uri "http://localhost:8080/api/products"

# GET product by ID
Invoke-RestMethod -Uri "http://localhost:8080/api/products/1"

# POST create product
$body = '{"name":"Monitor","description":"1440p 240hz Monitor","price":399.99,"stock":15}'
Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method POST -Body $body -ContentType "application/json"

# PUT update product
$body = '{"name":"Gaming Laptop","description":"High-performance gaming laptop","price":1500.00,"stock":5}'
Invoke-RestMethod -Uri "http://localhost:8080/api/products/1" -Method PUT -Body $body -ContentType "application/json"

# DELETE product
Invoke-RestMethod -Uri "http://localhost:8080/api/products/2" -Method DELETE