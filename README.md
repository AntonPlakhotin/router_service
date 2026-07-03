# Router Service

## Описание

Router Service — входная точка системы. Все HTTP-запросы от клиента сначала поступают в данный сервис. Он выполняет проверку JWT-токена, извлекает идентификатор пользователя и перенаправляет запросы в Chat Service.

Сервис не хранит данные и не взаимодействует с базой данных напрямую.

---

## Основные задачи

- Проверка JWT-токена.
- Извлечение идентификатора пользователя.
- Авторизация запросов.
- Маршрутизация запросов в Chat Service.
- Возврат HTTP-ответов клиенту.

---

## Архитектура

```
Клиент
    │
    ▼
Router Service
    │
    ▼
Chat Service
```

---

## Основные эндпоинты

| Метод | Endpoint | Описание |
|--------|----------|----------|
| GET | /api/router/chats | Получение списка чатов |
| GET | /api/router/chat/{id} | Получение информации о чате |
| POST | /api/router/chat/create | Создание нового чата |
| POST | /api/router/chat/write | Отправка сообщения |
| POST | /api/router/chat/setPrompt | Установка промпта |
| PUT | /api/router/chat/rename | Переименование чата |
| DELETE | /api/router/chat/delete/{id} | Удаление чата |
| GET | /api/router/chat/{id}/messages | Получение истории сообщений |

---

## Используемые технологии

- Java 21
- Spring Boot
- Spring Web
- Lombok
- Maven

---

## Взаимодействие

Router Service взаимодействует только с Chat Service.

```
Client
      │
      ▼
Router Service
      │
      ▼
Chat Service
```

---
## Ответственность сервиса

- проверка JWT;
- получение userId;
- перенаправление запросов;
- возврат HTTP-ответов.
