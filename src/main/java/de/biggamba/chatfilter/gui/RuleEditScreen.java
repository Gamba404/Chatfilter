package de.biggamba.chatfilter.gui;

import de.biggamba.chatfilter.config.ChatWindowConfig;
import de.biggamba.chatfilter.config.ConfigManager;
import de.biggamba.chatfilter.config.FilterRule;
import de.biggamba.chatfilter.config.RuleAction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Bearbeitet eine einzelne Filterregel. Änderungen wirken sofort und werden
 * beim Verlassen gespeichert.
 */
public class RuleEditScreen extends Screen {
	private final Screen parent;
	private final FilterRule rule;

	private String testText = "";

	public RuleEditScreen(Screen parent, FilterRule rule) {
		super(Component.literal("Regel bearbeiten"));
		this.parent = parent;
		this.rule = rule;
	}

	@Override
	protected void init() {
		int left = this.width / 2 - 152;
		int right = left + 154;

		EditBox nameBox = new EditBox(this.font, left, 46, 304, 20, Component.literal("Name"));
		nameBox.setMaxLength(64);
		nameBox.setValue(this.rule.name);
		nameBox.setResponder(value -> this.rule.name = value);
		this.addRenderableWidget(nameBox);

		EditBox patternBox = new EditBox(this.font, left, 80, 304, 20, Component.literal("Muster"));
		patternBox.setMaxLength(512);
		patternBox.setValue(this.rule.pattern);
		patternBox.setResponder(value -> this.rule.pattern = value);
		this.addRenderableWidget(patternBox);

		this.addRenderableWidget(Button.builder(
						Component.literal("Typ: " + this.rule.matchType.getDisplayName()),
						button -> {
							this.rule.matchType = this.rule.matchType.next();
							this.rebuildWidgets();
						})
				.bounds(left, 104, 150, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Groß/klein egal: " + onOff(this.rule.ignoreCase)),
						button -> {
							this.rule.ignoreCase = !this.rule.ignoreCase;
							this.rebuildWidgets();
						})
				.bounds(right, 104, 150, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Umkehren: " + onOff(this.rule.invert)),
						button -> {
							this.rule.invert = !this.rule.invert;
							this.rebuildWidgets();
						})
				.bounds(left, 128, 150, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Aktion: " + this.rule.action.getDisplayName()),
						button -> {
							this.rule.action = this.rule.action.next();
							this.rebuildWidgets();
						})
				.bounds(right, 128, 150, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Ziel: " + targetName()),
						button -> {
							cycleTarget();
							this.rebuildWidgets();
						})
				.bounds(left, 152, 150, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Auch im Hauptchat: " + onOff(this.rule.keepInMainChat)),
						button -> {
							this.rule.keepInMainChat = !this.rule.keepInMainChat;
							this.rebuildWidgets();
						})
				.bounds(right, 152, 150, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Danach stoppen: " + onOff(this.rule.stopProcessing)),
						button -> {
							this.rule.stopProcessing = !this.rule.stopProcessing;
							this.rebuildWidgets();
						})
				.bounds(left, 176, 150, 20).build());

		this.addRenderableWidget(Button.builder(
						Component.literal("Sound: " + onOff(this.rule.playSound)),
						button -> {
							this.rule.playSound = !this.rule.playSound;
							this.rebuildWidgets();
						})
				.bounds(right, 176, 150, 20).build());

		EditBox soundBox = new EditBox(this.font, left, 210, 150, 20, Component.literal("Sound"));
		soundBox.setMaxLength(128);
		soundBox.setValue(this.rule.soundId);
		soundBox.setResponder(value -> this.rule.soundId = value);
		this.addRenderableWidget(soundBox);

		EditBox colorBox = new EditBox(this.font, right, 210, 150, 20, Component.literal("Farbe"));
		colorBox.setMaxLength(7);
		colorBox.setValue(String.format("#%06X", this.rule.highlightColor & 0xFFFFFF));
		colorBox.setResponder(value -> {
			try {
				this.rule.highlightColor = Integer.parseInt(value.replace("#", "").trim(), 16) & 0xFFFFFF;
			} catch (NumberFormatException ignored) {
				// Ungültige Eingabe einfach ignorieren.
			}
		});
		this.addRenderableWidget(colorBox);

		EditBox testBox = new EditBox(this.font, left, 244, 304, 20, Component.literal("Testnachricht"));
		testBox.setMaxLength(256);
		testBox.setValue(this.testText);
		testBox.setResponder(value -> this.testText = value);
		this.addRenderableWidget(testBox);

		this.addRenderableWidget(Button.builder(Component.literal("Fertig"), button -> this.onClose())
				.bounds(this.width / 2 - 100, 290, 200, 20).build());
	}

	private String targetName() {
		ChatWindowConfig window = ConfigManager.get().findWindow(this.rule.targetWindowId);
		return window != null ? window.name : "– kein Fenster –";
	}

	private void cycleTarget() {
		List<ChatWindowConfig> windows = ConfigManager.get().windows;

		if (windows.isEmpty()) {
			this.rule.targetWindowId = "";
			return;
		}

		int index = -1;

		for (int i = 0; i < windows.size(); i++) {
			if (windows.get(i).id.equals(this.rule.targetWindowId)) {
				index = i;
				break;
			}
		}

		this.rule.targetWindowId = windows.get((index + 1) % windows.size()).id;
	}

	private static String onOff(boolean value) {
		return value ? "An" : "Aus";
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);

		int left = this.width / 2 - 152;
		int right = left + 154;

		graphics.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFFFF);
		graphics.text(this.font, Component.literal("Name"), left, 36, 0xFFA0A0A0);
		graphics.text(this.font, Component.literal("Muster (" + this.rule.matchType.getDisplayName() + ")"), left, 70, 0xFFA0A0A0);
		graphics.text(this.font, Component.literal("Sound-ID"), left, 200, 0xFFA0A0A0);
		graphics.text(this.font, Component.literal("Hervorhebungsfarbe"), right, 200, 0xFFA0A0A0);
		graphics.text(this.font, Component.literal("Testnachricht"), left, 234, 0xFFA0A0A0);

		String error = this.rule.getError();

		if (error != null) {
			graphics.text(this.font, Component.literal("Ungültiges Muster: " + error)
					.withStyle(ChatFormatting.RED), left, 268, 0xFFFF5555);
		} else if (!this.testText.isEmpty()) {
			boolean hit = this.rule.matches(this.testText);
			Component result = hit
					? Component.literal("Trifft zu → " + this.rule.action.getDisplayName()).withStyle(ChatFormatting.GREEN)
					: Component.literal("Trifft nicht zu").withStyle(ChatFormatting.GRAY);
			graphics.text(this.font, result, left, 268, 0xFFFFFFFF);
		}

		if (this.rule.action == RuleAction.ROUTE && ConfigManager.get().findWindow(this.rule.targetWindowId) == null) {
			graphics.centeredText(this.font,
					Component.literal("Achtung: kein Ziel-Fenster gewählt – die Nachricht verschwindet sonst.")
							.withStyle(ChatFormatting.YELLOW),
					this.width / 2, 278, 0xFFFFFF55);
		}
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		this.minecraft.gui.setScreen(this.parent);
	}
}
