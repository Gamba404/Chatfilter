package de.biggamba.chatfilter.gui;

import de.biggamba.chatfilter.config.ChatWindowConfig;
import de.biggamba.chatfilter.config.ChatfilterConfig;
import de.biggamba.chatfilter.config.ConfigManager;
import de.biggamba.chatfilter.window.ChatWindowManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Liste aller eigenen Chat-Fenster.
 */
public class WindowListScreen extends Screen {
	private static final int PAGE_SIZE = 7;

	private final Screen parent;
	private int page;

	public WindowListScreen(Screen parent) {
		super(Component.literal("Chat-Fenster"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ChatfilterConfig config = ConfigManager.get();
		List<ChatWindowConfig> windows = config.windows;
		int pages = Math.max(1, (windows.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		this.page = Math.min(this.page, pages - 1);

		int left = this.width / 2 - 124;
		int top = 40;

		for (int i = 0; i < PAGE_SIZE; i++) {
			int index = this.page * PAGE_SIZE + i;

			if (index >= windows.size()) {
				break;
			}

			ChatWindowConfig window = windows.get(index);
			int y = top + i * 24;

			this.addRenderableWidget(Button.builder(
							Component.literal(window.name),
							button -> this.minecraft.gui.setScreen(new WindowEditScreen(this, window)))
					.bounds(left, y, 160, 20).build());

			this.addRenderableWidget(Button.builder(
							Component.literal(window.enabled ? "An" : "Aus")
									.withStyle(window.enabled ? ChatFormatting.GREEN : ChatFormatting.RED),
							button -> {
								window.enabled = !window.enabled;
								ConfigManager.save();
								this.rebuildWidgets();
							})
					.bounds(left + 164, y, 60, 20).build());

			this.addRenderableWidget(Button.builder(
							Component.literal("X").withStyle(ChatFormatting.RED),
							button -> {
								windows.remove(window);

								for (var rule : config.rules) {
									if (window.id.equals(rule.targetWindowId)) {
										rule.targetWindowId = "";
									}
								}

								ConfigManager.save();
								ChatWindowManager.sync();
								this.rebuildWidgets();
							})
					.bounds(left + 228, y, 20, 20).build());
		}

		int bottom = this.height - 52;

		this.addRenderableWidget(Button.builder(
						Component.literal("Neues Fenster"),
						button -> {
							ChatWindowConfig created = new ChatWindowConfig("Fenster " + (windows.size() + 1));
							created.x = 4;
							created.y = 4 + windows.size() * 8;
							windows.add(created);
							ConfigManager.save();
							ChatWindowManager.sync();
							this.minecraft.gui.setScreen(new WindowEditScreen(this, created));
						})
				.bounds(this.width / 2 - 102, bottom, 100, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Zurück"),
						button -> this.onClose())
				.bounds(this.width / 2 + 2, bottom, 100, 20).build());

		if (pages > 1) {
			this.addRenderableWidget(Button.builder(Component.literal("<"), button -> {
				this.page = (this.page - 1 + pages) % pages;
				this.rebuildWidgets();
			}).bounds(this.width / 2 - 102, bottom + 24, 20, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal(">"), button -> {
				this.page = (this.page + 1) % pages;
				this.rebuildWidgets();
			}).bounds(this.width / 2 + 82, bottom + 24, 20, 20).build());
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);

		if (ConfigManager.get().windows.isEmpty()) {
			graphics.centeredText(this.font,
					Component.literal("Noch keine Fenster – erstelle eines mit \"Neues Fenster\"."),
					this.width / 2, 60, 0xFF808080);
		}

		int pages = Math.max(1, (ConfigManager.get().windows.size() + PAGE_SIZE - 1) / PAGE_SIZE);

		if (pages > 1) {
			graphics.centeredText(this.font, Component.literal("Seite " + (this.page + 1) + "/" + pages),
					this.width / 2, this.height - 22, 0xFFA0A0A0);
		}
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		ChatWindowManager.sync();
		this.minecraft.gui.setScreen(this.parent);
	}
}
