# Game Service

REST-сервис хранения состояния игроков и событий активности. Java 21, Spring Boot, Maven, PostgreSQL. Структуру БД создаёт Liquibase при запуске приложения.

## Запуск

Для запуска через Docker нужны Docker Desktop с Linux-контейнерами и Docker Compose. Из корня проекта:

```powershell
docker compose up --build -d
docker compose ps
docker compose logs -f app
```

API: `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

Остановка: `docker compose down`. Данные сохраняются в томе `postgres_data`. Команда с `-v` удаляет том и данные; для обычной остановки её не используйте.

Для запуска без Docker нужны JDK 21 и локальный PostgreSQL с БД `game_service`, пользователем `postgres` и паролем `postgres`. Настройки находятся в `src/main/resources/application.properties`. Их можно переопределить переменными `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

Если PostgreSQL установлен и пользователь `postgres` существует, создайте БД один раз (команда запросит пароль):

```powershell
psql -h localhost -U postgres -c "CREATE DATABASE game_service;"
```

Сборка без выполнения тестов и запуск приложения:

```powershell
.\mvnw.cmd clean package -DskipTests
.\mvnw.cmd spring-boot:run
```

Maven устанавливать отдельно не требуется: используется Maven Wrapper из проекта. Для первой сборки нужен доступ к репозиториям зависимостей.

Не запускайте приложение из IDE и Compose одновременно на порту 8080.

## Тесты

```powershell
.\mvnw.cmd test
```

Интеграционные тесты создают отдельный PostgreSQL через Testcontainers; Docker должен работать. Автоматический `contextLoads` использует настройки локальной БД из `application.properties`, поэтому для полного запуска тестов также нужна доступная локальная БД. PostgreSQL из Compose не публикует порт 5432 на компьютер и не заменяет эту локальную БД. Основные тесты можно запустить отдельно, без локального PostgreSQL:

```powershell
.\mvnw.cmd "-Dtest=UserServiceTest,UserApiIntegrationTest" test
```

Проверяются создание пользователя, обновление и получение состояния с неизвестными полями, ошибка валидации, запрет смены страны без изменения состояния и аналитический запрос.

## Данные для ручной проверки

Следующие команды предназначены для PowerShell. Выполняйте блоки последовательно в одной сессии после запуска приложения: последующие блоки используют переменные из предыдущих. Демонстрационные UUID отличаются от UUID интеграционных тестов. На новой базе данные ещё отсутствуют: их создают команды PUT и POST ниже.

| UUID | Страна | money |
| --- | --- | --- |
| `10000000-0000-0000-0000-000000000001` | US | 2000 |
| `10000000-0000-0000-0000-000000000002` | US | 2000 |
| `10000000-0000-0000-0000-000000000003` | US | 1000 |
| `10000000-0000-0000-0000-000000000004` | CA | 9000 |

### PUT /users/{userId}/state

Создание четырёх пользователей. Те же JSON можно вставить в Swagger, указав UUID из таблицы.

```powershell
$base = 'http://localhost:8080'
$states = @(
    @{ id = '10000000-0000-0000-0000-000000000001'; body = '{"money":2000,"country":"US","level":17,"inventory":{"sword":2,"potion":5}}' },
    @{ id = '10000000-0000-0000-0000-000000000002'; body = '{"money":2000,"country":"US","level":12}' },
    @{ id = '10000000-0000-0000-0000-000000000003'; body = '{"money":1000,"country":"US","level":3}' },
    @{ id = '10000000-0000-0000-0000-000000000004'; body = '{"money":9000,"country":"CA","level":25}' }
)
foreach ($state in $states) {
    Invoke-RestMethod -Method Put -Uri "$base/users/$($state.id)/state" -ContentType 'application/json' -Body $state.body
}
$userId = '10000000-0000-0000-0000-000000000001'
```

Успешный PUT возвращает HTTP 200 без тела. Повторный PUT полностью заменяет состояние; UUID, исходная страна и время создания сохраняются.

### GET /users/{userId}/state

```powershell
Invoke-RestMethod "$base/users/$userId/state" | ConvertTo-Json -Depth 20
```

Ответ содержит `userId` и `state`. В состоянии первого пользователя должны сохраниться `level` и вложенный `inventory`.

### POST /users/{userId}/activities

```powershell
Invoke-RestMethod -Method Post -Uri "$base/users/$userId/activities" -ContentType 'application/json' -Body '{"activity":42}'
Invoke-RestMethod -Method Post -Uri "$base/users/$userId/activities" -ContentType 'application/json' -Body '{"activity":100}'
```

Успешный POST возвращает HTTP 200 без тела. Каждый повтор команды добавляет отдельное событие.

### GET /users/{userId}/activities

```powershell
$from = '2000-01-01T00:00:00Z'
$to = '2100-01-01T00:00:00Z'
Invoke-RestMethod "$base/users/$userId/activities?from=$from&to=$to" | ConvertTo-Json -Depth 20
```

На первой проверке ожидаются два события: 42 и 100 с серверным временем получения, по возрастанию времени. При повторном добавлении событий список увеличивается.

### GET /users/top

```powershell
Invoke-RestMethod "$base/users/top?country=US&limit=2" | ConvertTo-Json -Depth 20
```

Если в базе только демонстрационные пользователи, результат:

```json
[
  {"userId":"10000000-0000-0000-0000-000000000001","money":2000},
  {"userId":"10000000-0000-0000-0000-000000000002","money":2000}
]
```

Пользователь CA исключён, несмотря на больший баланс. При равных балансах UUID идут по возрастанию. Другие пользователи US в базе тоже участвуют в рейтинге.

### GET /statistics/new-users

```powershell
Invoke-RestMethod "$base/statistics/new-users?country=US&from=$from&to=$to"
```

При наличии только демонстрационных пользователей ответ — число `3`. Повторное сохранение не создаёт нового пользователя. Другие пользователи US в периоде увеличивают результат.

## Правила и ошибки

`money` и `activity` — JSON-целые числа в диапазоне Java `long`; числовые строки и дробные значения отклоняются. `country` — непустая строка из латинских букв; регистр значим, автоматическое изменение регистра не выполняется. Размер тела состояния в UTF-8 — до 10 240 байт. Неизвестные поля сохраняются; порядок ключей и пробелы могут меняться.

В запросе `/users/top` обязательны `country` и целочисленный `limit` от 1 до 100.

`from` и `to` обязательны, задаются в ISO 8601 со смещением. Интервал `[from, to)`; `from < to`.

Обрабатываемые ошибки имеют JSON-формат `{"code":"...","message":"..."}`: некорректный запрос — 400, неизвестный пользователь — 404, смена страны — 409, превышение размера состояния — 413. При отсутствии пользователей указанной страны топ возвращает пустой список. Для существующего пользователя без событий список активностей пустой; для неизвестного пользователя возвращается 404. Отсутствие новых пользователей в периоде даёт число 0.

## Данные и индексы

`users`: UUID — первичный ключ, текущий баланс `BIGINT`, страна, состояние `JSONB`, серверное время создания `TIMESTAMP WITH TIME ZONE`.

`activities`: идентификатор события `BIGINT IDENTITY`, внешний ключ на пользователя, значение `BIGINT`, серверное время получения `TIMESTAMP WITH TIME ZONE`.

- `users(country, money DESC, id ASC)` — топ по стране с требуемой сортировкой.
- `users(country, created_at)` — подсчёт новых пользователей по стране и периоду.
- `activities(user_id, received_at)` — события пользователя за период по времени.

## Известное ограничение

Одновременное первое сохранение одного UUID может привести к конфликту первичного ключа и HTTP 500. Атомарное создание с проверкой неизменности страны пока не реализовано.
