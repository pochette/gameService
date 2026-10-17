# Game Service

> REST-сервис для хранения состояния игроков, событий активности и аналитики по ним.

![Java](https://img.shields.io/badge/Java_21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4.1-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_17-4169E1?logo=postgresql&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-2962FF?logo=liquibase&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-291A3F?logo=testcontainers&logoColor=white)
![Docker](https://img.shields.io/badge/Docker_Compose-2496ED?logo=docker&logoColor=white)

**Русский** · [English](README.en.md)

## О проекте

Бэкенд игрового сервиса, выполненный по техническому заданию. Клиент хранит произвольное JSON-состояние игрока и шлёт события активности. Сервис отдаёт историю событий за период, топ игроков страны по балансу и число новых пользователей.

**Ключевые решения:**

- **Гибкая схема и быстрая аналитика.** Состояние хранится в `JSONB` целиком, включая неизвестные поля. Баланс и страна вынесены в отдельные колонки с индексами. Фильтрация, сортировка и подсчёт выполняются в PostgreSQL, а не в памяти.
- **Устранена гонка при конкурентной записи.** Проверка «существует ли пользователь» и вставка заменены одним атомарным `INSERT ... ON CONFLICT DO UPDATE`. Поведение подтверждено интеграционным тестом.
- **Единый формат ошибок.** Все ошибки возвращаются как `{"code","message"}` с корректными HTTP-статусами 400/404/409/413.
- **Воспроизводимое окружение.** Docker Compose поднимает сервис одной командой. Схему БД создают миграции Liquibase. Тесты сами запускают PostgreSQL через Testcontainers.

## Стек

| Область | Технологии |
| --- | --- |
| Язык и фреймворк | Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Validation) |
| Данные | PostgreSQL 17 (JSONB), Liquibase |
| Документация API | springdoc-openapi, Swagger UI |
| Тесты | JUnit 5, Mockito, Testcontainers, Postman |
| Сборка и запуск | Maven Wrapper, Docker, Docker Compose |

## Архитектура

Пакеты разделены по предметным областям, внутри каждой — слои controller → service → repository.

```text
src/main/java/ru/burdak/game
├── user/         # состояние игрока, топ по стране
├── activity/     # события активности
├── statistic/    # аналитика: новые пользователи
└── common/       # обработка ошибок, валидация, настройки JSON
src/main/resources/db/changelog   # миграции Liquibase
```

## Быстрый старт

### Docker Compose

Нужны Docker Desktop и свободный порт 8080.

```bash
docker compose up --build -d
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

Остановка: `docker compose down`. Флаг `-v` дополнительно удаляет том с данными.

### Локально

Нужны JDK 21 и PostgreSQL с базой `game_service` на `localhost:5432` (пользователь и пароль — `postgres`).

```bash
./mvnw clean package -DskipTests
java -jar target/game-0.0.1-SNAPSHOT.jar
```

Подключение переопределяется переменными `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` и `SPRING_DATASOURCE_PASSWORD`.

## API

| Метод и путь | Назначение | Ответ 200 |
| --- | --- | --- |
| `PUT /users/{userId}/state` | Создать или полностью заменить состояние | Без тела |
| `GET /users/{userId}/state` | Получить состояние | `userId`, `state` |
| `POST /users/{userId}/activities` | Записать событие | Без тела |
| `GET /users/{userId}/activities?from=&to=` | События за период `[from, to)` | Массив `userId`, `activity`, `receivedAt` |
| `GET /users/top?country=&limit=` | Топ страны по балансу | Массив `userId`, `money` |
| `GET /statistics/new-users?country=&from=&to=` | Число новых пользователей за период | Число |

Ошибки: **400** — некорректный запрос, **404** — `USER_NOT_FOUND`, **409** — `COUNTRY_IMMUTABLE`, **413** — `STATE_TOO_LARGE`.

<details>
<summary><b>Подробный контракт</b></summary>

- `userId` — UUID. Состояние — JSON-объект с обязательными полями `money` (`long`) и `country` (1–255 латинских букв, регистр значим).
- Размер состояния — до 10 240 байт в UTF-8. Неизвестные поля, включая вложенные, сохраняются.
- Первый PUT создаёт пользователя. Повторный PUT заменяет состояние целиком, сохраняя страну и время создания. Попытка сменить страну отклоняется без изменения данных.
- `activity` — обязательное целое `long`. Каждый POST создаёт отдельное событие с серверным временем получения.
- `from` и `to` — ISO 8601 со смещением, `from < to`. Знак `+` в URL кодируется как `%2B`.
- `limit` — от 1 до 100. Сортировка топа: `money` по убыванию, при равенстве — UUID по возрастанию.
- Пустые выборки возвращают `[]`, подсчёт — `0`. Повторный PUT не увеличивает число новых пользователей.

</details>

<details>
<summary><b>Пример сценария (PowerShell)</b></summary>

```powershell
$base = 'http://localhost:8080'
$id = [guid]::NewGuid().ToString()
$from = '2000-01-01T00:00:00Z'
$to = '2100-01-01T00:00:00Z'

Invoke-RestMethod -Method Put -Uri "$base/users/$id/state" `
    -ContentType 'application/json' `
    -Body '{"money":1500,"country":"US","level":17,"inventory":{"sword":2}}'
Invoke-RestMethod "$base/users/$id/state" | ConvertTo-Json -Depth 20

Invoke-RestMethod -Method Post -Uri "$base/users/$id/activities" `
    -ContentType 'application/json' -Body '{"activity":42}'
Invoke-RestMethod "$base/users/$id/activities?from=$from&to=$to" | ConvertTo-Json -Depth 20

Invoke-RestMethod "$base/users/top?country=US&limit=10" | ConvertTo-Json -Depth 20
Invoke-RestMethod "$base/statistics/new-users?country=US&from=$from&to=$to"
```

</details>

## Хранение данных

| Таблица | Колонки |
| --- | --- |
| `users` | `id UUID` PK, `money BIGINT`, `country VARCHAR(255)`, `state JSONB`, `created_at TIMESTAMPTZ` |
| `activities` | `id BIGINT IDENTITY` PK, `user_id UUID` FK → `users.id`, `activity BIGINT`, `received_at TIMESTAMPTZ` |

| Индекс | Назначение |
| --- | --- |
| `users(country, money DESC, id ASC)` | Топ по стране сразу в нужном порядке |
| `users(country, created_at)` | Новые пользователи страны за период |
| `activities(user_id, received_at)` | События пользователя за период |

### Конкурентная запись

При тестировании обнаружилась гонка. Два одновременных первых PUT одного UUID оба видели, что записи нет, и оба пытались её создать. Транзакция вокруг «проверить, затем сохранить» проблему не решала.

Решение — атомарный UPSERT `INSERT ... ON CONFLICT (id) DO UPDATE` с условием на совпадение страны. Конфликт разрешает сама БД, баланс и JSON обновляются согласованно. Тест `shouldSaveSameUserConcurrently` отправляет два параллельных PUT и проверяет, что итоговое состояние целиком совпадает с одним из запросов.

## Тестирование

```bash
# Юнит- и интеграционные тесты (PostgreSQL поднимается в Testcontainers, нужен Docker)
./mvnw "-Dtest=UserServiceTest,UserApiIntegrationTest" test

# Полный набор (contextLoads дополнительно требует локальную БД)
./mvnw test
```

- **JUnit + Testcontainers:** 11 тестов — 4 юнит-теста, 6 интеграционных, 1 проверка контекста. Покрыты создание, замена состояния с неизвестными полями, валидация, запрет смены страны, топ и конкурентное сохранение.
- **Postman:** [коллекция](postman/game-service.postman_collection.json) из 28 запросов с автопроверками всех шести эндпоинтов, включая негативные сценарии (400/404/409/413).

## Объём и ограничения

Реализована вся обязательная часть ТЗ. Сознательно не входят в объём:

- пагинация событий и очистка старой статистики;
- отдельные автотесты на каждую ветку ошибок: часть сценариев проверяется только Postman-коллекцией;
- необязательная часть с оценкой нагрузки.

## Автор

**Андрей Бурдак** — [GitHub](https://github.com/pochette) · [Telegram @burdak_aa](https://t.me/burdak_aa) · [burdak.andrew@gmail.com](mailto:burdak.andrew@gmail.com)
