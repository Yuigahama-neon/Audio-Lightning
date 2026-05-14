# Audio Lightning

Веб-платформа для хранения и воспроизведения аудиофайлов. Проект создан по спецификации из `audio_platform_specification.md`: Java 17+, Spring Boot, Spring Security, Spring Data JPA, Thymeleaf, Bootstrap и PostgreSQL.

## Возможности MVP

- регистрация и вход пользователя;
- хранение паролей через BCrypt;
- роли `ROLE_USER` и `ROLE_ADMIN`;
- загрузка аудио `.mp3`, `.wav`, `.ogg`, `.m4a` до 50 МБ;
- сохранение файлов в `storage/audio`;
- хранение метаданных в PostgreSQL;
- личная аудиотека с поиском и фильтром по жанру;
- просмотр, редактирование и удаление своих аудиозаписей;
- защищённый endpoint `/audio/{id}/stream` для HTML5-плеера;
- админ-панель со списком пользователей и всех аудиозаписей.

## Требования

- Java 17 или новее;
- Maven 3.9+;
- PostgreSQL.

## Быстрый запуск

1. Создайте базу данных:

```sql
CREATE DATABASE audio_platform;
```

2. Проверьте настройки в `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/audio_platform
spring.datasource.username=postgres
spring.datasource.password=postgres
```

3. Запустите приложение:

```bash
mvn spring-boot:run
```

4. Откройте `http://localhost:8080`.

При первом старте создаются жанры, роли и администратор:

```text
login: admin
password: admin123
```

Пароль администратора можно изменить через свойства `app.bootstrap.admin-*`.

## Структура

```text
src/main/java/com/example/audioplatform
├── config
├── controller
├── dto
├── entity
├── repository
└── service

src/main/resources/templates
├── admin
├── audio
├── auth
├── fragments
└── profile
```

## Важные маршруты

- `/` — главная;
- `/register` — регистрация;
- `/login` — вход;
- `/profile` — профиль;
- `/audio` — личная аудиотека;
- `/audio/upload` — загрузка аудио;
- `/audio/{id}` — карточка аудиозаписи;
- `/audio/{id}/stream` — поток аудио;
- `/admin` — админ-панель.
