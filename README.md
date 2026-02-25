# 📦 Inventory Inputs - Stock Control and Production Optimization API

API developed with Spring Boot for raw material inventory control and production optimization,
finding the most profitable combination of products based on available stock.

## 🚀Technologies Used

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- H2 Database (in-memory)
- Lombok
- Maven

## 📋 Prerequisites

- Java 17 ou superior
- Maven
- IntelliJ IDEA (recommended) or another IDE of your choice
- Postman (para testar a API)

## 🔧 Environment Setup

### 1. Clone the repository

```bash
git clone https://github.com/seu-usuario/inventory-inputs.git
cd inventory-inputs
````
### 2. onfigure the H2 database (already configured in application.properties)
````
spring.datasource.url=jdbc:h2:mem:inventorydb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
````
### 3.Run the application
````
./mvnw spring-boot:run
````
Or run the InventoryInputsApplication.java class from your IDE.

### 📚 Project Structure

| Package | Description |
|--------|-----------|
| `controller/` | API endpoints (ProductController, RawMaterialController, ProductionController) |
| `dto/` | Data transfer records (ProductRequest, ProductResponse, etc.) |
| `model/` | JPA entities (Product, RawMaterial, ProductComposition) |
| `repository/` | Database access interfaces (ProductRepository, RawMaterialRepository) |
| `service/` | Business rules (ProductService, RawMaterialService, OptimizationService) |
| `config/` | Configurations (DataLoader to populate the database) |

#### 📊 Data Model

## 📦 Raw Materials

| Campo           | Tipo    | Descrição                     |
|----------------|---------|------------------------------|
| code           | String  |Raw material code      |
| name           | String  | Raw material name        |
| stockQuantity  | Double  | Quantity in stock        |
| unitOfMeasure  | String  | Unit of measure (kg, unit, etc.)   |


## 📦 Product

Represents an item produced from raw materials.

| Campo        | Tipo                     | Descrição                                     |
|-------------|--------------------------|-----------------------------------------------|
| id          | Long                     | Unique product identifier                     |
| code        | String                   | Internal product code                         |
| name        | String                   | Product name                               |
| price       | Double                   | Selling price per unit                   |
| profit      | Double                   | Profit per unit sold              |
| composition | List<ProductComposition> | List of raw materials and required quantities |

## 🌐 API Endpoints

### 📦 Raw Materials (`/api/raw-materials`)

| Method | Endpoint                               | Description                  |
|--------|----------------------------------------|----------------------------|
| GET    | `/`                                    | List all               |
| GET    | `/{id}`                                | Find by ID            |
| GET    | `/code/{code}`                         | Find by code          |
| POST   | `/`                                    | Create new              |
| PUT    | `/{id}`                                | Update                  |
| DELETE | `/{id}`                                | Deletar                    |
| PATCH  | `/{id}/add-stock?quantity=10`          | Add to stock       |
| PATCH  | `/{id}/remove-stock?quantity=10`       | Remove from stock         |


---

### 📦 Products (`/api/products`)

Endpoints responsible for product management and analysis.

| Method | Endpoint                                      | Description                           |
|--------|-----------------------------------------------|---------------------------------------|
| GET    | `/api/products`                               | List all products                     |
| GET    | `/api/products/{id}`                          | Find product by ID                |
| GET    | `/api/products/code/{code}`                   | Find product by code           |
| POST   | `/api/products`                               | Create new product                    |
| PUT    | `/api/products/{id}`                          | Update product                    |
| DELETE | `/api/products/{id}`                          | Delete product                       |
| GET    | `/api/products/{id}/composition`              | View product composition          |
| GET    | `/api/products/{id}/analysis`                 | Detailed product analysis          |
| GET    | `/api/products/optimize`                      | Optimize production                    |
| GET    | `/api/products/feasible`                      | List feasible products for production |
| POST   | `/api/products/{id}/simulate?quantity=10`     | Simulate production                     |
| POST   | `/api/products/{id}/produce?quantity=10`      | Produce product                     |

---
Some endpoints are not being used in the Frontend.

### 📊 Production Optimization (`/api/production`)

Endpoints responsible for global optimization strategies.

| Method | Endpoint                                | Description                               |
|--------|-----------------------------------------|------------------------------------------|
| GET    | `/api/production/optimize?topN=3`       | Returns the Top N most profitable products |

## 🚀 How to Test with Postman

Follow the steps below to test the API using Postman.

---

### 1️⃣ Create Raw Materials

**Endpoint:**  
`POST /api/raw-materials`

**Body (JSON):**

```json
{
  "code": "001",
  "name": "Metal",
  "stockQuantity": 300,
  "unitOfMeasure": "kg"
}
````
### 2️⃣ Create Products
**Endpoint:**  
`POST /api/products`

**Body (JSON):**

```json
{
  "code": "C001",
  "name": "Cadeira",
  "price": 100,
  "profit": 45,
  "composition": [
    {
      "rawMaterialCode": "001",
      "requiredQuantity": 5
    },
    {
      "rawMaterialCode": "002",
      "requiredQuantity": 2
    },
    {
      "rawMaterialCode": "003",
      "requiredQuantity": 0.5
    }
  ]
}
````

### 3️⃣ View Production Optimization
**Endpoint:**  
`GET /api/production/optimize?topN=3`

This endpoint returns the Top N most profitable products considering the current stock.

## 📖 Usage Example
Scenario: Finding the Most Profitable Product
Current stock:

Metal: 300 kg

Plastic: 150 kg

Paint: 35 L

Chair – 60 units – Profit: $2,700.00