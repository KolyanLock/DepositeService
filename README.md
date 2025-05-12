# UserDepositeService

Сервис для управления пользовательскими депозитами с использованием Spring Boot, PostgreSQL и Elasticsearch.
Это ветка с транзакциями для идемпотентности операций по переводам (не путайте с транзакциями БД).

## Требования

- Java 21
- Maven 3.9.x
- Docker + Docker Compose (Docker Desktop будет достаточно)

## Настройка

1. Создайте `.env` файл в корне проекта:

    ```properties
    DB_URL=jdbc:postgresql://host.docker.internal:5432/user_deposite_db
    DB_USER=postgres_логин
    DB_PASSWORD=postgres_пароль
    ES_URIS=http://host.docker.internal:9200
    ES_PASSWORD=elastic_пароль
    JWT_SECRET=jwt_секрет
    ```

2. Для запуска приложения без `Dockerfile`:

   Создайте файл `src/main/resources/secret/secret.properties`.
   `JWT_SECRET` - необязательный дополнительный параметр,
   если не задать будет использоваться дефолтное значение, но это не безопасно.
   Можно также задать эти свойства как переменные окружения в конфигурации запуска IDEA.
   ```properties
    DB_URL=jdbc:postgresql://localhost:5432/user_deposite_db
    DB_USER=postgres_логин
    DB_PASSWORD=postgres_пароль
    ES_URIS=http://localhost:9200
    ES_PASSWORD=elastic_пароль
    JWT_SECRET=jwt_секрет
    ```

3. Запустите контейнеры с PostgreSQL и Elasticsearch
    ```bash
    docker-compose up -d
    ```

4. Сборка и запуск приложения без Dockerfile:
    - сборка и запуск тестов
        ```bash
        mvn clean package
        ```
    - быстрая сборка с пропуском тестов
      ```bash
      mvn clean package -DskipTests
      ```
    - запуск jar-файла приложения
      ```bash
      java -jar target/UserDepositeService-0.0.1-SNAPSHOT.jar
      ```

5. Сборка и запуск приложения с помощью Dockerfile
    - сборка docker-образа
      ```bash
      docker build -t user_deposit_app .
      ```
    - запуск контейнера
      ```bash
      docker run --env-file .env --name user_deposit_app_container -p 8080:8080 -d user_deposit_app
      ```

## Swagger UI

- После запуска сервиса доступен Swagger UI по адресу:  
  <http://localhost:8080/swagger-ui/index.html>
- Данные пользователей можно посмотреть в файле:  
  `src/main/resources/sql/init_user_data.sql`

## Заметки

Для блокировки планировщиков при запуске нескольких экземпляров приложения используется 
упрощённый вариант через advisory lock. Можно вместо него настроить Quartz Job Scheduler для небольшого
количества одновременно запущенных экземпляров или Redis + Scheduled Lock
для большого количества экземпляров. Готов реализовать, если требуется.
Запуск полностью через docker-compose тоже могу сделать.