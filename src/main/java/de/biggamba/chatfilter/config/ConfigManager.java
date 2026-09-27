package de.biggamba.chatfilter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.biggamba.chatfilter.ChatfilterMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Lädt und speichert die Konfiguration als JSON.
 */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create();

	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("chatfilter.json");

	private static ChatfilterConfig config = new ChatfilterConfig();

	private ConfigManager() {
	}

	public static ChatfilterConfig get() {
		return config;
	}

	public static Path path() {
		return PATH;
	}

	/**
	 * Setzt die Konfiguration auf die im Mod mitgelieferte Standardkonfiguration zurück.
	 * Die bisherige Datei wird vorher als {@code chatfilter.json.bak} gesichert.
	 */
	public static void resetToDefaults() {
		try {
			if (Files.exists(PATH)) {
				Files.copy(PATH, PATH.resolveSibling("chatfilter.json.bak"), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			ChatfilterMod.LOGGER.error("Konnte kein Backup der Konfiguration anlegen.", e);
		}

		config = loadBundledDefault();
		save();
	}

	/** Liest die im Mod-JAR mitgelieferte Standardkonfiguration. */
	private static ChatfilterConfig loadBundledDefault() {
		try (InputStream stream = ConfigManager.class.getResourceAsStream("/default_config.json")) {
			if (stream != null) {
				try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
					ChatfilterConfig bundled = GSON.fromJson(reader, ChatfilterConfig.class);

					if (bundled != null) {
						sanitize(bundled);
						return bundled;
					}
				}
			}
		} catch (Exception e) {
			ChatfilterMod.LOGGER.error("Mitgelieferte Standardkonfiguration ist defekt.", e);
		}

		return ChatfilterConfig.createDefault();
	}

	public static void load() {
		if (!Files.exists(PATH)) {
			config = loadBundledDefault();
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(PATH)) {
			ChatfilterConfig loaded = GSON.fromJson(reader, ChatfilterConfig.class);
			config = loaded != null ? loaded : ChatfilterConfig.createDefault();
			sanitize(config);
		} catch (Exception e) {
			ChatfilterMod.LOGGER.error("Konnte {} nicht lesen – benutze Standardwerte.", PATH, e);
			config = ChatfilterConfig.createDefault();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());

			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			ChatfilterMod.LOGGER.error("Konnte {} nicht schreiben.", PATH, e);
		}
	}

	/** Fängt kaputte/alte JSON-Dateien ab, damit der Mod nie mit null-Feldern startet. */
	private static void sanitize(ChatfilterConfig config) {
		if (config.windows == null) {
			config.windows = new java.util.ArrayList<>();
		}

		if (config.rules == null) {
			config.rules = new java.util.ArrayList<>();
		}

		config.windows.removeIf(window -> window == null || window.id == null);
		config.rules.removeIf(rule -> rule == null || rule.id == null);

		for (FilterRule rule : config.rules) {
			if (rule.matchType == null) {
				rule.matchType = MatchType.CONTAINS;
			}

			if (rule.action == null) {
				rule.action = RuleAction.KEEP;
			}

			if (rule.pattern == null) {
				rule.pattern = "";
			}
		}
	}
}
