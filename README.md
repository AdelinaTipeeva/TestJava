Описание проекта
Зависимости
Для реализации требуемого в задании микросервиса будем использовать Spring Boot 3.3.1 со следующими зависимостями:
•	Spring Web (spring-boot-starter-web) – для создания RESTful сервиса;
•	Spring Data JPA (spring-boot-starter-data-jpa) – для работы с базой данных;
•	Validation (spring-boot-starter-validation) – для валидации данных входящих запросов;
•	Lombok – для упрощения кода;
•	PostrgeSQLDriver – драйвер для БД PostrgeSQL;
•	Flyway Migration (flyway-core, flyway-database-postgresql) – миграции схемы БД.
Также добавим зависимости для написания модульных и интеграционных тестов для требуемых в задании методов:
•	Testing (spring-boot-starter-test) – JUnit + Mockito;
•	Testcontainers – контейнеры для поднятия реальной чистой БД для интеграционных тестов.

Реализация
Схема БД описывается одной таблицей – таблицей хранимых файлов: Create Table. В ней название файлов сделали уникальным, чтобы у каждого файла было неповторяющееся имя.
Класс сущности файла описывается классом: File
Для данного класса введем DTO-классы (записи) FileDto и NewFileIdResponse.
FileDto - для приема/передачи данных файла по запросам получения/сохранения файла от клиентов сервиса
Для полей названия, а также даты и времени сохранения файла добавлены ограничения на валидность данных. Формат даты – ГГГГ.ММ.ДД ЧЧ:ММ:СС. Поля описания и собственно данных файла могут быть пустыми (например, файл нулевой длины).
NewFileIdResponse – для отправки клиенту id сохраненного файла в случае успеха операции добавления нового файла
Здесь добавлен метод для поиска файлов по названию. Он будет использоваться для нахождения уже существующего файла с данным именем при сохранении нового файла.
Добавим интерфейс для сервиса работы с файлами. У сервиса будет два метода: получение файла по id (getFile) и сохранение нового файла по полученным от клиента данным (saveFile):
•	getFile – возвращает данные файла для отправки клиенту, если файл с указанным id найден;
•	saveFile – возвращает id (обернутый в структуру DTO) только если файл с указанным именем еще не существует.
Реализация интерфейса описывается классом FileServiceImpl (см. файл FileServiceImpl.java).

Класс-контроллер FileController (файл FileController.java) с базовым путем /api/v1/files (далее base_uri) реализует требуемые в задании методы:
•	getFile – получение файла по его id (метод GET, путь <base_uri>/{id}, где id - число), если таковой существует. Иначе возвращается ответ 404 Not Found;
•	saveFile – создание файла с его атрибутами (метод POST, путь <base_uri>). Файл сохраняется, если данные от клиента пришли валидные и файл с таким именем еще не существует в БД. Если файл уже существует, возвращается описание ошибки со статусом 409 Conflict. Если данные имеют некорректный формат (например, неверный формат даты и времени) или значение (например, пустое название файла), то возвращается описание ошибки со статусом 400 Bad Request. Описание ошибки возвращается в виде JSON problem details (RFC 9457) с указанием описания ошибок в поле errors.

Тестирование
Модульное тестирование
Модульные тесты требуемых в задании методов реализованы в классе FileControllerTest.
Модульные тесты используют mock-объекты для внедрения в тестируемый контроллер. Методы тестов:
•	getFile_FileIdExists_ReturnsFile – тест получения данных файла по существующему id;
•	getFile_FileIdDoesNotExist_ReturnsNotFound – тест возвращения статуса 404 при запросе несуществующего файла (по id);
•	saveFile_ValidData_ReturnsId – тест сохранения файла с валидными данными и возвращения id со статусом 201;
•	saveFile_DuplicateTitle_ReturnsProblemDetail – тест возвращения статуса 409 и описания ошибки при запросе сохранения файла с уже существующем названием в БД;
•	saveFile_InvalidDto_ReturnsProblemDetail – тест возвращения статуса 400 и описания ошибки при запросе сохранения файла с некорректными данными.

Интеграционное тестирование
Тесты описаны в классе FileControllerIntegrationTest.
Для интеграционных тестов будем использовать реальную БД postres. Для удобства и «чистого» тестирования используем фреймворк testcontainers, который будет поднимать чистую БД при запуске тестов в docker-контейнере. Для выполнения данных тестов требуется, чтобы сервис (демон) docker был запущен.
Методы тестов имеют такие же названия и назначения (с учетом, что теперь выполняются реальные запросы с реальными бинами в контексте приложения). Кроме того добавлен методт-тест:
•	saveFile_InvalidDateFormat_ReturnsProblemDetail – тест возвращения статуса 400 и описания ошибки при запросе сохранения файла с невалидным форматом даты. Данный сценарий мы не могли протестировать в модульных тестах методов, так как валидация и конвертация JSON происходит до вызова метода контроллера.

Скриншоты работы тестов можно посмотреть в файле Readme.docx
