package de.biggamba.chatfilter.gui;

import de.biggamba.chatfilter.config.ChatWindowConfig;
import de.biggamba.chatfilter.config.ConfigManager;
import de.biggamba.chatfilter.window.ChatWindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Einstellungen eines einzelnen Chat-Fensters.
 */
public class WindowEditScreen extends Screen {
	private final Screen parent;
	private final ChatWindowConfig window;

	public WindowEditScreen(Screen parent, ChatWindowConfig window) {
		super(Component.literal("Fenster bearbeiten"));
		this.parent = parent;
		this.window = window;
	}

	@Override
	protected void init() {
		int left = this.width / 2 - 160;
		int right = this.width / 2 + 6;

		intRow(left, 46, "X", () -> this.window.x, value -> this.window.x = value, 2, -4096, 4096);
		intRow(left, 70, "Y", () -> this.window.y, value -> this.window.y = value, 2, -4096, 4096);
		intRow(left, 94, "Breite", () -> this.window.width, value -> this.window.width = value, 5, 40, 2048);
		intRow(left, 118, "Höhe", () -> this.window.height, value -> this.window.height = value, 5, 20, 2048);
		floatRow(left, 142, "Skalierung", () -> this.window.scale, value -> this.window.scale = value, 0.05F, 0.25F, 3.0F);
		floatRow(left, 166, "Deckkraft", () -> this.window.backgroundOpacity,
				value -> this.window.backgroundOpacity = value, 0.05F, 0.0F, 1.0F);
		intRow(left, 190, "Ausblenden (s)", () -> this.window.fadeOutSeconds,
				value -> this.window.fadeOutSeconds = value, 1, 1, 600);
		intRow(left, 214, "Max. Nachrichten", () -> this.window.maxMessages,
				value -> this.window.maxMessages = value, 25, 10, 1000);

		EditBox nameBox = new EditBox(this.font, right, 46, 154, 20, Component.literal("Name"));
		nameBox.setMaxLength(48);
		nameBox.setValue(this.window.name);
		nameBox.setResponder(value -> this.window.name = value);
		this.addRenderableWidget(nameBox);

		toggle(right, 70, "Aktiv", this.window.enabled, () -> this.window.enabled = !this.window.enabled);
		toggle(right, 94, "Titel", this.window.showTitle, () -> this.window.showTitle = !this.window.showTitle);
		toggle(right, 118, "Immer sichtbar", this.window.alwaysVisible,
				() -> this.window.alwaysVisible = !this.window.alwaysVisible);
		toggle(right, 142, "Zeitstempel", this.window.showTimestamps,
				() -> this.window.showTimestamps = !this.window.showTimestamps);
		toggle(right, 166, "Hauptchat spiegeln", this.window.mirrorMainChat,
				() -> this.window.mirrorMainChat = !this.window.mirrorMainChat);

		this.addRenderableWidget(Button.builder(
						Component.literal("Ausrichtung: " + (this.window.anchorTop ? "Oben" : "Unten")),
						button -> {
							this.window.anchorTop = !this.window.anchorTop;
							ConfigManager.save();
							this.rebuildWidgets();
						})
				.bounds(right, 190, 154, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Mit Maus anordnen"),
						button -> this.minecraft.gui.setScreen(new LayoutScreen(this)))
				.bounds(right, 214, 154, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Testnachricht senden"), button -> {
			int tick = Minecraft.getInstance().gui.hud.getGuiTicks();
			ChatWindowManager.addMessage(this.window.id,
					Component.literal("Testnachricht in \"" + this.window.name + "\" um "
							+ java.time.LocalTime.now().withNano(0)), tick);
		}).bounds(right, 238, 154, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Fertig"), button -> this.onClose())
				.bounds(this.width / 2 - 100, Math.max(266, this.height - 28), 200, 20).build());
	}

	private void toggle(int x, int y, String label, boolean value, Runnable action) {
		this.addRenderableWidget(Button.builder(
						Component.literal(label + ": " + (value ? "An" : "Aus")),
						button -> {
							action.run();
							ConfigManager.save();
							this.rebuildWidgets();
						})
				.bounds(x, y, 154, 20).build());
	}

	private void intRow(int x, int y, String label, IntSupplier getter, IntConsumer setter, int step, int min, int max) {
		this.addRenderableWidget(Button.builder(Component.literal("-"), button -> {
			setter.accept(Mth.clamp(getter.getAsInt() - step, min, max));
			this.rebuildWidgets();
		}).bounds(x, y, 20, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal(label + ": " + getter.getAsInt()), button -> {
						})
				.bounds(x + 22, y, 110, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("+"), button -> {
			setter.accept(Mth.clamp(getter.getAsInt() + step, min, max));
			this.rebuildWidgets();
		}).bounds(x + 134, y, 20, 20).build());
	}

	private void floatRow(int x, int y, String label, FloatSupplier getter, FloatConsumer setter,
			float step, float min, float max) {
		this.addRenderableWidget(Button.builder(Component.literal("-"), button -> {
			setter.accept(Mth.clamp(getter.get() - step, min, max));
			this.rebuildWidgets();
		}).bounds(x, y, 20, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal(label + ": " + String.format("%.2f", getter.get())), button -> {
						})
				.bounds(x + 22, y, 110, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("+"), button -> {
			setter.accept(Mth.clamp(getter.get() + step, min, max));
			this.rebuildWidgets();
		}).bounds(x + 134, y, 20, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFFFF);
		graphics.centeredText(this.font,
				Component.literal("Regeln mit Ziel \"" + this.window.name + "\" schicken Nachrichten hierher."),
				this.width / 2, 28, 0xFF808080);
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		ChatWindowManager.sync();
		this.minecraft.gui.setScreen(this.parent);
	}

	@FunctionalInterface
	private interface FloatSupplier {
		float get();
	}

	@FunctionalInterface
	private interface FloatConsumer {
		void accept(float value);
	}
}
