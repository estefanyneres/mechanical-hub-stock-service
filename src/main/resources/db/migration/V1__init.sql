-- Migrations do stock-service comecam aqui (TASK-028: stock,
-- stock_movements, stock_pending_items).
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

SET timezone TO 'UTC';
