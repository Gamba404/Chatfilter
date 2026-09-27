package de.biggamba.chatfilter.gui;

import de.biggamba.chatfilter.config.ChatfilterConfig;
import de.biggamba.chatfilter.config.ConfigManager;
import de.biggamba.chatfilter.window.ChatWindowManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Hauptmenü des Mods.
 */
public class ChatfilterScreen extends Screen {
	private final @Nullable Screen parent;

	public ChatfilterScreen(@Nullable Screen parent) {
		super(Component.literal("Chatfilter"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ChatfilterConfig config = ConfigManager.get();
		int x = this.width / 2 - 100;
		int y = this.height / 4;

		this.addRenderableWidget(Button.builder(
						Component.literal("Chatfilter: " + (config.enabled ? "An" : "Aus")),
						button -> {
							config.enabled = !config.enabled;
							ConfigManager.save();
							this.rebuildWidgets();
						})
				.bounds(x, y, 200, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Chat-Fenster verwalten (" + config.windows.size() + ")"),
						button -> this.minecraft.gui.setScreen(new WindowListScreen(this)))
				.bounds(x, y + 24, 200, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Filterregeln verwalten (" + config.rules.size() + ")"),
						button -> this.minecraft.gui.setScreen(new RuleListScreen(this)))
				.bounds(x, y + 48, 200, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Fenster anordnen"),
						button -> this.minecraft.gui.setScreen(new LayoutScreen(this)))
				.bounds(x, y + 72, 200, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Konfiguration neu laden"),
						button -> {
							ConfigManager.load();
							ChatWindowManager.sync();
							this.rebuildWidgets();
						})
				.bounds(x, y + 100, 98, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Speichern"),
						button -> ConfigManager.save())
				.bounds(x + 102, y + 100, 98, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Fertig"),
						button -> this.onClose())
				.bounds(x, y + 128, 200, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(this.font, this.title, this.width / 2, this.height / 4 - 30, 0xFFFFFFFF);
		graphics.centeredText(this.font, Component.literal("Konfiguration: " + ConfigManager.path().getFileName()),
				this.width / 2, this.height / 4 - 18, 0xFF808080);
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		ChatWindowManager.sync();
		this.minecraft.gui.setScreen(this.parent);
	}
}
