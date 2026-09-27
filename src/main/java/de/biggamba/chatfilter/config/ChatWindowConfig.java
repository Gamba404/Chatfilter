package de.biggamba.chatfilter.config;

import java.util.UUID;

/**
 * Einstellungen eines eigenen Chat-Fensters ("zweiter Chat").
 * Position und Größe sind GUI-Pixel (also bereits mit der GUI-Skalierung von Minecraft).
 */
public class ChatWindowConfig {
	public String id = UUID.randomUUID().toString().substring(0, 8);
	public String name = "Neues Fenster";
	public boolean enabled = true;

	/** Linke obere Ecke. */
	public int x = 4;
	public int y = 4;
	public int width = 200;
	public int height = 80;

	/** Textskalierung innerhalb des Fensters. */
	public float scale = 1.0F;
	/** Deckkraft des Hintergrunds (0 = unsichtbar). */
	public float backgroundOpacity = 0.5F;

	/** Titelzeile mit dem Fensternamen anzeigen. */
	public boolean showTitle = true;

	/**
	 * {@code true}: Nachrichten beginnen oben im Fenster und laufen nach unten.
	 * {@code false}: wie der Vanilla-Chat am unteren Rand verankert (wächst nach oben).
	 * Die Lesereihenfolge bleibt in beiden Fällen gleich – die neueste Nachricht steht unten.
	 */
	public boolean anchorTop = true;
	/** Fenster nie ausblenden (sonst wie der Vanilla-Chat nach {@link #fadeOutSeconds}). */
	public boolean alwaysVisible = false;
	public int fadeOutSeconds = 10;

	/** Zeitstempel [HH:mm] vor jede Nachricht setzen. */
	public boolean showTimestamps = false;
	/** Zusätzlich jede Nachricht anzeigen, die auch im Hauptchat landet. */
	public boolean mirrorMainChat = false;

	public int maxMessages = 200;

	public ChatWindowConfig() {
	}

	public ChatWindowConfig(String name) {
		this.name = name;
	}

	/** Innenbreite in Textpixeln (nach Skalierung). */
	public int contentWidth() {
		return Math.max(20, (int) ((width - 4) / Math.max(0.25F, scale)));
	}

	/** Innenhöhe in Textpixeln (nach Skalierung). */
	public int contentHeight() {
		return Math.max(9, (int) ((height - 4) / Math.max(0.25F, scale)));
	}

	public int fadeOutTicks() {
		return Math.max(20, fadeOutSeconds * 20);
	}
}
