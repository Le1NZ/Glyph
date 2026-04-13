# Нагрузочное тестирование Glyph API

Скрипт: `server/k6/load-test.js`  
Инструмент: **k6** — Go-агент с JS-скриптами, встроенные метрики и thresholds.

---

## 1. Установка k6

```bash
brew install k6          # macOS
```

Проверка:
```bash
k6 version
```

---

## 2. Запуск сервера

```bash
./gradlew :server:run
# Сервер поднимается на http://localhost:8080
```

---

## 3. Получение Yandex OAuth-токена

Токен нужен, чтобы сервер аутентифицировал запросы через `X-Auth-Token`.

**Способ**: запустить приложение на Android-эмуляторе/устройстве, войти в аккаунт,
перехватить токен через Logcat или временный лог в `YandexAuth.kt`:

```kotlin
// Временно добавить в authenticate { ... }
println("TOKEN: ${credential.token}")
```

Или получить напрямую через [Yandex OAuth Debug](https://oauth.yandex.ru/authorize?response_type=token&client_id=<YANDEX_CLIENT_ID>).

---

## 4. Запуск тестов

Скрипт выбирает сценарий через переменную `SCENARIO`.

### Smoke (базовая, 5 пользователей, 1 мин)
```bash
cd server
k6 run -e TOKEN=<твой_токен> -e SCENARIO=smoke k6/load-test.js
```

### Ramp-up (до 200 пользователей за 9 мин)
```bash
k6 run -e TOKEN=<твой_токен> -e SCENARIO=rampup k6/load-test.js
```

### Spike (внезапный рост до 500)
```bash
k6 run -e TOKEN=<твой_токен> -e SCENARIO=spike k6/load-test.js
```

### Soak (100 пользователей 2 часа — проверка утечек памяти)
```bash
k6 run -e TOKEN=<твой_токен> -e SCENARIO=soak k6/load-test.js
```

### Против prod-сервера
```bash
k6 run -e TOKEN=<твой_токен> -e BASE_URL=https://api.glyph.example.com -e SCENARIO=rampup k6/load-test.js
```

---

## 5. Что проверяет каждая итерация

Каждый виртуальный пользователь выполняет полный CRUD-цикл:

1. `GET  /api/v1/notes` — список заметок пользователя
2. `POST /api/v1/notes` — создать заметку
3. `PUT  /api/v1/notes/{id}` — обновить её
4. `DEL  /api/v1/notes/{id}` — удалить

---

## 6. Thresholds (критерии провала)

| Метрика | Порог |
|---------|-------|
| `GET /notes` p95 | < 300 мс |
| `POST /notes` p95 | < 400 мс |
| `PUT /notes/{id}` p95 | < 400 мс |
| `DELETE /notes/{id}` p95 | < 300 мс |
| Доля ошибок | < 1 % |

k6 вернёт exit code `99` если хоть один threshold нарушен — удобно для CI.

---

## 7. Пример вывода

```
✓ GET /notes → 200
✓ POST /notes → 201
✓ PUT /notes/{id} → 200
✓ DELETE /notes/{id} → 204

http_req_duration............: avg=45ms  p(95)=210ms
http_req_failed..............: 0.00%
vus..........................: 200
```
