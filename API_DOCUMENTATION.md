# Stock Trading Application API

This Spring Boot application provides REST APIs for a stock trading platform. The application uses Java 17, Spring Boot 3.5, and provides CRUD operations for all entities in the system.

## Technologies Used

- **Java**: 17
- **Spring Boot**: 3.5.3
- **Spring Data JPA**: For data persistence
- **PostgreSQL**: Production database
- **H2**: Test database
- **Maven**: Build tool

## Entities

The application manages the following entities:

1. **Stock** - Available stocks for trading
2. **UserProfile** - User profiles with balances
3. **StockTransaction** - Buy/sell transactions
4. **OwnedStock** - Stocks currently owned by users
5. **HistoryLog** - Activity logs for users
6. **StockHistory** - Historical price data for stocks
7. **TransactionHistory** - Transaction status history

## REST API Endpoints

All endpoints follow RESTful conventions and return JSON responses.

### Stock Management (`/api/stocks`)

- `GET /api/stocks` - Get all stocks
- `GET /api/stocks/{id}` - Get stock by ID
- `GET /api/stocks/count` - Get total count of stocks
- `POST /api/stocks` - Create a new stock
- `PUT /api/stocks/{id}` - Update an existing stock
- `DELETE /api/stocks/{id}` - Delete a stock

### Stock Transactions (`/api/stock-transactions`)

- `GET /api/stock-transactions` - Get all transactions
- `GET /api/stock-transactions/{id}` - Get transaction by ID
- `GET /api/stock-transactions/count` - Get total count of transactions
- `POST /api/stock-transactions` - Create a new transaction
- `PUT /api/stock-transactions/{id}` - Update an existing transaction
- `DELETE /api/stock-transactions/{id}` - Delete a transaction

### User Profiles (`/api/user-profiles`)

- `GET /api/user-profiles` - Get all user profiles
- `GET /api/user-profiles/{id}` - Get user profile by ID
- `GET /api/user-profiles/count` - Get total count of users
- `POST /api/user-profiles` - Create a new user profile
- `PUT /api/user-profiles/{id}` - Update an existing user profile
- `DELETE /api/user-profiles/{id}` - Delete a user profile

### Owned Stocks (`/api/owned-stocks`)

- `GET /api/owned-stocks` - Get all owned stock records
- `GET /api/owned-stocks/{id}` - Get owned stock by ID
- `GET /api/owned-stocks/count` - Get total count of owned stocks
- `POST /api/owned-stocks` - Create a new owned stock record
- `PUT /api/owned-stocks/{id}` - Update an existing owned stock record
- `DELETE /api/owned-stocks/{id}` - Delete an owned stock record

### History Logs (`/api/history-logs`)

- `GET /api/history-logs` - Get all history logs
- `GET /api/history-logs/{id}` - Get history log by ID
- `GET /api/history-logs/count` - Get total count of history logs
- `POST /api/history-logs` - Create a new history log
- `PUT /api/history-logs/{id}` - Update an existing history log
- `DELETE /api/history-logs/{id}` - Delete a history log

### Stock History (`/api/stock-histories`)

- `GET /api/stock-histories` - Get all stock price history
- `GET /api/stock-histories/{id}` - Get stock history by ID
- `GET /api/stock-histories/count` - Get total count of stock history records
- `POST /api/stock-histories` - Create a new stock history record
- `PUT /api/stock-histories/{id}` - Update an existing stock history record
- `DELETE /api/stock-histories/{id}` - Delete a stock history record

### Transaction History (`/api/transaction-histories`)

- `GET /api/transaction-histories` - Get all transaction history
- `GET /api/transaction-histories/{id}` - Get transaction history by ID
- `GET /api/transaction-histories/count` - Get total count of transaction history
- `POST /api/transaction-histories` - Create a new transaction history record
- `PUT /api/transaction-histories/{id}` - Update an existing transaction history record
- `DELETE /api/transaction-histories/{id}` - Delete a transaction history record

## Configuration

The application includes several configuration classes:

- **WebConfig**: CORS configuration for API endpoints
- **ApplicationConfig**: Application-specific properties (scheduler settings)
- **JacksonConfig**: JSON serialization configuration

## Running the Application

### Prerequisites

- Java 17
- PostgreSQL database (for production)
- Maven

### Testing

The application includes comprehensive test coverage:

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=StockServiceTest
```

### Development

For development, you can use the H2 in-memory database by running with the `test` profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

### Production

Configure PostgreSQL connection in `application.yml` and run:

```bash
mvn spring-boot:run
```

## Database Schema

The application automatically creates the following tables:

- `stock` - Stock information
- `user_profile` - User profiles
- `stock_transaction` - Transaction records
- `owned_stock` - Current stock ownership
- `history_log` - User activity logs
- `stock_history` - Stock price history
- `transaction_history` - Transaction status changes

All entities use UUID primary keys and include proper foreign key relationships.

## HTTP Status Codes

The API uses standard HTTP status codes:

- `200 OK` - Successful GET requests
- `201 Created` - Successful POST requests
- `204 No Content` - Successful DELETE requests
- `404 Not Found` - Resource not found
- `400 Bad Request` - Invalid request data

## Example Usage

### Create a Stock

```bash
curl -X POST http://localhost:8080/api/stocks \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "AAPL",
    "name": "Apple Inc.",
    "currentPrice": 150.00,
    "market": "NASDAQ",
    "description": "Technology company"
  }'
```

### Get All Stocks

```bash
curl http://localhost:8080/api/stocks
```

### Get Stock by ID

```bash
curl http://localhost:8080/api/stocks/{stock-id}
```