package de.biggamba.chatfilter.window;

import de.biggamba.chatfilter.config.ChatWindowConfig;
import de.biggamba.chatfilter.config.ConfigManager;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hält für jedes konfigurierte Fenster den Laufzeitzustand und hält ihn mit der Config synchron.
 */
public final class ChatWindowManager {
	private static final Map<String, ChatWindow> WINDOWS = new LinkedHashMap<>();

	private ChatWindowManager() {
	}

	/** Gleicht die Fensterliste mit der Config ab (nach Laden/Ändern der Config aufrufen). */
	public static void sync() {
		List<ChatWindowConfig> configured = ConfigManager.get().windows;
		WINDOWS.keySet().removeIf(id -> configured.stream().noneMatch(window -> window.id.equals(id)));

		for (ChatWindowConfig config : configured) {
			ChatWindow window = WINDOWS.get(config.id);

			if (window == null) {
				WINDOWS.put(config.id, new ChatWindow(config));
			} else {
				window.updateConfig(config);
			}
		}
	}

	public static List<ChatWindow> windows() {
		return new ArrayList<>(WINDOWS.values());
	}

	public static ChatWindow get(String id) {
		return WINDOWS.get(id);
	}

	public static void addMessage(String windowId, Component message, int tick) {
		ChatWindow window = WINDOWS.get(windowId);

		if (window != null) {
			window.addMessage(message, tick);
		}
	}

	/**
	 * Nachricht an alle Fenster, die den Hauptchat spiegeln.
	 *
	 * @param alreadyRouted Fenster, die die Nachricht schon über eine Regel bekommen haben
	 */
	public static void mirror(Component message, int tick, List<String> alreadyRouted) {
		for (ChatWindow window : WINDOWS.values()) {
			if (window.config().mirrorMainChat && !alreadyRouted.contains(window.config().id)) {
				window.addMessage(message, tick);
			}
		}
	}

	public static void clearAll() {
		WINDOWS.values().forEach(ChatWindow::clear);
	}
}
