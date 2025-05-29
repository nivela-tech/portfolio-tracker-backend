# Portfolio Tracker Backend Documentation

This document provides a high-level overview of the Portfolio Tracker backend application to help AI agents work with the codebase more effectively.

## Application Structure

- `com.portfolio.tracker`
  - **controller/**: REST API controllers
  - **model/**: Domain entities
  - **repository/**: Database access layer
  - **service/**: Business logic
  - **config/**: Configuration classes
  - **security/**: Security configuration
  - **exception/**: Custom exceptions

## Database Model

1. **User**: Authenticated user
2. **PortfolioAccount**: Account associated with a user
3. **PortfolioEntry**: Individual portfolio entry belonging to an account

## API Endpoints

### Authentication

All endpoints require OAuth2 authentication unless specified.

### Portfolio Controller (`/api/portfolio`)

| Method | Endpoint                 | Description                           | Parameters                        |
|--------|--------------------------|---------------------------------------|------------------------------------|
| GET    | `/`                      | Get all entries                       | `accountId` (optional)             |
| GET    | `/combined`              | Get combined portfolio                | None                               |
| GET    | `/combined/by-currency`  | Get combined portfolio by currency    | None                               |
| GET    | `/currency/{currency}`   | Get entries by currency               | `currency`, `accountId` (optional) |
| GET    | `/country/{country}`     | Get entries by country                | `country`, `accountId` (optional)  |
| GET    | `/source/{source}`       | Get entries by source                 | `source`, `accountId` (optional)   |
| GET    | `/type/{type}`           | Get entries by type                   | `type`, `accountId` (optional)     |
| POST   | `/entries`               | Add new entry                         | `entry` object in request body     |
| PUT    | `/entries/{id}`          | Update entry                          | `id` and `entry` object            |
| DELETE | `/entries/{id}`          | Delete entry                          | `id` and user details in body      |
| GET    | `/export/xlsx`           | Export portfolio as Excel             | `accountId` (optional)             |
| GET    | `/export/csv`            | Export portfolio as CSV               | `accountId` (optional)             |

### Account Controller (`/api/accounts`)

| Method | Endpoint                 | Description                           | Parameters                        |
|--------|--------------------------|---------------------------------------|------------------------------------|
| GET    | `/`                      | Get all accounts                      | None                               |
| GET    | `/{id}`                  | Get specific account                  | `id`                               |
| POST   | `/`                      | Create account                        | `account` object in request body   |
| PUT    | `/{id}`                  | Update account                        | `id` and `account` object          |
| DELETE | `/{id}`                  | Delete account                        | `id`                               |

## Key Services

1. **PortfolioService**: Handles CRUD operations for portfolio entries
   - `addEntry(entry, user)`: Creates a new portfolio entry
   - `updateEntry(entry, user)`: Updates an existing entry
   - `deleteEntry(id, user)`: Deletes an entry
   - `getAllEntriesByUser(user)`: Gets all entries for a user
   - `getEntriesByAccountIdAndUser(accountId, user)`: Gets entries for specific account

2. **PortfolioAccountService**: Manages portfolio accounts
   - `createAccount(account)`: Creates a new account
   - `findAllAccountsByUser(user)`: Finds all accounts for a user
   - `findAccountById(id)`: Finds a specific account

3. **ExportService**: Handles data export functionality
   - `exportEntriesToXlsx(entries)`: Exports entries to Excel format
   - `exportEntriesToCsv(entries)`: Exports entries to CSV format

## Authentication Flow

1. User authenticates via Google OAuth2
2. Backend receives OAuth2User principal
3. User information is extracted from principal
4. If user doesn't exist in the database, a new user record is created
5. User is associated with their actions via the OAuth2 principal

## Common Issues and Solutions

1. **Authentication Issues**: Ensure the OAuth2User principal is correctly passed and the user is retrieved properly.
2. **CSRF Protection**: All non-GET requests should include CSRF tokens.
3. **Entry Ownership**: When updating/deleting entries, verify ownership to prevent unauthorized access.
4. **UUID vs Long IDs**: All IDs are UUIDs, not Long values. Make sure to use appropriate type conversion.

## Example API Usage

### Adding a new entry
```json
POST /api/portfolio/entries
{
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "type": "STOCK",
  "currency": "USD",
  "amount": 1000.0,
  "country": "United States",
  "source": "ETrade",
  "notes": "Initial investment",
  "dateAdded": "2023-05-01T00:00:00",
  "user": {
    "email": "user@example.com"
  }
}
```

### Exporting data
GET `/api/portfolio/export/xlsx?accountId=550e8400-e29b-41d4-a716-446655440000`
GET `/api/portfolio/export/csv` (for all accounts)
