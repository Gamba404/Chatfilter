package de.biggamba.chatfilter.config;

import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Eine einzelne Filterregel. Wird als JSON in der Config gespeichert,
 * daher bewusst nur einfache Felder und ein Default-Konstruktor.
 */
public class FilterRule {
	public String id = UUID.randomUUID().toString();
	public String name = "Neue Regel";
	public boolean enabled = true;

	public MatchType matchType = MatchType.CONTAINS;
	public String pattern = "";
	public boolean ignoreCase = true;
	/** Regel trifft zu, wenn das Muster NICHT passt. */
	public boolean invert = false;

	public RuleAction action = RuleAction.ROUTE;
	/** Ziel-Fenster für {@link RuleAction#ROUTE}. */
	public String targetWindowId = "";
	/** Bei ROUTE: Nachricht zusätzlich im Haupt-Chat lassen. */
	public boolean keepInMainChat = false;

	public boolean playSound = false;
	public String soundId = "minecraft:entity.experience_orb.pickup";
	public float soundVolume = 1.0F;
	public float soundPitch = 1.0F;

	/** Farbe für {@link RuleAction#HIGHLIGHT} als RGB (ohne Alpha). */
	public int highlightColor = 0xFFFF55;

	/** Nach einem Treffer keine weiteren Regeln mehr prüfen. */
	public boolean stopProcessing = true;

	/** Kompiliertes Muster – wird nicht serialisiert. */
	private transient Pattern compiled;
	private transient String compiledFor;
	private transient boolean compiledIgnoreCase;
	private transient MatchType compiledType;
	private transient String error;

	public FilterRule() {
	}

	public FilterRule(String name, MatchType matchType, String pattern, RuleAction action) {
		this.name = name;
		this.matchType = matchType;
		this.pattern = pattern;
		this.action = action;
	}

	/**
	 * Prüft, ob diese Regel auf den übergebenen (bereits von Formatierungscodes befreiten) Text zutrifft.
	 */
	public boolean matches(String plainText) {
		if (!enabled || pattern == null || pattern.isEmpty()) {
			return false;
		}

		boolean hit;
		switch (matchType) {
			case CONTAINS -> hit = ignoreCase
					? plainText.toLowerCase().contains(pattern.toLowerCase())
					: plainText.contains(pattern);
			case STARTS_WITH -> hit = ignoreCase
					? plainText.toLowerCase().startsWith(pattern.toLowerCase())
					: plainText.startsWith(pattern);
			case ENDS_WITH -> hit = ignoreCase
					? plainText.toLowerCase().endsWith(pattern.toLowerCase())
					: plainText.endsWith(pattern);
			case EXACT -> hit = ignoreCase
					? plainText.equalsIgnoreCase(pattern)
					: plainText.equals(pattern);
			case WILDCARD, REGEX -> {
				Pattern p = getCompiledPattern();
				hit = p != null && p.matcher(plainText).find();
			}
			default -> hit = false;
		}

		return invert != hit;
	}

	/**
	 * @return das kompilierte Pattern oder {@code null}, wenn die Regex ungültig ist.
	 */
	public Pattern getCompiledPattern() {
		if (compiled != null
				&& pattern.equals(compiledFor)
				&& ignoreCase == compiledIgnoreCase
				&& matchType == compiledType) {
			return compiled;
		}

		compiledFor = pattern;
		compiledIgnoreCase = ignoreCase;
		compiledType = matchType;
		error = null;

		String regex = matchType == MatchType.WILDCARD ? wildcardToRegex(pattern) : pattern;
		int flags = ignoreCase ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0;

		try {
			compiled = Pattern.compile(regex, flags);
		} catch (PatternSyntaxException e) {
			compiled = null;
			error = e.getDescription();
		}

		return compiled;
	}

	/**
	 * @return Fehlermeldung der zuletzt fehlgeschlagenen Kompilierung oder {@code null}.
	 */
	public String getError() {
		if (matchType == MatchType.REGEX || matchType == MatchType.WILDCARD) {
			getCompiledPattern();
			return error;
		}
		return null;
	}

	public boolean isValid() {
		return getError() == null;
	}

	private static String wildcardToRegex(String wildcard) {
		StringBuilder sb = new StringBuilder("^");
		for (int i = 0; i < wildcard.length(); i++) {
			char c = wildcard.charAt(i);
			switch (c) {
				case '*' -> sb.append(".*");
				case '?' -> sb.append('.');
				default -> sb.append(Pattern.quote(String.valueOf(c)));
			}
		}
		return sb.append('$').toString();
	}

	public FilterRule copy() {
		FilterRule copy = new FilterRule();
		copy.id = UUID.randomUUID().toString();
		copy.name = name;
		copy.enabled = enabled;
		copy.matchType = matchType;
		copy.pattern = pattern;
		copy.ignoreCase = ignoreCase;
		copy.invert = invert;
		copy.action = action;
		copy.targetWindowId = targetWindowId;
		copy.keepInMainChat = keepInMainChat;
		copy.playSound = playSound;
		copy.soundId = soundId;
		copy.soundVolume = soundVolume;
		copy.soundPitch = soundPitch;
		copy.highlightColor = highlightColor;
		copy.stopProcessing = stopProcessing;
		return copy;
	}
}
