package de.biggamba.chatfilter.config;

/**
 * Was mit einer Nachricht passiert, auf die eine Regel zutrifft.
 */
public enum RuleAction {
	/** Nachricht komplett verstecken (weder Haupt-Chat noch eigene Fenster). */
	HIDE("Verstecken"),
	/** Nachricht in das Ziel-Fenster umleiten. */
	ROUTE("In Fenster umleiten"),
	/** Nachricht bleibt, wird aber eingefärbt / markiert. */
	HIGHLIGHT("Hervorheben"),
	/** Nichts verändern – nützlich in Kombination mit Sound oder als "Ausnahme"-Regel. */
	KEEP("Behalten");

	private final String displayName;

	RuleAction(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public RuleAction next() {
		RuleAction[] values = values();
		return values[(ordinal() + 1) % values.length];
	}
}
