# CodeBoosterUtility 🤖

**AI-powered Java code analyzer** — интеллектуальный помощник для анализа Java-кода, генерации тестов, рефакторинга, поиска ошибок и проверки соответствия кода задачам с использованием DeepSeek API.

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.org/projects/jdk/17/)
[![DeepSeek](https://img.shields.io/badge/DeepSeek-API-green.svg)](https://deepseek.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://makeapullrequest.com)

---

## Содержание

- [Возможности](#возможности)
- [Анализ задач](#анализ-задач)
- [Как это работает](#как-это-работает)
- [Технологии](#технологии)
- [Установка](#установка)
- [Настройка](#настройка)
- [Использование](#использование)
- [Команды](#команды)
- [Примеры](#примеры)
- [Структура проекта](#структура-проекта)
- [Планы по развитию](#планы-по-развитию)
- [Лицензия](#лицензия)
- [Автор](#автор)

---

## Возможности

### 🔍 Анализ кода
- Детальный анализ Java-классов (качество, баги, SOLID, производительность)
- Анализ архитектуры проекта (структура пакетов, паттерны)
- Поиск code smells — автоматическое обнаружение проблемных мест

### 📋 Анализ задач
- Проверка соответствия кода требованиям — сравнение задачи и реализации
- Анализ задач из текстовых файлов
- Произвольные требования
- Процент соответствия и детальный отчёт

### 🧪 Генерация тестов
- JUnit 5 + Mockito — автоматическая генерация unit-тестов
- Сохранение тестов в src/test/java

### 🔧 Рефакторинг и исправление
- Автоматическое исправление типичных ошибок
- Предложения по рефакторингу с возможностью применения
- Создание бэкапов перед изменениями

### ✨ Создание кода
- Генерация новых классов по текстовому описанию

### 💬 Интерактивный режим
- Общение с DeepSeek в контексте вашего проекта
- Загрузка классов в контекст для точных ответов

---

## Анализ задач

| Сценарий               | Как помогает |
|------------------------|--------------|
| QA проверка            | Автоматическая проверка реализации требований |
| Code Review            | Оценка соответствия PR задаче |
| Документирование       | Отчёты о покрытии требований |
| Аттестация             | Проверка выполнения всех пунктов задачи |

---

## Как это работает
ПОЛЬЗОВАТЕЛЬ
↓
MAIN.java (консоль)

Приём команд (/test, /fix, /analyze-project)
Формирование промптов
↓
ProjectScanner + ContextBuilder
Сканирование файлов проекта
Загрузка кода в контекст
↓
DeepSeekService
HTTP-запрос к DeepSeek API
Передача промпта + контекста
↓
DeepSeek API (облако)
deepseek-chat / deepseek-coder
Анализ кода, генерация тестов, рефакторинг
↓
ВЫВОД В КОНСОЛЬ
Структурированный ответ с анализом
Сохранение сгенерированных файлов

---

## Технологии

| Компонент          | Технология                                  | Назначение |
|--------------------|---------------------------------------------|------------|
| Язык               | Java 17                                     | Основной язык разработки |
| Сборка             | Maven                                       | Управление зависимостями и сборка |
| HTTP клиент        | java.net.HttpURLConnection                  | Чистый HTTP без внешних библиотек |
| JSON парсинг       | Ручной парсинг                              | Без внешних зависимостей |
| AI модель          | DeepSeek (deepseek-chat, deepseek-coder)    | Анализ кода, генерация, рефакторинг |
| Файловая система   | Java NIO (Files.walk)                       | Сканирование проекта |

---

## Установка

### 1. Клонировать репозиторий

```bash
git clone https://github.com/ITmeansIvanTyulkin/CodeBoosterUtility.git
cd CodeBoosterUtility

### 2. Собрать проект
mvn clean package

### 3. Настроить API ключ
export DEEPSEEK_API_KEY="sk-ваш_ключ_от_deepseek"
Или добавьте в ~/.zshrc / ~/.bash_profile:
echo 'export DEEPSEEK_API_KEY="sk-ваш_ключ_от_deepseek"' >> ~/.zshrc
source ~/.zshrc

Настройка
Получение API ключа DeepSeek

Зарегистрируйтесь на platform.deepseek.com
Перейдите в раздел API Keys
Создайте новый ключ
Скопируйте ключ и используйте его как DEEPSEEK_API_KEY
Настройка прокси (если нужно)

В файле Main.java измените параметры:
private static final String PROXY_HOST = "ваш_прокси_хост";
private static final int PROXY_PORT = 8080;
private static final boolean USE_PROXY = true;

Использование
Запуск
java -jar target/CodeBoosterUtility-1.0-SNAPSHOT.jar /path/to/your/project
Или без аргументов — программа спросит путь:
java -jar target/CodeBoosterUtility-1.0-SNAPSHOT.jar

Алиас для быстрого запуска (macOS/Linux)
Добавьте в ~/.zshrc или ~/.bashrc:
alias ai-helper='java -jar ~/MEGA/Work/CodeBoosterUtility/mainModule/target/CodeBoosterUtility-1.0-SNAPSHOT.jar'

После перезагрузки терминала:
ai-helper ~/my-project

Команды

Команда	                            Описание	                        Пример
/structure	                        Показать структуру проекта          /structure
/load <класс>	                    Загрузить класс в контекст	        /load UserService
/analyze <класс>	                Детальный анализ класса	            /analyze UserService
/analyze-project	                Полный анализ всего проекта	        /analyze-project
/analyze-task <файл>	            Анализ задачи из текстового файла	/analyze-task task.txt
/analyze-requirement <текст>	    Анализ произвольного требования	    /analyze-requirement "Добавить метод"
/test <класс>	                    Сгенерировать JUnit тесты	        /test UserService
/fix <класс>	                    Исправить ошибки в классе	        /fix UserService
/refactor <класс>	                Предложить рефакторинг	            /refactor UserService
/create <описание>	                Создать новый класс	                /create класс для работы с пользователями
/clear	                            Очистить контекст	                /clear
/exit	                            Выйти из программы	                /exit

Примеры

Пример 1: Анализ требования
🤔 Вы: /analyze-requirement "Реализовать метод findUser, который возвращает пользователя по ID. Если пользователь не найден, возвращать Optional.empty()."

🔍 Анализирую соответствие кода требованию...

=== АНАЛИЗ СООТВЕТСТВИЯ ===

1. Краткий анализ: Код частично соответствует требованию.

2. Соответствие требованиям:
   | Требование | Статус | Комментарий |
   |------------|--------|-------------|
   | Метод findUser | ✅ | Реализован в UserService.java |
   | Возврат Optional | ❌ | Возвращает null |

3. Общий процент соответствия: 60%

4. Рекомендации:
   public Optional<User> findUser(String id) {
       return Optional.ofNullable(userRepository.findById(id));
   }

Пример 2: Анализ задачи из файла

Создайте файл task.txt:
Задача: Реализовать регистрацию пользователя
Требования:
1. Валидация email
2. Хеширование пароля
3. Сохранение в БД

🤔 Вы: /analyze-task task.txt

=== РЕЗУЛЬТАТ ===
✅ Валидация email: реализована
⚠️ Хеширование пароля: используется MD5 (рекомендуется BCrypt)
✅ Сохранение в БД: реализовано

📊 ИТОГО: 75% соответствия

Пример 3: Генерация тестов
🤔 Вы: /test UserService

📝 Сгенерированные тесты:
---
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
...

💾 Сохранить тесты в /src/test/java/com/example/UserServiceTest.java? (y/n): y
✅ Тесты сохранены!

Пример 4: Полный анализ проекта

🤔 Вы: /analyze-project

🔍 ЗАПУСК ПОЛНОГО АНАЛИЗА ПРОЕКТА...

📁 СТРУКТУРА ПРОЕКТА:
src/main/java/com/example/
  ├── UserService.java
  ├── UserRepository.java

📄 НАЙДЕНО ФАЙЛОВ: 15

🔬 Сколько файлов проанализировать? (1-15, enter=3): 3

🏗️ АНАЛИЗ АРХИТЕКТУРЫ:
Проект использует трёхслойную архитектуру...

🔍 ПОИСК ПРОБЛЕМ В КОДЕ:
1. UserService.java: возврат null вместо Optional

✅ АНАЛИЗ ПРОЕКТА ЗАВЕРШЁН!

Структура проекта
CodeBoosterUtility/
├── mainModule/
│   ├── pom.xml
│   └── src/
│       └── main/
│           └── java/
│               ├── Main.java
│               ├── builder/
│               │   └── ContextBuilder.java
│               ├── scanner/
│               │   └── ProjectScanner.java
│               └── service/
│                   └── DeepSeekService.java
├── .gitignore
├── LICENSE
└── README.md

Планы по развитию

Jira интеграция — автоматическая загрузка задач по ID
GitHub Actions — автоматический анализ при создании PR
Визуализация — веб-интерфейс для отчётов
Поддержка других языков — Python, Go, Kotlin
Интеграция с IntelliJ IDEA — плагин для IDE
Поддержка локальных моделей — Ollama, LM Studio

Лицензия

MIT License. См. файл LICENSE для подробностей.

Автор

Ivan Tyulkin
AQA Java Engineer
GitHub: @ITmeansIvanTyulkin

Звезда проекту

Если вам понравился проект, поставьте звезду на GitHub!