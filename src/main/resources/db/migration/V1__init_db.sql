CREATE
    EXTENSION IF NOT EXISTS pg_trgm;

-- 1. Схема пользователя
CREATE TABLE "user"
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(500) NOT NULL,
    date_of_birth DATE         NOT NULL,
    password      VARCHAR(500) NOT NULL CHECK (char_length(password) BETWEEN 8 AND 500)
);

-- 2. Схема аккаунта
CREATE TABLE account
(
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT         NOT NULL UNIQUE REFERENCES "user" (id),
    deposit        NUMERIC(19, 2) NOT NULL CHECK (balance >= 0),
    capitalization NUMERIC(19, 2) NOT NULL CHECK (balance >= 0),
    balance        NUMERIC(19, 2) NOT NULL CHECK (balance >= 0)
);

-- 3. Схема email
CREATE TABLE email_data
(
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT       NOT NULL REFERENCES "user" (id),
    email   VARCHAR(200) NOT NULL UNIQUE
);

-- 4. Схема телефона
CREATE TABLE phone_data
(
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT      NOT NULL REFERENCES "user" (id),
    phone   VARCHAR(13) NOT NULL UNIQUE
);

-- 5. Транзакции
CREATE TABLE transaction
(
    id           BIGSERIAL PRIMARY KEY,
    from_user_id BIGINT         NOT NULL,
    to_user_id   BIGINT         NOT NULL,
    amount       NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    type         varchar(50)    NOT NULL,
    status       varchar(50)    NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL,
    completed_at TIMESTAMPTZ
);

-- 6. Индексы

-- 1) Для фильтрации по дате
CREATE INDEX idx_user_date_of_birth ON "user" (date_of_birth);

-- 2) Для LIKE '%…%' или префиксных LIKE на name
CREATE INDEX idx_user_name_trgm ON "user" USING GIN (name gin_trgm_ops);

-- 3a) Для точного поиска по email / phone
CREATE INDEX idx_email_data_email ON email_data (email);
CREATE INDEX idx_phone_data_phone ON phone_data (phone);

-- 3b) fuzzy-поиск по email / phone
CREATE INDEX idx_email_data_email_trgm ON email_data USING GIN (email gin_trgm_ops);
CREATE INDEX idx_phone_data_phone_trgm ON phone_data USING GIN (phone gin_trgm_ops);

-- 4) Индексы для быстрого поиска транзакций по отправителю и получателю
CREATE INDEX idx_transaction_from_user ON transaction (from_user_id);
CREATE INDEX idx_transaction_to_user ON transaction (to_user_id);