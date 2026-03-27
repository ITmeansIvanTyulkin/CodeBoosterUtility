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

        // Инициализация DeepSeek
        if (USE_PROXY) {
            deepSeek = new DeepSeekService(DEEPSEEK_TOKEN, PROXY_HOST, PROXY_PORT);
        } else {
            deepSeek = new DeepSeekService(DEEPSEEK_TOKEN);
        }
        System.out.println("✅ DeepSeek готов");

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
        System.out.println("  /analyze <класс>     - детальный анализ кода");
        System.out.println("  /structure           - показать структуру проекта");
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

            if (!input.trim().isEmpty()) {
                // Обычный вопрос
                askQuestion(input);
            }
        }

        scanner.close();
    }

    // ========== НОВЫЕ ФУНКЦИИ ==========

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

            // Извлекаем код из ответа (между ```java и ```)
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
                // Создаём директорию, если её нет
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

            // Извлекаем исправленный код
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
                // Создаём бэкап
                Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".backup");
                Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("📦 Бэкап сохранён: " + backupPath);

                // Сохраняем исправленный код
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

            // Пытаемся определить имя класса
            String className = extractClassName(code);
            if (className == null) {
                className = "NewClass";
            }

            // Определяем путь для сохранения
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

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private static void showStructure() {
        try {
            System.out.println(context.getFullContext());
        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private static void loadClass(String className) {
        try {
            context.addClassToContext(className);
            System.out.println("✅ Загружен: " + className);
        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

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
        String packagePath = getPackagePath(className);
        String testFileName = className + "Test.java";

        Path testPath = projectPath.resolve("src/test/java")
                .resolve(packagePath.replace('.', '/'))
                .resolve(testFileName);

        return testPath;
    }

    private static Path getMainPath(String className) {
        String testFileName = className + ".java";
        return projectPath.resolve("src/main/java").resolve(testFileName);
    }

    private static String getPackagePath(String className) {
        // Простая эвристика: ищем package в файле
        try {
            String filePath = findClassFile(className);
            return filePath.replace("/", ".").replace(".java", "");
        } catch (Exception e) {
            return "";
        }
    }

    private static String extractCode(String response) {
        // Ищем блок кода между ```java и ```
        Pattern pattern = Pattern.compile("```java\\s*(.*?)\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Если не нашли, пробуем без указания языка
        Pattern pattern2 = Pattern.compile("```\\s*(.*?)\\s*```", Pattern.DOTALL);
        Matcher matcher2 = pattern2.matcher(response);

        if (matcher2.find()) {
            return matcher2.group(1).trim();
        }

        return response;
    }

    private static String extractClassName(String code) {
        Pattern pattern = Pattern.compile("class\\s+(\\w+)");
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
}