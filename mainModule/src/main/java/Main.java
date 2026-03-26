import builder.ContextBuilder;
import service.DeepSeekService;
import java.util.Scanner;

public class Main {

    private static final String DEEPSEEK_TOKEN = "";
    private static final String PROXY_HOST = "18.199.183.77";
    private static final int PROXY_PORT = 49232;
    private static final boolean USE_PROXY = false;

    private static DeepSeekService deepSeek;
    private static ContextBuilder context;

    public static void main(String[] args) {

        // ========== ПОЛУЧАЕМ ПУТЬ К ПРОЕКТУ ==========
        String projectPath;

        if (args.length > 0) {
            projectPath = args[0];
            System.out.println("📁 Анализируем проект: " + projectPath);
        } else {
            System.out.print("📁 Введите путь к проекту: ");
            Scanner pathScanner = new Scanner(System.in);
            projectPath = pathScanner.nextLine().trim();

            if (projectPath.isEmpty()) {
                projectPath = System.getProperty("user.dir");
                System.out.println("📁 Используем текущую директорию: " + projectPath);
            }
        }
        // ============================================

        // Инициализация DeepSeek
        if (USE_PROXY) {
            deepSeek = new DeepSeekService(DEEPSEEK_TOKEN, PROXY_HOST, PROXY_PORT);
            System.out.println("✅ DeepSeek готов");
        } else {
            deepSeek = new DeepSeekService(DEEPSEEK_TOKEN);
            System.out.println("✅ DeepSeek готов");
        }

        // Инициализация контекста проекта
        context = new ContextBuilder(projectPath);

        System.out.println("\n=== 🤖 DeepSeek AI Assistant ===");
        System.out.println("📁 Проект: " + projectPath);
        System.out.println("----------------------------------------");

        // ========== АВТОМАТИЧЕСКИЙ АНАЛИЗ ПРОЕКТА ==========
        System.out.println("\n🔍 Запускаю автоматический анализ проекта...\n");

        try {
            // 1. Показываем структуру проекта
            System.out.println("📁 СТРУКТУРА ПРОЕКТА:");
            String structure = context.getFullContext();
            System.out.println(structure);

            // 2. Получаем список всех Java-файлов
            var allFiles = context.getAllJavaFiles();
            System.out.println("\n📄 НАЙДЕНО ФАЙЛОВ: " + allFiles.size());

            if (allFiles.isEmpty()) {
                System.out.println("⚠️ Java-файлы не найдены. Убедитесь, что путь указан верно.");
            } else {
                // 3. Анализируем первые несколько файлов (чтобы не перегружать API)
                System.out.println("\n🔬 АНАЛИЗ КОДА (первые 3 файла):");
                int analyzedCount = 0;
                for (String fileInfo : allFiles) {
                    if (analyzedCount >= 3) break;

                    String fileName = fileInfo.split(":")[0];
                    System.out.println("\n--- Анализ: " + fileName + " ---");

                    // Получаем код файла
                    String code = context.readFileContent(fileName);

                    // Запрашиваем у DeepSeek анализ кода
                    String analysis = analyzeCode(fileName, code);
                    System.out.println(analysis);

                    analyzedCount++;
                }

                if (allFiles.size() > 3) {
                    System.out.println("\n... и ещё " + (allFiles.size() - 3) + " файлов.");
                    System.out.println("Используйте /analyze-all для полного анализа.");
                }
            }

            // 4. Общий анализ архитектуры
            System.out.println("\n🏗️ ОБЩИЙ АНАЛИЗ АРХИТЕКТУРЫ:");
            String architectureAnalysis = analyzeArchitecture(structure);
            System.out.println(architectureAnalysis);

        } catch (Exception e) {
            System.out.println("❌ Ошибка при анализе: " + e.getMessage());
        }
        // ====================================================

        // Интерактивный режим
        System.out.println("\n----------------------------------------");
        System.out.println("💡 Теперь можно задавать вопросы или использовать команды:");
        System.out.println("  /load <класс>     - загрузить класс в контекст");
        System.out.println("  /analyze-all      - проанализировать все файлы");
        System.out.println("  /analyze <файл>   - проанализировать конкретный файл");
        System.out.println("  /find-smells      - найти проблемы в коде");
        System.out.println("  /structure        - показать структуру проекта");
        System.out.println("  /clear            - очистить контекст");
        System.out.println("  /exit             - выход");
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
                try {
                    System.out.println(context.getFullContext());
                } catch (Exception e) {
                    System.out.println("❌ " + e.getMessage());
                }
                continue;
            }

            if (input.equalsIgnoreCase("/analyze-all")) {
                analyzeAllFiles();
                continue;
            }

            if (input.startsWith("/analyze ")) {
                String fileName = input.substring(9).trim();
                analyzeSpecificFile(fileName);
                continue;
            }

            if (input.equalsIgnoreCase("/find-smells")) {
                findCodeSmells();
                continue;
            }

            if (input.equalsIgnoreCase("/clear")) {
                context.clearContext();
                System.out.println("🧹 Контекст очищен");
                continue;
            }

            if (input.startsWith("/load ")) {
                String className = input.substring(6).trim();
                try {
                    context.addClassToContext(className);
                    System.out.println("✅ Загружен: " + className);
                } catch (Exception e) {
                    System.out.println("❌ " + e.getMessage());
                }
                continue;
            }

            if (!input.trim().isEmpty()) {
                try {
                    String prompt = buildPrompt(input);
                    System.out.print("🤖 DeepSeek: ");
                    String response = deepSeek.sendMessage(prompt);
                    System.out.println(response);
                } catch (Exception e) {
                    System.out.println("❌ " + e.getMessage());
                }
            }
        }

        scanner.close();
    }

    /**
     * Анализ конкретного файла
     */
    private static String analyzeCode(String fileName, String code) {
        String prompt = "Проанализируй этот Java-код. Укажи:\n" +
                "1. Что делает этот класс/метод?\n" +
                "2. Есть ли потенциальные проблемы (null safety, исключения, производительность)?\n" +
                "3. Что можно улучшить?\n" +
                "4. Соответствует ли код Java-стилю?\n\n" +
                "Файл: " + fileName + "\n```java\n" + code + "\n```";

        String response = deepSeek.sendMessage(prompt);
        System.out.println(response);  // печатаем
        return response;               // и возвращаем
    }

    /**
     * Анализ архитектуры проекта
     */
    private static String analyzeArchitecture(String structure) {
        String prompt = "Проанализируй структуру этого Java-проекта:\n" + structure + "\n\n" +
                "Ответь на вопросы:\n" +
                "1. Какая архитектура используется (MVC, layered, и т.д.)?\n" +
                "2. Есть ли проблемы в организации пакетов?\n" +
                "3. Какие паттерны проектирования можно применить?\n" +
                "4. Общие рекомендации по улучшению структуры.";

        return deepSeek.sendMessage(prompt);
    }

    /**
     * Анализ всех файлов проекта
     */
    private static void analyzeAllFiles() {
        try {
            var allFiles = context.getAllJavaFiles();
            System.out.println("\n🔍 Анализирую " + allFiles.size() + " файлов...\n");

            for (String fileInfo : allFiles) {
                String fileName = fileInfo.split(":")[0];
                System.out.println("\n📄 === " + fileName + " ===");

                String code = context.readFileContent(fileName);
                analyzeCode(fileName, code);

                // Небольшая задержка, чтобы не забивать API
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println("\n✅ Анализ всех файлов завершён!");

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    /**
     * Анализ конкретного файла по имени
     */
    private static void analyzeSpecificFile(String fileName) {
        try {
            String code = context.readFileContent(fileName);
            System.out.println("\n🔍 Анализ файла: " + fileName);
            analyzeCode(fileName, code);
        } catch (Exception e) {
            System.out.println("❌ Файл не найден: " + fileName);
            System.out.println("Используйте /structure чтобы увидеть список файлов");
        }
    }

    /**
     * Поиск проблем в коде (code smells)
     */
    private static void findCodeSmells() {
        try {
            var allFiles = context.getAllJavaFiles();
            System.out.println("\n🔍 Ищу потенциальные проблемы в коде...\n");

            StringBuilder allCode = new StringBuilder();
            for (String fileInfo : allFiles) {
                String fileName = fileInfo.split(":")[0];
                String code = context.readFileContent(fileName);
                allCode.append("Файл: ").append(fileName).append("\n");
                allCode.append("```java\n").append(code).append("\n```\n\n");

                // Ограничиваем размер запроса
                if (allCode.length() > 10000) break;
            }

            String prompt = "Проанализируй этот Java-код и найди:\n" +
                    "1. Code smells (проблемы в коде)\n" +
                    "2. Нарушения принципов SOLID\n" +
                    "3. Потенциальные баги\n" +
                    "4. Проблемы с производительностью\n" +
                    "5. Риски безопасности\n\n" +
                    "Код:\n" + allCode.toString();

            System.out.print("🤖 DeepSeek анализирует...\n");
            String response = deepSeek.sendMessage(prompt);
            System.out.println(response);

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static String buildPrompt(String question) {
        String currentContext = context.getCurrentContext();
        if (currentContext.isEmpty()) {
            return question;
        }
        return "Вот код из моего проекта:\n\n" + currentContext + "\n\nМой вопрос: " + question;
    }
}