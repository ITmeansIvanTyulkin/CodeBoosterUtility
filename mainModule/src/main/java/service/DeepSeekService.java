package service;

import builder.ContextBuilder;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DeepSeekService {

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "deepseek-chat";

    private final String apiKey;
    private final Proxy proxy;
    private final int connectTimeout;
    private final int readTimeout;

    // Конструктор без прокси
    public DeepSeekService(String apiKey) {
        this(apiKey, null, 0);
    }

    // Конструктор с прокси (без авторизации)
    public DeepSeekService(String apiKey, String proxyHost, int proxyPort) {
        this.apiKey = apiKey;
        this.connectTimeout = 30000;
        this.readTimeout = 60000;

        // Настраиваем прокси только если передан хост и порт
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
            this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        } else {
            this.proxy = null;
        }
    }

    /**
     * Отправить сообщение DeepSeek и получить ответ
     */
    public String sendMessage(String message) {
        return sendMessage(message, DEFAULT_MODEL);
    }

    public String sendMessage(String message, String model) {
        HttpURLConnection connection = null;

        try {
            String jsonBody = buildJsonRequest(message, model);
            URL url = new URL(API_URL);

            // Открываем соединение (с прокси или без)
            if (proxy != null) {
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            // Настройка таймаутов
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);

            // Настройка заголовков
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);

            // Отправляем запрос
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String responseBody = readResponse(connection.getInputStream());
                return parseResponse(responseBody);
            } else {
                String errorBody = readResponse(connection.getErrorStream());
                return handleError(responseCode, errorBody);
            }

        } catch (SocketTimeoutException e) {
            return "❌ Таймаут соединения. Проверьте интернет.";
        } catch (UnknownHostException e) {
            return "❌ Не удаётся подключиться к api.deepseek.com. Проверьте интернет.";
        } catch (IOException e) {
            return "❌ Ошибка соединения: " + e.getMessage();
        } catch (Exception e) {
            return "❌ Ошибка: " + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Исправление ошибок в коде
     */
    public String fixCode(String code, String className) {
        String prompt = "Ты опытный Java-разработчик. Найди и исправь ошибки в этом коде.\n" +
                "Класс: " + className + "\n" +
                "```java\n" + code + "\n```\n\n" +
                "Верни только исправленный код целиком в формате ```java ... ```";
        return sendMessage(prompt, "deepseek-coder");
    }

    /**
     * Детальный анализ кода
     */
    public String deepAnalyze(String code, String className) {
        String prompt = "Проведи детальный анализ Java-класса " + className + ":\n" +
                "1. Качество кода (читаемость, стиль)\n" +
                "2. Потенциальные баги (NPE, исключения, race conditions)\n" +
                "3. Нарушения SOLID\n" +
                "4. Проблемы производительности\n" +
                "5. Риски безопасности\n" +
                "6. Что можно улучшить\n\n" +
                "Код:\n```java\n" + code + "\n```";
        return sendMessage(prompt, "deepseek-coder");
    }

    /**
     * Создание нового класса по описанию
     */
    public String createClass(String description) {
        String prompt = "Создай Java-класс по описанию:\n" + description + "\n\n" +
                "Требования:\n" +
                "- Используй Java 17\n" +
                "- Добавь Javadoc для класса и публичных методов\n" +
                "- Добавь обработку ошибок\n" +
                "- Верни только код в формате ```java ... ```";
        return sendMessage(prompt, "deepseek-chat");
    }

    /**
     * Анализ архитектуры проекта
     */
    public String analyzeArchitecture(String structure) {
        String prompt = "Проанализируй структуру этого Java-проекта:\n" + structure + "\n\n" +
                "Оцени:\n" +
                "1. Какая архитектура используется (MVC, layered, и т.д.)?\n" +
                "2. Есть ли проблемы в организации пакетов?\n" +
                "3. Соответствует ли структура best practices?\n" +
                "4. Какие улучшения можно предложить?\n\n" +
                "Ответь структурированно, на русском языке.";
        return sendMessage(prompt, "deepseek-chat");
    }

    /**
     * Поиск проблем в коде (code smells) по всем файлам
     */
    public String findCodeSmells(List<String> files, ContextBuilder context) {
        StringBuilder allCode = new StringBuilder();
        int filesLoaded = 0;

        for (String fileName : files) {
            try {
                String code = context.readFileContent(fileName);
                allCode.append("Файл: ").append(fileName).append("\n");
                allCode.append("```java\n").append(code).append("\n```\n\n");
                filesLoaded++;

                // Ограничиваем размер запроса (DeepSeek имеет ограничение)
                if (allCode.length() > 15000) {
                    allCode.append("\n... (показано ").append(filesLoaded).append(" из ").append(files.size()).append(" файлов)");
                    break;
                }
            } catch (Exception e) {
                allCode.append("❌ Не удалось прочитать файл: ").append(fileName).append("\n");
            }
        }

        String prompt = "Проанализируй код и найди:\n" +
                "1. Code smells (проблемы в коде)\n" +
                "2. Нарушения принципов SOLID\n" +
                "3. Потенциальные баги\n" +
                "4. Проблемы с производительностью\n" +
                "5. Риски безопасности\n\n" +
                "Код:\n" + allCode.toString() + "\n\n" +
                "Для каждой проблемы укажи: файл, описание, рекомендацию по исправлению.";

        return sendMessage(prompt, "deepseek-coder");
    }

    private String buildJsonRequest(String message, String model) {
        String escapedMessage = escapeJson(message);
        return "{"
                + "\"model\": \"" + model + "\","
                + "\"messages\": ["
                + "  {\"role\": \"user\", \"content\": \"" + escapedMessage + "\"}"
                + "],"
                + "\"temperature\": 0.3,"
                + "\"max_tokens\": 2048"
                + "}";
    }

    private String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    private String readResponse(InputStream stream) throws IOException {
        if (stream == null) return "";
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private String parseResponse(String json) {
        try {
            int contentIndex = json.indexOf("\"content\"");
            if (contentIndex == -1) {
                int errorIndex = json.indexOf("\"error\"");
                if (errorIndex != -1) {
                    return "⚠️ API вернул ошибку: " + json;
                }
                return "⚠️ Не удалось распарсить ответ от DeepSeek";
            }
            int startQuote = json.indexOf("\"", contentIndex + 10);
            if (startQuote == -1) return "Не удалось найти содержимое ответа";
            int endQuote = findEndQuote(json, startQuote + 1);
            if (endQuote == -1) return "Не удалось найти конец содержимого";
            String content = json.substring(startQuote + 1, endQuote);
            return unescapeJson(content);
        } catch (Exception e) {
            return "Ошибка парсинга ответа: " + e.getMessage();
        }
    }

    private int findEndQuote(String json, int start) {
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\') i++;
            else if (c == '"') return i;
        }
        return -1;
    }

    private String unescapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"': sb.append('"'); i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    case 'n': sb.append('\n'); i++; break;
                    case 'r': sb.append('\r'); i++; break;
                    case 't': sb.append('\t'); i++; break;
                    default: sb.append(c); break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String handleError(int statusCode, String responseBody) {
        switch (statusCode) {
            case 401: return "❌ Неверный API ключ DeepSeek. Проверьте токен.";
            case 429: return "⚠️ Превышен лимит запросов. Подождите немного.";
            case 403: return "❌ Доступ запрещён (403). Возможно, проблема с прокси.";
            case 500:
            case 502:
            case 503: return "🔧 Сервер DeepSeek временно недоступен. Попробуйте позже.";
            default: return "❌ Ошибка API (код " + statusCode + "): " + responseBody;
        }
    }

    // ========== Удобные методы ==========

    public String askAboutCode(String code, String question) {
        String prompt = "Вот код:\n```java\n" + code + "\n```\n\nВопрос: " + question;
        return sendMessage(prompt, "deepseek-coder");
    }

    public String generateTests(String className, String code) {
        String prompt = "Напиши JUnit 5 тесты для класса " + className + ":\n```java\n" + code + "\n```";
        return sendMessage(prompt, "deepseek-coder");
    }

    public String explainCode(String code) {
        String prompt = "Объясни этот код на русском:\n```java\n" + code + "\n```";
        return sendMessage(prompt, "deepseek-coder");
    }

    public String suggestRefactoring(String code) {
        String prompt = "Предложи рефакторинг этого кода:\n```java\n" + code + "\n```";
        return sendMessage(prompt, "deepseek-coder");
    }
}