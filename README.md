# Агрегатор java вакансий.
## Описание:

Система запускается по расписанию - раз в минуту.  Период запуска указывается в настройках - app.properties.

Первый сайт - career.habr.com.
Работа идёт с разделом по Java вакансиям - https://career.habr.com/vacancies/java_developer.
Программа считывает все вакансии относящиеся к Java и записывает их в базу.

Доступ к интерфейсу реализован через REST API.

## Расширения:

 - В проект можно добавить новые сайты без изменения кода.

 - В проекте можно сделать параллельный парсинг сайтов.

## В проекте используются:
 - Maven 
 - Jacoco 
 - Checkstyle
