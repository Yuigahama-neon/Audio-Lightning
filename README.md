# Audio Lightning

Веб-платформа для хранения и воспроизведения личной аудиотеки. Проект построен на Java 17+, Spring Boot, Spring Security, Spring Data JPA, Thymeleaf, Bootstrap и PostgreSQL.

## Возможности

- регистрация и вход пользователей;
- хранение паролей через BCrypt;
- роли `ROLE_USER` и `ROLE_ADMIN`;
- загрузка аудио `.mp3`, `.wav`, `.ogg`, `.m4a` до 50 МБ;
- хранение аудиофайлов в `storage/audio`;
- личная аудиотека с поиском по названию и автору;
- просмотр, редактирование и удаление своих аудиозаписей;
- защищённый endpoint `/audio/{id}/stream` для воспроизведения;
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

2. Проверьте настройки подключения в `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/audio_platform
spring.datasource.username=postgres
spring.datasource.password=postgres
```

3. Запустите приложение:

```bash
mvn spring-boot:run
```

4. Откройте сайт:

```text
http://localhost:8080
```

При первом старте создаются роли и администратор:

```text
login: admin
password: admin123
```

Пароль и данные администратора можно изменить через свойства `app.bootstrap.admin-*`.

## Важные настройки

```properties
app.storage.audio-path=storage/audio
app.storage.max-file-size-bytes=52428800
```

## Структура

```text
src/main/java/com/example/audioplatform
├── config
├── controller
├── dto
├── entity
├── repository
└── service

src/main/resources
├── static
│   ├── css
│   └── img
└── templates
    ├── admin
    ├── audio
    ├── auth
    ├── fragments
    └── profile
```

## Основные маршруты

- `/` — главная страница;
- `/register` — регистрация;
- `/login` — вход;
- `/profile` — профиль пользователя;
- `/audio` — личная аудиотека;
- `/audio/upload` — загрузка аудио;
- `/audio/{id}` — карточка аудиозаписи;
- `/audio/{id}/edit` — редактирование аудиозаписи;
- `/audio/{id}/stream` — поток аудио для воспроизведения;
- `/admin` — админ-панель;
- `/admin/users` — управление пользователями;
- `/admin/audio` — список всех аудиозаписей.

## Проверка

```bash
mvn compile
```

Для полной сборки без тестов:

```bash
mvn package -DskipTests
```
