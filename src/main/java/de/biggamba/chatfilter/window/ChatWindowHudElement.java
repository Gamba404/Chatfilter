package de.biggamba.chatfilter.window;

import de.biggamba.chatfilter.config.ConfigManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;

/**
 * Zeichnet alle eigenen Chat-Fenster ins HUD (direkt nach dem Vanilla-Chat).
 */
public class ChatWindowHudElement implements HudElement {
	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();

		if (!ConfigManager.get().enabled || minecraft.gui.hud.isHidden()) {
			return;
		}

		boolean chatOpen = minecraft.gui.screen() instanceof ChatScreen;
		int ticks = minecraft.gui.hud.getGuiTicks();

		for (ChatWindow window : ChatWindowManager.windows()) {
			if (window.config().enabled) {
				window.render(graphics, minecraft.font, ticks, chatOpen);
			}
		}
	}
}
