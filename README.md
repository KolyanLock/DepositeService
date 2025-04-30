# UserDepositeService

Сервис для управления пользовательскими депозитами с использованием Spring Boot, PostgreSQL и Elasticsearch.

## Требования
- Java 21
- Maven/Gradle
- Docker + Docker Compose

## Настройка
1. Создайте `.env` файл в корне проекта:

    ```properties
    DB_USER=postgres_логин
    DB_PASSWORD=postgres_пароль
    ES_PASSWORD=elastic_пароль
    ```

2. Создайте файл `src/main/resources/secret/secret.properties`. 
Свойства должны совпадать с `.env`. 
`JWT_SECRET` - необязательный дополнительный параметр, 
если не задать будет использоваться дефолтное значение, но это не безопасно. 
Можно также задать эти свойства как переменные окружения в конфигурации запуска IDEA.
   ```properties
    DB_USER=postgres_логин
    DB_PASSWORD=postgres_пароль
    ES_PASSWORD=elastic_пароль
    JWT_SECRET=jwt_секрет
    ```

3. Запустите контейнеры с PostgreSQL и Elasticsearch
    ```bash
    docker-compose up -d
    ```

4. Сборка приложения: 
   - сборка и запуск тестов
       ```bash
       mvn clean package
       ```
   - быстрая сборка с пропуском тестов
     ```bash
     mvn clean package -DskipTests
     ```

5. Запуск jar-файла приложения:
    ```bash
    java -jar target/UserDepositeService-0.0.1-SNAPSHOT.jar
    ```

## Пояснение
Логика для перевода средств для банковских операции требует идемпотентности, 
для этого требуется действовать в 2 этапа: сначала создать транзакцию, 
потом произвести операцию перевода с использованием ID транзакции. 
Возможно в тестовом хотели увидеть именно это, но я ещё не успел.
Также для того чтобы правильно работал планировщик пополнения баланса при запуске
нескольких экземпляров сервиса требуется настроить Quartz Job Scheduler для небольшого 
количества одновременно запущенных экземпляров или Redis + Scheduled Lock
для большого количества экземпляров.