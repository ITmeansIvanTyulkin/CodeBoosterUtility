# CodeBoosterUtility 🤖

**AI-powered Java code analyzer** — интеллектуальный помощник для анализа Java-кода, генерации тестов, рефакторинга, поиска ошибок и **проверки соответствия кода задачам** с использованием DeepSeek API.

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.org/projects/jdk/17/)
[![DeepSeek](https://img.shields.io/badge/DeepSeek-API-green.svg)](https://deepseek.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://makeapullrequest.com)

---

## 📋 Оглавление

- [Возможности](#-возможности)
- [Новые функции: анализ задач](#-новые-функции-анализ-задач)
- [Как это работает](#-как-это-работает)
- [Технологии](#-технологии)
- [Установка](#-установка)
- [Настройка](#-настройка)
- [Использование](#-использование)
- [Команды](#-команды)
- [Примеры](#-примеры)
    - [Анализ задачи](#пример-1-анализ-задачи)
    - [Анализ требования](#пример-2-анализ-требования)
    - [Генерация тестов](#пример-3-генерация-тестов)
    - [Анализ проекта](#пример-4-анализ-проекта)
- [Структура проекта](#-структура-проекта)
- [Планы по развитию](#-планы-по-развитию)
- [Лицензия](#-лицензия)

---

## 🚀 Возможности

### 🔍 Анализ кода
- **Детальный анализ** Java-классов (качество, баги, SOLID, производительность)
- **Анализ архитектуры** проекта (структура пакетов, паттерны)
- **Поиск code smells** — автоматическое обнаружение проблемных мест

### 📋 Анализ задач (NEW!)
- **Проверка соответствия кода требованиям** — сравнение задачи и реализации
- **Анализ задач из текстовых файлов** — загрузка и анализ требований
- **Произвольные требования** — проверка любого текстового описания
- **Процент соответствия** — количественная оценка выполнения задачи
- **Детальный отчёт** — что реализовано, что отсутствует, рекомендации

### 🧪 Генерация тестов
- **JUnit 5 + Mockito** — автоматическая генерация unit-тестов
- Сохранение тестов в `src/test/java` с правильной структурой пакетов
- Поддержка параметризованных тестов

### 🔧 Рефакторинг и исправление
- **Автоматическое исправление** типичных ошибок
- **Предложения по рефакторингу** с возможностью применения
- Создание бэкапов перед изменениями

### ✨ Создание кода
- **Генерация новых классов** по текстовому описанию
- Автоматическое определение имени класса и пакета

### 💬 Интерактивный режим
- Общение с DeepSeek в контексте вашего проекта
- Загрузка классов в контекст для точных ответов
- Поддержка русского языка

---

## ⚙️ Как это работает
┌─────────────────────────────────────────────────────────────┐
│ ПОЛЬЗОВАТЕЛЬ │
│ ↓ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ MAIN.java (консоль) │ │
│ │ - Приём команд (/test, /fix, /analyze-project) │ │
│ │ - Формирование промптов │ │
│ └─────────────────────────────────────────────────────┘ │
│ ↓ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ ProjectScanner + ContextBuilder │ │
│ │ - Сканирование файлов проекта │ │
│ │ - Загрузка кода в контекст │ │
│ └─────────────────────────────────────────────────────┘ │
│ ↓ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ DeepSeekService │ │
│ │ - HTTP-запрос к DeepSeek API │ │
│ │ - Передача промпта + контекста │ │
│ └─────────────────────────────────────────────────────┘ │
│ ↓ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ DeepSeek API (облако) │ │
│ │ - deepseek-chat / deepseek-coder │ │
│ │ - Анализ кода, генерация тестов, рефакторинг │ │
│ └─────────────────────────────────────────────────────┘ │
│ ↓ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ ВЫВОД В КОНСОЛЬ │ │
│ │ - Структурированный ответ с анализом │ │
│ │ - Сохранение сгенерированных файлов │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘

---

## 🛠 Технологии

| Компонент | Технология | Назначение |
|-----------|------------|------------|
| **Язык** | Java 17 | Основной язык разработки |
| **Сборка** | Maven | Управление зависимостями и сборка |
| **HTTP клиент** | java.net.HttpURLConnection | Чистый HTTP без внешних библиотек |
| **JSON парсинг** | Ручной парсинг | Без внешних зависимостей |
| **AI модель** | DeepSeek (deepseek-chat, deepseek-coder) | Анализ кода, генерация, рефакторинг |
| **Файловая система** | Java NIO (Files.walk) | Сканирование проекта |

---

## 📦 Установка

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

🔧 Настройка
Получение API ключа DeepSeek

Зарегистрируйтесь на platform.deepseek.com
Перейдите в раздел API Keys
Создайте новый ключ
Скопируйте ключ и используйте его как DEEPSEEK_API_KEY

💻 Использование
Запуск
java -jar target/CodeBoosterUtility-1.0-SNAPSHOT.jar /path/to/your/project
Или без аргументов — программа спросит путь:
java -jar target/CodeBoosterUtility-1.0-SNAPSHOT.jar

Алиас для быстрого запуска (macOS/Linux)
Добавьте в ~/.zshrc или ~/.bashrc:
alias ai-helper='java -jar ~/MEGA/Work/CodeBoosterUtility/mainModule/target/CodeBoosterUtility-1.0-SNAPSHOT.jar'

После перезагрузки терминала:
ai-helper ~/my-project

📖 Команды

Команда	                        Описание                            Пример
/structure                      Показать структуру проекта          /structure
/load <класс>	                Загрузить класс в контекст          /load UserService
/analyze <класс>                Детальный анализ класса             /analyze UserService
/analyze-project                Полный анализ всего проекта         /analyze-project
/test <класс>	                Сгенерировать JUnit тесты           /test UserService
/fix <класс>	                Исправить ошибки в классе           /fix UserService
/refactor <класс>               Предложить рефакторинг              /refactor UserService
/create <описание>              Создать новый класс                 /create класс для работы с пользователями
/clear	                        Очистить контекст                   /clear
/exit	                        Выйти из программы                  /exit

📝 Примеры

1. Анализ конкретного класса
🤔 Вы: /analyze UserService

🤖 DeepSeek: 
🔍 Анализ класса UserService:

1. Что делает класс?
   Отвечает за бизнес-логику пользователей: регистрация, поиск, обновление.

2. Потенциальные проблемы:
   - Метод findUser() возвращает null вместо Optional
   - Отсутствует обработка исключений при работе с БД
   - Нет валидации входных параметров

3. Рекомендации:
   - Использовать Optional<User> вместо User
   - Добавить @NotNull аннотации
   - Обернуть вызовы репозитория в try-catch

4. Улучшенный код:
   public Optional<User> findUser(@NotNull String id) {
       try {
           return userRepository.findById(id);
       } catch (DataAccessException e) {
           log.error("Failed to find user: {}", id, e);
           return Optional.empty();
       }
   }

2. Генерация тестов
🤔 Вы: /test UserService

📝 Сгенерированные тесты:
---
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findUser_ShouldReturnUser_WhenExists() {
        // given
        String userId = "123";
        User expectedUser = new User(userId, "John");
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // when
        Optional<User> result = userService.findUser(userId);

        // then
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
    }

    @Test
    void findUser_ShouldReturnEmpty_WhenNotExists() {
        // given
        String userId = "999";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        Optional<User> result = userService.findUser(userId);

        // then
        assertTrue(result.isEmpty());
    }
}
---

💾 Сохранить тесты в /src/test/java/com/example/UserServiceTest.java? (y/n): y
✅ Тесты сохранены!

3. Полный анализ проекта
🤔 Вы: /analyze-project

🔍 ЗАПУСК ПОЛНОГО АНАЛИЗА ПРОЕКТА...

📁 СТРУКТУРА ПРОЕКТА:
src/main/java/com/example/
  ├── UserService.java
  ├── UserRepository.java
  ├── UserController.java
src/test/java/com/example/
  ├── UserServiceTest.java

📄 НАЙДЕНО ФАЙЛОВ: 15

🔬 Сколько файлов проанализировать? (1-15, enter=3): 3

🔬 АНАЛИЗ КОДА (первые 3 файлов):
...
🏗️ АНАЛИЗ АРХИТЕКТУРЫ:
Проект использует классическую трёхслойную архитектуру (Controller → Service → Repository).
Рекомендации: добавить DTO для разделения слоёв, использовать интерфейсы.

🔍 ПОИСК ПРОБЛЕМ В КОДЕ:
1. UserService.java, строка 45: возврат null вместо Optional
2. UserController.java, строка 23: отсутствует валидация входных данных
3. UserRepository.java, строка 12: SQL-инъекция (использовать PreparedStatement)

✅ АНАЛИЗ ПРОЕКТА ЗАВЕРШЁН!

📁 Структура проекта
CodeBoosterUtility/
├── mainModule/
│   ├── pom.xml                     # Maven конфигурация
│   └── src/
│       └── main/
│           └── java/
│               ├── Main.java       # Точка входа, консольный интерфейс
│               ├── builder/
│               │   └── ContextBuilder.java    # Сборка контекста
│               ├── scanner/
│               │   └── ProjectScanner.java    # Сканирование файлов
│               └── service/
│                   └── DeepSeekService.java   # Работа с DeepSeek API
├── .gitignore                      # Игнорируемые файлы
├── LICENSE                         # MIT лицензия
└── README.md                       # Документация

🗺 Планы по развитию

CI/CD интеграция — анализ кода при каждом коммите
Визуализация — веб-интерфейс для отчётов
Поддержка других языков — Python, Go, Kotlin
Интеграция с IntelliJ IDEA — плагин для IDE
Автоматическое создание PR — с исправлениями
Анализ покрытия тестами — интеграция с JaCoCo
Поддержка локальных моделей — Ollama, LM Studio

📄 Лицензия
MIT License. См. файл LICENSE для подробностей.

👤 Автор
Ivan Tyulkin, AQA Java Engineer
GitHub: @ITmeansIvanTyulkin

⭐ Если вам понравился проект
Поставьте звезду на GitHub — это поможет другим найти инструмент!