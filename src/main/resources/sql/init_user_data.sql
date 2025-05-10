-- Пользователь 1: ivanov@1993-05-01!
WITH new_user AS (
INSERT
INTO "user" (name, date_of_birth, password)
VALUES ('Иван Иванов', '1993-05-01', '$2a$10$3xlUksXZuXABUaLUm4WgSuDCwzwnBQcyo6so2zki8k0/9TlUPwxYm')
    RETURNING id
    ), ins_account AS (
INSERT
INTO account(user_id, deposit, balance)
SELECT id, 1000.00, 1000.00
FROM new_user
    ), ins_email AS (
INSERT
INTO email_data(user_id, email)
SELECT id, unnest(
    ARRAY['ivanov@example.com', 'ivanov2@example.com']
    )
FROM new_user
    )
INSERT
INTO phone_data(user_id, phone)
SELECT id,
       unnest(ARRAY['79201234567', '79201230001'])
FROM new_user;

-- Пользователь 2: petrova@1988-11-23!
WITH new_user AS (
INSERT
INTO "user" (name, date_of_birth, password)
VALUES ('Мария Петрова', '1988-11-23', '$2a$10$uFjNWC7xNmzE9nnzg2JYjOydfufOueoqY7tBvBja0vDuCKZWGK.SS')
    RETURNING id
    ), ins_account AS (
INSERT
INTO account(user_id, deposit, balance)
SELECT id, 250.50, 250.50
FROM new_user
    ), ins_email AS (
INSERT
INTO email_data(user_id, email)
SELECT id, 'petrova@example.com'
FROM new_user
    )
INSERT
INTO phone_data(user_id, phone)
SELECT id, '79209876543'
FROM new_user;

-- Пользователь 3: sidorov@2000-02-14!
WITH new_user AS (
INSERT
INTO "user" (name, date_of_birth, password)
VALUES ('Сергей Сидоров', '2000-02-14', '$2a$10$ebJdv9ygqSvofizWpFqyVuqb2PXmxTvMjlaIlubW9Aq5x7l7Mgcte')
    RETURNING id
    ), ins_account AS (
INSERT
INTO account(user_id, deposit, balance)
SELECT id, 0.00, 0.00
FROM new_user
    ), ins_email AS (
INSERT
INTO email_data(user_id, email)
SELECT id, 'sidorov@example.com'
FROM new_user
    )
INSERT
INTO phone_data(user_id, phone)
SELECT id, '79201112233'
FROM new_user;
