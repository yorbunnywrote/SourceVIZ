package com.sourceviz.scanner;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class ProjectScanner {
    private static final Set<String> CPP_EXTENSIONS = Set.of(
            ".cpp", ".c", ".cc", ".cxx", ".h", ".hpp", ".hxx", ".inl"
    );

    private static final Set<String> JAVA_EXTENSIONS = Set.of(
            ".java"
    );

    private static final Set<String> IGNORED_DIRS = Set.of(
            ".git", ".svn", ".vs", ".vscode", ".idea", "build", "target", "bin", "out",
            "node_modules", "obj", "x64", "debug", "release"
    );

    private final Path projectRoot;
    private final List<Path> cppFiles = new ArrayList<>();
    private final List<Path> javaFiles = new ArrayList<>();

    public ProjectScanner(Path projectRoot) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
    }

    public void scan() throws IOException {
        cppFiles.clear();
        javaFiles.clear();

        if (!Files.exists(projectRoot) || !Files.isDirectory(projectRoot)) {
            throw new IllegalArgumentException("Project directory does not exist: " + projectRoot);
        }

        Files.walkFileTree(projectRoot, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String name = dir.getFileName() != null ? dir.getFileName().toString().toLowerCase() : "";
                if (IGNORED_DIRS.contains(name)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String fileName = file.getFileName().toString().toLowerCase();
                for (String ext : CPP_EXTENSIONS) {
                    if (fileName.endsWith(ext)) {
                        cppFiles.add(file.toAbsolutePath().normalize());
                        return FileVisitResult.CONTINUE;
                    }
                }
                for (String ext : JAVA_EXTENSIONS) {
                    if (fileName.endsWith(ext)) {
                        javaFiles.add(file.toAbsolutePath().normalize());
                        return FileVisitResult.CONTINUE;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public Path getProjectRoot() { return projectRoot; }
    public List<Path> getCppFiles() { return Collections.unmodifiableList(cppFiles); }
    public List<Path> getJavaFiles() { return Collections.unmodifiableList(javaFiles); }

    public List<Path> getAllFiles() {
        List<Path> all = new ArrayList<>(cppFiles.size() + javaFiles.size());
        all.addAll(cppFiles);
        all.addAll(javaFiles);
        return all;
    }

    public boolean isCppFile(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        return CPP_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    public boolean isJavaFile(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        return JAVA_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    /**
     * Resolves C++ header reference to an actual file if possible
     */
    public String resolveCppHeader(Path currentFile, String headerName) {
        String clean = headerName.trim();
        if ((clean.startsWith("\"") && clean.endsWith("\"")) || (clean.startsWith("<") && clean.endsWith(">"))) {
            clean = clean.substring(1, clean.length() - 1);
        }

        // Check relative to current file
        if (currentFile != null && currentFile.getParent() != null) {
            Path relative = currentFile.getParent().resolve(clean).normalize();
            if (Files.exists(relative)) {
                return relative.toString();
            }
        }

        // Check relative to project root
        Path fromRoot = projectRoot.resolve(clean).normalize();
        if (Files.exists(fromRoot)) {
            return fromRoot.toString();
        }

        // Fallback: search by file name in scanned files
        String searchName = Paths.get(clean).getFileName().toString();
        for (Path p : cppFiles) {
            if (p.getFileName().toString().equalsIgnoreCase(searchName)) {
                return p.toString();
            }
        }

        return clean;
    }

    /**
     * Resolves Java import to file if it belongs to this project
     */
    public String resolveJavaImport(String importStatement) {
        String clean = importStatement.trim();
        if (clean.endsWith(";")) clean = clean.substring(0, clean.length() - 1);
        if (clean.startsWith("import ")) clean = clean.substring(7).trim();
        if (clean.startsWith("static ")) clean = clean.substring(7).trim();

        // Convert package to path e.g. com.example.MyClass -> com/example/MyClass.java
        String classRelPath = clean.replace('.', '/') + ".java";
        for (Path p : javaFiles) {
            String norm = p.toString().replace('\\', '/');
            if (norm.endsWith(classRelPath)) {
                return p.toString();
            }
        }

        // Simple class name fallback
        int lastDot = clean.lastIndexOf('.');
        String simpleName = lastDot >= 0 ? clean.substring(lastDot + 1) + ".java" : clean + ".java";
        for (Path p : javaFiles) {
            if (p.getFileName().toString().equals(simpleName)) {
                return p.toString();
            }
        }

        return clean;
    }
}
