package com.antenapro.hotelcheck.sitecheck;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Hotel Site Check is local-only (REQ-038, docs/plans/hotel-site-check-plan.md). This test fails the build if
 * the flag names reach anything that builds, deploys or configures a shared environment: a GitHub workflow, a
 * Dockerfile, a compose/stack file, the frontend's package.json / next.config / tracked env files, or a Spring
 * properties file other than {@code application-local.properties} setting the flag to true.
 */
class SiteCheckFlagLeakTest {

    private static final List<String> FLAGS = List.of("hotelcheck.sitecheck.enabled", "HOTELCHECK_SITECHECK_ENABLED",
            "antena.sitecheck.enabled", "NEXT_PUBLIC_SITECHECK_ENABLED");
    private static final Set<String> SKIP_DIRS = Set.of("node_modules", ".git", "target", ".next", "dist", "build");

    @Test
    void flagNames_ShouldNotAppearInWorkflowsDockerfilesOrComposeFiles() throws IOException {
        Path root = repoRoot();
        List<Path> files = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(p -> !isSkipped(root, p)).filter(Files::isRegularFile).filter(p -> isGuarded(root, p)).forEach(files::add);
        }
        List<String> names = files.stream().map(root::relativize).map(Path::toString).map(n -> n.replace('\\', '/')).toList();
        assertTrue(names.contains(".github/workflows/backend-validation.yml"), "guarded files found under " + root + ": " + names);
        assertTrue(names.contains("frontend/package.json"), "frontend/package.json must be guarded: " + names);

        List<String> leaks = new ArrayList<>();
        for (Path file : files) {
            String text = Files.readString(file, StandardCharsets.ISO_8859_1);
            for (String flag : FLAGS) {
                if (text.contains(flag)) {
                    leaks.add(root.relativize(file) + " mentions " + flag);
                }
            }
        }
        assertTrue(leaks.isEmpty(), "the Hotel Site Check must stay local-only; remove the flag from: " + leaks);
    }

    @Test
    void onlyTheLocalProfile_ShouldTurnTheFlagOn() throws IOException {
        Path resources = repoRoot().resolve("backend/src/main/resources");
        assertEquals("false", value(resources.resolve("application.properties")), "application.properties must keep the flag off");
        assertEquals("true", value(resources.resolve("application-local.properties")), "application-local.properties turns the flag on");
        try (Stream<Path> files = Files.list(resources)) {
            for (Path p : files.filter(f -> f.getFileName().toString().startsWith("application-")).toList()) {
                if (!p.getFileName().toString().equals("application-local.properties")) {
                    assertFalse("true".equals(value(p)), p.getFileName() + " must not turn the flag on");
                }
            }
        }
    }

    private static String value(Path propertiesFile) throws IOException {
        if (!Files.exists(propertiesFile)) {
            return null;
        }
        for (String line : Files.readAllLines(propertiesFile)) {
            String t = line.trim();
            if (t.startsWith("hotelcheck.sitecheck.enabled")) {
                return t.substring(t.indexOf('=') + 1).trim();
            }
        }
        return null;
    }

    private static boolean isGuarded(Path root, Path p) {
        String rel = root.relativize(p).toString().replace('\\', '/');
        String name = p.getFileName().toString();
        if (name.startsWith("Dockerfile") || name.startsWith("docker-compose") || name.startsWith("docker-stack") || name.startsWith("compose.")) {
            return true;
        }
        if (rel.startsWith(".github/workflows/") && (name.endsWith(".yml") || name.endsWith(".yaml"))) {
            return true;
        }
        // The frontend inlines NEXT_PUBLIC_* at build time: only the gitignored .env.local may set the flag;
        // .env.example documents the variable name and is the one file allowed to mention it.
        if (rel.startsWith("frontend/") && !rel.startsWith("frontend/node_modules/")) {
            if (name.equals("package.json") || name.startsWith("next.config")) {
                return true;
            }
            return name.startsWith(".env") && !name.equals(".env.local") && !name.equals(".env.example");
        }
        return false;
    }

    private static boolean isSkipped(Path root, Path p) {
        for (Path part : root.relativize(p)) {
            if (SKIP_DIRS.contains(part.toString())) {
                return true;
            }
        }
        return false;
    }

    /** Walks up from the module directory to the directory that holds {@code .github/workflows}. */
    private static Path repoRoot() {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null) {
            if (Files.isDirectory(dir.resolve(".github/workflows")) && Files.isDirectory(dir.resolve("backend"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("Repository root not found above " + Paths.get("").toAbsolutePath());
    }
}
