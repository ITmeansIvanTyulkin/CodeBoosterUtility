package scanner;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectScanner {

    private final Path projectPath;

    public ProjectScanner(String projectPath) {
        this.projectPath = Paths.get(projectPath);
    }

    // Получить список всех Java-файлов в проекте
    public List<Path> getAllJavaFiles() throws IOException {
        return Files.walk(projectPath)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> !path.toString().contains("/target/")) // исключаем target
                .filter(path -> !path.toString().contains("/build/"))  // исключаем build
                .collect(Collectors.toList());
    }

    // Получить структуру проекта (пакеты и классы)
    public String getProjectStructure() throws IOException {
        StringBuilder structure = new StringBuilder();
        structure.append("Структура проекта:\n");

        Map<String, List<String>> packages = new TreeMap<>();

        for (Path file : getAllJavaFiles()) {
            String relativePath = projectPath.relativize(file).toString();
            String packageName = relativePath.contains("/") ?
                    relativePath.substring(0, relativePath.lastIndexOf('/')) : "";
            String className = file.getFileName().toString();

            packages.computeIfAbsent(packageName, k -> new ArrayList<>()).add(className);
        }

        for (Map.Entry<String, List<String>> entry : packages.entrySet()) {
            structure.append("  ").append(entry.getKey().isEmpty() ? "(root)" : entry.getKey()).append("/\n");
            for (String className : entry.getValue()) {
                structure.append("    ├── ").append(className).append("\n");
            }
        }

        return structure.toString();
    }

    // Прочитать содержимое конкретного файла
    public String readFile(String fileName) throws IOException {
        return Files.readString(projectPath.resolve(fileName));
    }

    // Найти файл по имени класса
    public Optional<Path> findClass(String className) throws IOException {
        return getAllJavaFiles().stream()
                .filter(path -> path.getFileName().toString().equals(className + ".java"))
                .findFirst();
    }
}