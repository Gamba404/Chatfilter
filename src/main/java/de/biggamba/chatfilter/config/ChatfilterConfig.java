package de.biggamba.chatfilter.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Gesamte Konfiguration des Mods (wird als {@code config/chatfilter.json} gespeichert).
 */
public class ChatfilterConfig {
	/** Globaler Schalter – aus heißt: alles verhält sich wie Vanilla. */
	public boolean enabled = true;

	/** Eigene Chat-Fenster. */
	public List<ChatWindowConfig> windows = new ArrayList<>();

	/** Filterregeln, werden von oben nach unten abgearbeitet. */
	public List<FilterRule> rules = new ArrayList<>();

	/** Gefilterte Nachrichten trotzdem ins Log schreiben. */
	public boolean logFilteredMessages = false;

	public ChatWindowConfig findWindow(String id) {
		if (id == null || id.isEmpty()) {
			return null;
		}

		for (ChatWindowConfig window : windows) {
			if (window.id.equals(id)) {
				return window;
			}
		}

		return null;
	}

	public FilterRule findRule(String id) {
		for (FilterRule rule : rules) {
			if (rule.id.equals(id)) {
				return rule;
			}
		}

		return null;
	}

	/**
	 * Beispielkonfiguration beim ersten Start: ein Fenster oben rechts und eine
	 * (noch deaktivierte) Regel, die private Nachrichten dorthin umleitet.
	 */
	public static ChatfilterConfig createDefault() {
		ChatfilterConfig config = new ChatfilterConfig();

		ChatWindowConfig second = new ChatWindowConfig("Zweiter Chat");
		second.x = 4;
		second.y = 4;
		second.width = 220;
		second.height = 90;
		second.scale = 0.8F;
		config.windows.add(second);

		FilterRule pm = new FilterRule("Private Nachrichten", MatchType.REGEX, "^(?:\\[.*\\] )?(?:Von|To|From|An) ", RuleAction.ROUTE);
		pm.targetWindowId = second.id;
		pm.enabled = false;
		config.rules.add(pm);

		FilterRule join = new FilterRule("Join-/Leave-Spam ausblenden", MatchType.REGEX, " (joined|left) the game$", RuleAction.HIDE);
		join.enabled = false;
		config.rules.add(join);

		return config;
	}
}
