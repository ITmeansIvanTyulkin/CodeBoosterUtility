import builder.ContextBuilder;
import service.DeepSeekService;
import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;
import java.util.List;
import java.util.regex.*;

public class Main {

    private static final String DEEPSEEK_TOKEN = System.getenv("DEEPSEEK_API_KEY");
    private static final String PROXY_HOST = "18.199.183.77";
    private static final int PROXY_PORT = 49232;
    private static final boolean USE_PROXY = false;

    private static DeepSeekService deepSeek;
    private static ContextBuilder context;
    private static Path projectPath;

    public static void main(String[] args) {

        // ========== ПОЛУЧАЕМ ПУТЬ К ПРОЕКТУ ==========
        if (args.length > 0) {
            projectPath = Paths.get(args[0]);
            System.out.println("📁 Анализируем проект: " + projectPath);
        } else {
            System.out.print("📁 Введите путь к проекту: ");
            Scanner pathScanner = new Scanner(System.in);
            String path = pathScanner.nextLine().trim();
            projectPath = path.isEmpty() ? Paths.get(System.getProperty("user.dir")) : Paths.get(path);
            System.out.println("📁 Используем: " + projectPath);
        }
        // ============================================

        // Проверяем наличие API ключа
        if (DEEPSEEK_TOKEN == null || DEEPSEEK_TOKEN.isEmpty()) {
            System.err.println("❌ Ошибка: Не задан API ключ DeepSeek.");
            System.err.println("Установите переменную окружения DEEPSEEK_API_KEY");
            System.err.println("Или добавьте токен в код (не рекомендуется для публичного репозитория)");
            return;
        }

        // Инициализация DeepSeek
        if (USE_PROXY) {
            deepSeek = new DeepSeekService(DEEPSEEK_TOKEN, PROXY_HOST, PROXY_PORT);
            System.out.println("✅ DeepSeek готов (прокси: " + PROXY_HOST + ":" + PROXY_PORT + ")");
        } else {
            deepSeek = new DeepSeekService(DEEPSEEK_TOKEN);
            System.out.println("✅ DeepSeek готов (без прокси)");
        }

        // Инициализация контекста
        context = new ContextBuilder(projectPath.toString());

        System.out.println("\n=== 🤖 DeepSeek AI Assistant ===");
        System.out.println("📁 Проект: " + projectPath);
        System.out.println("----------------------------------------");
        System.out.println("💡 Команды:");
        System.out.println("  /load <класс>        - загрузить класс в контекст");
        System.out.println("  /test <класс>        - сгенерировать и сохранить JUnit тесты");
        System.out.println("  /fix <класс>         - найти и исправить ошибки в классе");
        System.out.println("  /refactor <класс>    - предложить и применить рефакторинг");
        System.out.println("  /create <описание>   - создать новый класс");
        System.out.println("  /analyze <класс>     - детальный анализ конкретного класса");
        System.out.println("  /analyze-project     - полный анализ всего проекта");
        System.out.println("  /structure           - показать структуру проекта");
        System.out.println("  /analyze-task <файл> - анализ задачи из файла");
        System.out.println("  /analyze-requirement <текст> - анализ произвольного требования");
        System.out.println("  /clear               - очистить контекст");
        System.out.println("  /exit                - выход");
        System.out.println("----------------------------------------");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\n🤔 Вы: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("/exit")) {
                System.out.println("👋 До свидания!");
                break;
            }

            if (input.equalsIgnoreCase("/structure")) {
                showStructure();
                continue;
            }

            if (input.equalsIgnoreCase("/clear")) {
                context.clearContext();
                System.out.println("🧹 Контекст очищен");
                continue;
            }

            if (input.startsWith("/load ")) {
                String className = input.substring(6).trim();
                loadClass(className);
                continue;
            }

            if (input.startsWith("/test ")) {
                String className = input.substring(6).trim();
                generateAndSaveTests(className);
                continue;
            }

            if (input.startsWith("/fix ")) {
                String className = input.substring(5).trim();
                fixCode(className);
                continue;
            }

            if (input.startsWith("/refactor ")) {
                String className = input.substring(10).trim();
                refactorCode(className);
                continue;
            }

            if (input.startsWith("/create ")) {
                String description = input.substring(8).trim();
                createNewClass(description);
                continue;
            }

            if (input.startsWith("/analyze ")) {
                String className = input.substring(9).trim();
                analyzeClass(className);
                continue;
            }

            if (input.equalsIgnoreCase("/analyze-project")) {
                analyzeProject();
                continue;
            }

            // ========== НОВЫЕ КОМАНДЫ ДЛЯ АНАЛИЗА ЗАДАЧ ==========
            if (input.startsWith("/analyze-task ")) {
                String taskPath = input.substring(14).trim();
                analyzeTaskFromFile(taskPath);
                continue;
            }

            if (input.startsWith("/analyze-requirement ")) {
                String requirement = input.substring(21).trim();
                analyzeRequirement(requirement);
                continue;
            }

            if (!input.trim().isEmpty()) {
                // Обычный вопрос
                askQuestion(input);
            }
        }

        scanner.close();
    }

    // ========== НОВАЯ ФУНКЦИЯ: АНАЛИЗ ВСЕГО ПРОЕКТА ==========

    /**
     * Полный анализ всего проекта
     */
    private static void analyzeProject() {
        System.out.println("\n🔍 ЗАПУСК ПОЛНОГО АНАЛИЗА ПРОЕКТА...\n");

        try {
            // 1. Показываем структуру проекта
            System.out.println("📁 СТРУКТУРА ПРОЕКТА:");
            System.out.println(context.getFullContext());

            // 2. Получаем список всех Java-файлов
            List<String> allFiles = context.getAllJavaFiles();
            System.out.println("\n📄 НАЙДЕНО ФАЙЛОВ: " + allFiles.size());

            if (allFiles.isEmpty()) {
                System.out.println("⚠️ Java-файлы не найдены. Проверьте путь к проекту.");
                return;
            }

            // 3. Спрашиваем, сколько файлов анализировать
            System.out.print("\n🔬 Сколько файлов проанализировать? (1-" + allFiles.size() + ", enter=3): ");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine().trim();
            int filesToAnalyze;
            try {
                filesToAnalyze = answer.isEmpty() ? 3 : Math.min(Integer.parseInt(answer), allFiles.size());
            } catch (NumberFormatException e) {
                filesToAnalyze = 3;
            }

            // 4. Анализируем выбранные файлы
            System.out.println("\n🔬 АНАЛИЗ КОДА (первые " + filesToAnalyze + " файлов):");
            int analyzed = 0;
            for (String fileName : allFiles) {
                if (analyzed >= filesToAnalyze) break;

                System.out.println("\n--- Анализ: " + fileName + " ---");
                String code = context.readFileContent(fileName);

                System.out.print("🤖 DeepSeek анализирует... ");
                String analysis = deepSeek.deepAnalyze(code, fileName);
                System.out.println("\n" + analysis);

                analyzed++;

                // Небольшая задержка, чтобы не перегружать API
                if (analyzed < filesToAnalyze) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            if (allFiles.size() > filesToAnalyze) {
                System.out.println("\n... и ещё " + (allFiles.size() - filesToAnalyze) + " файлов.");
                System.out.println("💡 Используйте /analyze <класс> для анализа конкретного файла.");
            }

            // 5. Анализ архитектуры
            System.out.println("\n🏗️ АНАЛИЗ АРХИТЕКТУРЫ ПРОЕКТА:");
            System.out.print("🤖 DeepSeek анализирует архитектуру... ");
            String structure = context.getFullContext();
            String archAnalysis = deepSeek.analyzeArchitecture(structure);
            System.out.println("\n" + archAnalysis);

            // 6. Поиск проблем в коде (code smells)
            System.out.println("\n🔍 ПОИСК ПРОБЛЕМ В КОДЕ (CODE SMELLS):");
            System.out.print("🤖 DeepSeek ищет проблемы... ");
            String smells = deepSeek.findCodeSmells(allFiles, context);
            System.out.println("\n" + smells);

            System.out.println("\n✅ АНАЛИЗ ПРОЕКТА ЗАВЕРШЁН!");

        } catch (Exception e) {
            System.out.println("❌ Ошибка при анализе: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== ОСТАЛЬНЫЕ ФУНКЦИИ ==========

    /**
     * Генерация и сохранение тестов
     */
    private static void generateAndSaveTests(String className) {
        try {
            System.out.println("🔍 Загружаю класс " + className + "...");
            context.addClassToContext(className);
            String code = context.readFileContent(findClassFile(className));

            System.out.println("🤖 Генерирую тесты...");
            String tests = deepSeek.generateTests(className, code);

            // Извлекаем код из ответа
            String testCode = extractCode(tests);

            // Определяем путь для сохранения теста
            Path testPath = getTestPath(className);

            System.out.println("\n📝 Сгенерированные тесты:");
            System.out.println("---");
            System.out.println(testCode);
            System.out.println("---");

            System.out.print("\n💾 Сохранить тесты в " + testPath + "? (y/n): ");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine();

            if (answer.equalsIgnoreCase("y")) {
                Files.createDirectories(testPath.getParent());
                Files.writeString(testPath, testCode);
                System.out.println("✅ Тесты сохранены в " + testPath);
            } else {
                System.out.println("❌ Сохранение отменено");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    /**
     * Исправление ошибок в коде
     */
    private static void fixCode(String className) {
        try {
            System.out.println("🔍 Загружаю класс " + className + "...");
            context.addClassToContext(className);
            String code = context.readFileContent(findClassFile(className));

            System.out.println("🤖 Анализирую и исправляю ошибки...");
            String fixedCode = deepSeek.fixCode(code, className);

            String newCode = extractCode(fixedCode);

            System.out.println("\n📝 Исправленный код:");
            System.out.println("---");
            System.out.println(newCode);
            System.out.println("---");

            Path filePath = projectPath.resolve(findClassFile(className));
            System.out.print("\n💾 Применить изменения к " + filePath + "? (y/n): ");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine();

            if (answer.equalsIgnoreCase("y")) {
                Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".backup");
                Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("📦 Бэкап сохранён: " + backupPath);

                Files.writeString(filePath, newCode);
                System.out.println("✅ Код обновлён!");
            } else {
                System.out.println("❌ Изменения отменены");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    /**
     * Рефакторинг кода
     */
    private static void refactorCode(String className) {
        try {
            System.out.println("🔍 Загружаю класс " + className + "...");
            context.addClassToContext(className);
            String code = context.readFileContent(findClassFile(className));

            System.out.println("🤖 Предлагаю рефакторинг...");
            String refactored = deepSeek.suggestRefactoring(code);

            String newCode = extractCode(refactored);

            System.out.println("\n📝 Предлагаемый рефакторинг:");
            System.out.println("---");
            System.out.println(newCode);
            System.out.println("---");

            Path filePath = projectPath.resolve(findClassFile(className));
            System.out.print("\n💾 Применить рефакторинг к " + filePath + "? (y/n): ");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine();

            if (answer.equalsIgnoreCase("y")) {
                Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".refactor.backup");
                Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                Files.writeString(filePath, newCode);
                System.out.println("✅ Рефакторинг применён! Бэкап: " + backupPath);
            } else {
                System.out.println("❌ Рефакторинг отменён");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    /**
     * Создание нового класса
     */
    private static void createNewClass(String description) {
        try {
            System.out.println("🤖 Создаю класс по описанию: " + description);
            String newClass = deepSeek.createClass(description);

            String code = extractCode(newClass);

            String className = extractClassName(code);
            if (className == null) {
                className = "NewClass";
            }

            Path classPath = getMainPath(className);

            System.out.println("\n📝 Сгенерированный класс:");
            System.out.println("---");
            System.out.println(code);
            System.out.println("---");

            System.out.print("\n💾 Сохранить в " + classPath + "? (y/n): ");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine();

            if (answer.equalsIgnoreCase("y")) {
                Files.createDirectories(classPath.getParent());
                Files.writeString(classPath, code);
                System.out.println("✅ Класс сохранён в " + classPath);
            } else {
                System.out.println("❌ Сохранение отменено");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    /**
     * Детальный анализ класса
     */
    private static void analyzeClass(String className) {
        try {
            System.out.println("🔍 Загружаю класс " + className + "...");
            context.addClassToContext(className);
            String code = context.readFileContent(findClassFile(className));

            System.out.println("🤖 Провожу детальный анализ...");
            String analysis = deepSeek.deepAnalyze(code, className);
            System.out.println(analysis);

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    /**
     * Показать структуру проекта
     */
    private static void showStructure() {
        try {
            System.out.println(context.getFullContext());
        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    /**
     * Загрузить класс в контекст
     */
    private static void loadClass(String className) {
        try {
            context.addClassToContext(className);
            System.out.println("✅ Загружен: " + className);
        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    /**
     * Задать вопрос
     */
    private static void askQuestion(String question) {
        try {
            String prompt = buildPrompt(question);
            System.out.print("🤖 DeepSeek: ");
            String response = deepSeek.sendMessage(prompt);
            System.out.println(response);
        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private static String findClassFile(String className) throws IOException {
        var allFiles = context.getAllJavaFiles();
        for (String file : allFiles) {
            if (file.endsWith(className + ".java")) {
                return file;
            }
        }
        throw new IOException("Класс " + className + " не найден");
    }

    private static Path getTestPath(String className) {
        String testFileName = className + "Test.java";

        // Пытаемся определить пакет
        try {
            String filePath = findClassFile(className);
            if (filePath.contains("/src/main/java/")) {
                String packagePath = filePath.substring(filePath.indexOf("/src/main/java/") + 16);
                packagePath = packagePath.substring(0, packagePath.lastIndexOf('/'));
                return projectPath.resolve("src/test/java")
                        .resolve(packagePath)
                        .resolve(testFileName);
            }
        } catch (Exception e) {
            // Игнорируем, используем корневую директорию
        }

        return projectPath.resolve("src/test/java").resolve(testFileName);
    }

    private static Path getMainPath(String className) {
        return projectPath.resolve("src/main/java").resolve(className + ".java");
    }

    private static String extractCode(String response) {
        Pattern pattern = Pattern.compile("```java\\s*(.*?)\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        Pattern pattern2 = Pattern.compile("```\\s*(.*?)\\s*```", Pattern.DOTALL);
        Matcher matcher2 = pattern2.matcher(response);

        if (matcher2.find()) {
            return matcher2.group(1).trim();
        }

        return response;
    }

    private static String extractClassName(String code) {
        Pattern pattern = Pattern.compile("(?:public\\s+)?class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String buildPrompt(String question) {
        String currentContext = context.getCurrentContext();
        if (currentContext.isEmpty()) {
            return question;
        }
        return "Вот код из моего проекта:\n\n" + currentContext + "\n\nМой вопрос: " + question;
    }

    // ========== МЕТОДЫ ДЛЯ АНАЛИЗА ЗАДАЧ ==========

    /**
     * Анализ задачи из текстового файла
     */
    private static void analyzeTaskFromFile(String filePath) {
        try {
            Path taskFile = Paths.get(filePath);
            if (!Files.exists(taskFile)) {
                System.out.println("❌ Файл не найден: " + filePath);
                return;
            }

            String taskDescription = Files.readString(taskFile);
            System.out.println("\n📄 Загружена задача из файла:");
            System.out.println("---");
            // Показываем первые 200 символов
            String preview = taskDescription.length() > 200 ?
                    taskDescription.substring(0, 200) + "..." : taskDescription;
            System.out.println(preview);
            System.out.println("---\n");

            analyzeRequirement(taskDescription);

        } catch (Exception e) {
            System.out.println("❌ Ошибка чтения файла: " + e.getMessage());
        }
    }

    /**
     * Анализ соответствия кода произвольному требованию
     */
    private static void analyzeRequirement(String requirement) {
        try {
            System.out.println("🔍 Анализирую соответствие кода требованию...\n");

            // Получаем список всех Java-файлов
            List<String> allFiles = context.getAllJavaFiles();

            if (allFiles.isEmpty()) {
                System.out.println("⚠️ Java-файлы не найдены");
                return;
            }

            // Собираем код проекта для анализа
            StringBuilder projectCode = new StringBuilder();
            int filesLoaded = 0;

            System.out.println("📁 Загружаю файлы проекта...");
            for (String fileName : allFiles) {
                try {
                    String code = context.readFileContent(fileName);
                    projectCode.append("=== ").append(fileName).append(" ===\n");
                    projectCode.append("```java\n").append(code).append("\n```\n\n");
                    filesLoaded++;

                    // Ограничиваем размер для API (чтобы не превысить лимит)
                    if (projectCode.length() > 20000) {
                        projectCode.append("\n... (показано ").append(filesLoaded)
                                .append(" из ").append(allFiles.size()).append(" файлов)");
                        break;
                    }
                } catch (Exception e) {
                    projectCode.append("❌ Ошибка чтения: ").append(fileName).append("\n");
                }
            }

            System.out.println("✅ Загружено " + filesLoaded + " файлов\n");
            System.out.println("🤖 DeepSeek анализирует соответствие...\n");

            String analysis = deepSeek.analyzeCompliance(requirement, projectCode.toString());
            System.out.println(analysis);

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}