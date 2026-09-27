package de.biggamba.chatfilter.filter;

import de.biggamba.chatfilter.config.FilterRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Ergebnis der Regelauswertung für eine einzelne Nachricht.
 */
public class FilterResult {
	/** Nachricht soll nicht mehr im Hauptchat erscheinen. */
	public boolean hideFromMainChat;
	/** IDs der Fenster, in die die Nachricht zusätzlich soll. */
	public final List<String> targetWindows = new ArrayList<>();
	/** Farbe für Hervorhebung oder {@code null}. */
	public Integer highlightColor;
	/** Regel, die den Sound ausgelöst hat, oder {@code null}. */
	public FilterRule soundRule;
	/** Erste zutreffende Regel (für Debug-Ausgaben). */
	public FilterRule firstMatch;

	public boolean matched() {
		return firstMatch != null;
	}
}
