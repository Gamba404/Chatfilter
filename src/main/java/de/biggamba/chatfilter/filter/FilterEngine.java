package de.biggamba.chatfilter.filter;

import de.biggamba.chatfilter.config.ChatfilterConfig;
import de.biggamba.chatfilter.config.FilterRule;

/**
 * Wertet alle Regeln der Reihe nach gegen den reinen Text einer Nachricht aus.
 */
public final class FilterEngine {
	private FilterEngine() {
	}

	public static FilterResult process(ChatfilterConfig config, String plainText) {
		FilterResult result = new FilterResult();

		if (!config.enabled) {
			return result;
		}

		for (FilterRule rule : config.rules) {
			if (!rule.enabled || !rule.matches(plainText)) {
				continue;
			}

			if (result.firstMatch == null) {
				result.firstMatch = rule;
			}

			switch (rule.action) {
				case HIDE -> {
					result.hideFromMainChat = true;
					result.targetWindows.clear();
				}
				case ROUTE -> {
					if (!rule.targetWindowId.isEmpty() && !result.targetWindows.contains(rule.targetWindowId)) {
						result.targetWindows.add(rule.targetWindowId);
					}

					if (!rule.keepInMainChat) {
						result.hideFromMainChat = true;
					}
				}
				case HIGHLIGHT -> result.highlightColor = rule.highlightColor;
				case KEEP -> {
					// Bewusst nichts tun: nützlich als Ausnahme vor einer HIDE-Regel.
				}
			}

			if (rule.playSound && result.soundRule == null) {
				result.soundRule = rule;
			}

			if (rule.stopProcessing) {
				break;
			}
		}

		// Eine versteckte Nachricht darf trotzdem in ein Fenster – aber nur, wenn
		// die HIDE-Regel nicht nach der ROUTE-Regel kam (siehe clear() oben).
		return result;
	}
}
