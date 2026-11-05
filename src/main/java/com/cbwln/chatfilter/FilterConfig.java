package com.cbwln.chatfilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FilterConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("chatfilter.json");

    public enum Type {
        CONTAINS("Contains"), STARTS_WITH("Starts with"), REGEX("Matches regex");

        public final String label;

        Type(String label) {
            this.label = label;
        }
    }

    public static final class Rule {
        private final Type type;
        private final String text;
        private transient Pattern pattern;
        private transient boolean patternAttempted;

        public Rule(Type type, String text) {
            this.type = type;
            this.text = text;
        }

        public Type type() {
            return type;
        }

        public String text() {
            return text;
        }

        public boolean matches(String input) {
            if (!patternAttempted) {
                patternAttempted = true;

                try {
                    pattern = Pattern.compile(text);
                } catch (PatternSyntaxException e) {
                    ChatFilter.LOGGER.warn("Ignoring invalid regex {}", text);
                }
            }

            return pattern != null && pattern.matcher(input).find();
        }
    }

    private List<Rule> rules = new ArrayList<>();

    public static FilterConfig load() {
        FilterConfig config;
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                config = Objects.requireNonNullElseGet(GSON.fromJson(reader, FilterConfig.class), FilterConfig::new);
            } catch (IOException | JsonSyntaxException e) {
                ChatFilter.LOGGER.error("Failed to read config", e);
                config = new FilterConfig();
            }
        } else {
            config = new FilterConfig();
        }
        config.validate();
        config.save();
        return config;
    }

    private void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            ChatFilter.LOGGER.error("Failed to write config", e);
        }
    }

    public List<Rule> rules() {
        return Collections.unmodifiableList(rules);
    }

    public void add(Type type, String text) {
        rules.add(new Rule(type, text));
        save();
    }

    private void validate() {
        if (rules == null) {
            rules = new ArrayList<>();
            return;
        }

        rules.removeIf(rule -> rule == null || rule.type() == null || rule.text() == null);
    }

    public void remove(int index) {
        if (index < 0 || index >= rules.size()) return;
        rules.remove(index);
        save();
    }
}
