package builder;

import scanner.ProjectScanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ContextBuilder {

    private final Path projectPath;
    private final ProjectScanner scanner;
    private final StringBuilder currentContext;

    public ContextBuilder(String projectPath) {
        this.projectPath = Paths.get(projectPath);
        this.scanner = new ProjectScanner(projectPath);
        this.currentContext = new StringBuilder();
    }

    /**
     * Получить список всех Java-файлов в проекте (относительные пути)
     */
    public List<String> getAllJavaFiles() throws IOException {
        List<String> files = new ArrayList<>();
        // ✅ Исправлено: вызываем правильный метод scanner.getAllJavaFiles()
        for (Path path : scanner.getAllJavaFiles()) {
            String relativePath = projectPath.relativize(path).toString();
            files.add(relativePath);
        }
        return files;
    }

    /**
     * Прочитать содержимое файла по относительному пути
     */
    public String readFileContent(String fileName) throws IOException {
        Path fullPath = projectPath.resolve(fileName);
        return Files.readString(fullPath);
    }

    /**
     * Получить структуру проекта
     */
    public String getFullContext() throws IOException {
        return scanner.getProjectStructure();
    }

    /**
     * Загрузить класс в контекст
     */
    public void addClassToContext(String className) throws IOException {
        Optional<Path> file = scanner.findClass(className);
        if (file.isPresent()) {
            String content = Files.readString(file.get());
            currentContext.append("\n=== Класс: ").append(className).append(" ===\n");
            currentContext.append("```java\n").append(content).append("\n```\n");
        } else {
            throw new IOException("Класс " + className + " не найден");
        }
    }

    /**
     * Получить текущий контекст
     */
    public String getCurrentContext() {
        return currentContext.toString();
    }

    /**
     * Очистить контекст
     */
    public void clearContext() {
        currentContext.setLength(0);
    }
}