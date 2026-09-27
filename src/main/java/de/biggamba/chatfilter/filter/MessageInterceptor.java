package de.biggamba.chatfilter.filter;

import de.biggamba.chatfilter.ChatfilterMod;
import de.biggamba.chatfilter.config.ChatfilterConfig;
import de.biggamba.chatfilter.config.ConfigManager;
import de.biggamba.chatfilter.config.FilterRule;
import de.biggamba.chatfilter.window.ChatWindowManager;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Hängt sich in den Nachrichtenempfang des Clients ein und wendet die Filterregeln an.
 */
public final class MessageInterceptor {
	private MessageInterceptor() {
	}

	public static void register() {
		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			// Nachrichten in der Actionbar werden nicht gefiltert.
			return overlay || handle(message);
		});

		ClientReceiveMessageEvents.ALLOW_CHAT.register(
				(message, signedMessage, sender, params, receptionTimestamp) -> handle(message));
	}

	/**
	 * @return {@code true}, wenn die Nachricht ganz normal im Hauptchat erscheinen soll
	 */
	private static boolean handle(Component message) {
		ChatfilterConfig config = ConfigManager.get();

		if (!config.enabled) {
			return true;
		}

		Minecraft minecraft = Minecraft.getInstance();
		int tick = minecraft.gui.hud.getGuiTicks();

		FilterResult result = FilterEngine.process(config, message.getString());

		Component forWindows = result.highlightColor != null
				? highlight(message, result.highlightColor)
				: message;

		for (String windowId : result.targetWindows) {
			ChatWindowManager.addMessage(windowId, forWindows, tick);
		}

		ChatWindowManager.mirror(forWindows, tick, result.targetWindows);

		if (result.soundRule != null) {
			playSound(result.soundRule);
		}

		if (result.hideFromMainChat) {
			if (config.logFilteredMessages) {
				ChatfilterMod.LOGGER.info("[gefiltert] {}", message.getString());
			}

			return false;
		}

		if (result.highlightColor != null) {
			// Zum Einfärben muss die Nachricht ersetzt werden: Original abbrechen,
			// eingefärbte Kopie selbst in den Chat legen.
			minecraft.gui.hud.getChat().addClientSystemMessage(forWindows);
			return false;
		}

		return true;
	}

	private static Component highlight(Component message, int rgb) {
		return Component.empty()
				.append(message)
				.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
	}

	private static void playSound(FilterRule rule) {
		Identifier id = Identifier.tryParse(rule.soundId);

		if (id == null) {
			return;
		}

		SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(id);

		if (sound == null) {
			return;
		}

		Minecraft.getInstance().getSoundManager()
				.play(SimpleSoundInstance.forUI(sound, rule.soundPitch, rule.soundVolume));
	}
}
