# UserDepositeService

Сервис для управления пользовательскими депозитами с использованием Spring Boot, PostgreSQL и Elasticsearch.
Это ветка с транзакциями для идемпотентности операций по переводам (не путайте с транзакциями БД).

## Требования

- Java 21 и Maven 3.9.x (опционально)
- Docker + Docker Compose (Docker Desktop будет достаточно) | Kubernetes кластер

## Настройка

1. Создайте `.env` файл в корне проекта:
    ```properties
    DB_URL=jdbc:postgresql://host.docker.internal:5432/user_deposite_db
    DB_USER=ваш_postgres_юзер
    DB_PASSWORD=ваш_postgres_пароль
    ES_URIS=http://host.docker.internal:9200
    ES_PASSWORD=ваш_elastic_пароль
    JWT_SECRET=ваш_jwt_секрет
    ```

2. Быстрый запуск всего сразу с `docker-compose` (Docker-образ будет собран из `Dockerfile`, если отсутствует):
    ```bash
    docker-compose up -d
    ```

3. Запуск в кластере Kubernetes:
    - Создайте кластер Kubernetes, установите `kubectl` и подключитесь к кластеру.
      Это можно сделать за 1 шаг, если у вас установлен `Docker Desktop` просто активируйте Kubernetes `Kubeadm` в
      настройках.
    - Создайте Docker-образ любым из указанных ниже способов:
   ```bash
   mvn clean package -DskipTests -Pdocker-image
   ```
   ```bash
   docker build -t user-deposit-service:0.0.1-SNAPSHOT .
   ```

    - Создайте файл в папке проекта `k8s/user-deposit-secrets.yaml`:
   ```yaml
    apiVersion: v1
    kind: Secret
    metadata:
      name: user-deposit-secrets
    type: Opaque
    stringData:
      DB_USER: "ваш_postgres_юзер"
      DB_PASSWORD: "ваш_postgres_пароль"
      ES_PASSWORD: "ваш_elastic_пароль"
      JWT_SECRET: "ваш_jwt_секрет"
    ```

    - Если у вас не очень мощный процессор отрегулируйте настройки в `k8s/user-deposit-app-deployment.yaml`:
   ```yaml
    resources:
      requests:
        cpu: 100m
      limits:
        cpu: 500m
    metrics:
      - type: Resource
        resource:
          name: cpu
        target:
          type: AverageValue
          averageValue: 10m
    ```

    - Находясь в корне проекта выполните команду:
      ```bash
      kubectl apply -f ./k8s/
      ```

4. Для запуска приложения без `Dockerfile`:

   Создайте файл `src/main/resources/secret/secret.properties`.
   `JWT_SECRET` - необязательный дополнительный параметр,
   если не задать будет использоваться дефолтное значение, но это не безопасно.
   Можно также задать эти свойства как переменные окружения в конфигурации запуска IDEA.

   ```properties
    DB_URL=jdbc:postgresql://localhost:5432/user_deposite_db
    DB_USER=ваш_postgres_юзер
    DB_PASSWORD=ваш_postgres_пароль
    ES_URIS=http://localhost:9200
    ES_PASSWORD=ваш_elastic_пароль
    JWT_SECRET=ваш_jwt_секрет
    ```
   Можете сами создать PostreSQL и Elasticsearch БД или использовать
    ```bash
    docker-compose-no-jar up -d
    ```
   запустит только БД PostreSQL и Elasticsearch в Docker.

5. Сборка и запуск приложения без контейнера
   (используйте флаг `-Pdocker-image` если хотите собирать Docker-образ с помощью Maven):
    - сборка и запуск тестов
        ```bash
        mvn clean package
        ```
    - быстрая сборка с пропуском тестов
      ```bash
      mvn clean package -DskipTests
      ```
    - запуск контейнера
      ```bash
      docker run --env-file .env --name user_deposit_app --restart unless-stopped -p 8080:8080 -d user-deposit-service:0.0.1-SNAPSHOT
      ```
    - запуск jar-файла приложения без контейнера
      ```bash
      java -jar target/user-deposit-service-0.0.1-SNAPSHOT.jar
      ```

6. Сборка и запуск приложения отдельно в контейнере с помощью Dockerfile без Java и Maven в системе:
    - сборка docker-образа
      ```bash
      docker build -t user-deposit-service:0.0.1-SNAPSHOT .
      ```
    - запуск контейнера
      ```bash
      docker run --env-file .env --name user-deposit-app --restart unless-stopped -p 8080:8080 -d user-deposit-service:0.0.1-SNAPSHOT
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