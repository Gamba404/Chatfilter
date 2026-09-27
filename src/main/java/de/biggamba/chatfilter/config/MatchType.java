package de.biggamba.chatfilter.config;

/**
 * Art, wie das Muster einer Regel gegen den reinen Text einer Chatnachricht geprüft wird.
 */
public enum MatchType {
	/** Nachricht enthält das Muster irgendwo. */
	CONTAINS("Enthält"),
	/** Nachricht beginnt mit dem Muster. */
	STARTS_WITH("Beginnt mit"),
	/** Nachricht endet mit dem Muster. */
	ENDS_WITH("Endet mit"),
	/** Nachricht ist exakt das Muster. */
	EXACT("Exakt"),
	/** Muster mit Platzhaltern: {@code *} = beliebig viele Zeichen, {@code ?} = ein Zeichen. */
	WILDCARD("Platzhalter (*)"),
	/** Vollwertiger regulärer Ausdruck (java.util.regex). */
	REGEX("Regex");

	private final String displayName;

	MatchType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public MatchType next() {
		MatchType[] values = values();
		return values[(ordinal() + 1) % values.length];
	}
}
