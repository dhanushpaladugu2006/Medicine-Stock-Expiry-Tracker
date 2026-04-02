# Medicine Stock Expiry Tracker

A production-style full-stack pharmacy inventory platform with JWT authentication, role-based access, expiry monitoring, stock movement logs, branch-aware medicine management, exportable reports, notification history, and a modern React dashboard.

## Stack

- Backend: Java 17, Spring Boot 3.x, Spring Security, JWT, JPA/Hibernate, MySQL, Flyway, Lombok, MapStruct, OpenAPI, Docker
- Frontend: React + TypeScript, Vite, Tailwind CSS, React Query, Zustand, Axios, React Router v6, Recharts, PWA support

## Folder Structure

```text
medicine-project/
+-- backend/
¦   +-- Dockerfile
¦   +-- pom.xml
¦   +-- src/
¦       +-- main/java/com/medicinetracker/
¦       ¦   +-- config
¦       ¦   +-- controller
¦       ¦   +-- dto
¦       ¦   +-- entity
¦       ¦   +-- exception
¦       ¦   +-- mapper
¦       ¦   +-- repository
¦       ¦   +-- security
¦       ¦   +-- service
¦       ¦   +-- util
¦       +-- main/resources/
¦           +-- application.yml
¦           +-- db/migration/V1__init_schema.sql
+-- frontend/
¦   +-- Dockerfile
¦   +-- nginx.conf
¦   +-- package.json
¦   +-- src/
¦       +-- api
¦       +-- components
¦       +-- hooks
¦       +-- lib
¦       +-- pages
¦       +-- store
¦       +-- types
+-- docker-compose.yml
+-- .env.example
```

## Core Product Areas

- Authentication and user profile management with `ADMIN`, `PHARMACIST`, and `STAFF` roles
- Medicine CRUD with branch-aware inventory, image upload, and CSV bulk import
- Stock adjustments with movement history and automatic low-stock notification triggers
- Expiry tracking with auto status updates and scheduled daily alert scans
- Dashboard metrics, charts, recent notifications, and heuristic expiry-risk predictions
- CSV/PDF reports for expiry, stock, and usage activity
- Audit log tracking for create, update, delete, login, export, and scheduler events
- Dark mode, responsive UI, offline-ready PWA registration, and API interceptors

## API Endpoints

### Auth

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

### Users

- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`

### Branches

- `GET /api/v1/branches`
- `POST /api/v1/branches`

### Medicines

- `GET /api/v1/medicines`
- `GET /api/v1/medicines/{id}`
- `POST /api/v1/medicines`
- `PUT /api/v1/medicines/{id}`
- `DELETE /api/v1/medicines/{id}`
- `POST /api/v1/medicines/{id}/image`
- `POST /api/v1/medicines/bulk-upload`

### Stock

- `POST /api/v1/stocks/adjustments`
- `GET /api/v1/stocks/medicines/{medicineId}/history`

### Dashboard

- `GET /api/v1/dashboard/summary`

### Notifications

- `GET /api/v1/notifications`
- `PATCH /api/v1/notifications/{id}/read`

### Reports

- `GET /api/v1/reports/{type}/export?format=csv|pdf`

### Audit

- `GET /api/v1/audit/logs`

### AI Placeholder`r`n`r`n- `POST /api/v1/ai/recognition-placeholder``r`n`r`n### API Docs

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Sample Database Schema

### `branches`

- `id`, `name`, `code`, `address`, `city`, `state`, `country`, `phone`, `email`, `active`

### `users`

- `id`, `full_name`, `email`, `password`, `phone`, `role`, `branch_id`, `active`
- `email_notifications_enabled`, `sms_notifications_enabled`, `last_login_at`

### `medicines`

- `id`, `name`, `batch_number`, `category`, `manufacturer`
- `quantity`, `reorder_level`, `price`, `expiry_date`, `manufacture_date`
- `barcode`, `image_url`, `status`, `branch_id`, `archived`
- `last_sold_at`, `last_restocked_at`

### `stock_transactions`

- `id`, `medicine_id`, `branch_id`, `performed_by`, `type`
- `quantity_before`, `quantity_change`, `quantity_after`, `reference_note`, `unit_price`, `transaction_date`

### `notifications`

- `id`, `title`, `message`, `type`, `status`, `medicine_id`, `user_id`, `branch_id`
- `channel`, `recipient`, `sent_at`, `read_at`

### `audit_logs`

- `id`, `action`, `entity_type`, `entity_id`, `actor_email`, `description`, `metadata`

## Setup Instructions

### Local backend

1. Create MySQL database `medicine_tracker`.
2. By default the local app expects MySQL on `localhost:3306` with username `root` and password `root`. Override with environment variables if needed.
3. Start the backend:
   - `cd backend`
   - `mvn spring-boot:run`
4. Flyway will create the schema automatically.
5. Register your first admin account through `POST /api/v1/auth/register` or the frontend register page.

### Local frontend

1. Start the frontend:
   - `cd frontend`
   - `npm install`
   - `npm run dev`
2. Open `http://localhost:5173`.
3. Set `VITE_API_URL` if your backend runs on a different host.

### Docker

1. Copy `.env.example` to `.env` if you want to customize settings.
2. Run `docker compose up --build`.
3. Access:
   - Frontend: `http://localhost:5173`
   - Backend: `http://localhost:8080`
   - MailHog: `http://localhost:8025`

## Notes

- AI prediction is implemented as a heuristic sell-through vs expiry risk signal and can be swapped for a dedicated ML service later.
- SMS integration and image recognition are scaffolded as placeholders for downstream provider integration.
- Uploaded medicine images are stored under the configurable upload directory and served through `/uploads/**`.
- Demo users are auto-seeded on startup if they do not already exist:
  `admin@medicinetracker.com / Admin@123`,
  `pharmacist@medicinetracker.com / Pharma@123`,
  `staff@medicinetracker.com / Staff@123`

