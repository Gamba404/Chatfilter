package de.biggamba.chatfilter.gui;

import de.biggamba.chatfilter.config.ChatWindowConfig;
import de.biggamba.chatfilter.config.ConfigManager;
import de.biggamba.chatfilter.window.ChatWindowManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Fenster mit der Maus verschieben und in der Größe ändern – wie das Anordnen
 * der Chat-Fenster in LabyMod.
 */
public class LayoutScreen extends Screen {
	private static final int HANDLE_SIZE = 8;
	private static final int SNAP_DISTANCE = 6;

	private final Screen parent;

	private ChatWindowConfig dragged;
	private boolean resizing;
	private double grabX;
	private double grabY;

	public LayoutScreen(Screen parent) {
		super(Component.literal("Fenster anordnen"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.addRenderableWidget(Button.builder(Component.literal("Fertig"), button -> this.onClose())
				.bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);

		graphics.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
		graphics.centeredText(this.font,
				Component.literal("Ziehen = verschieben · Ecke unten rechts = Größe ändern"),
				this.width / 2, 20, 0xFFA0A0A0);

		for (ChatWindowConfig window : ConfigManager.get().windows) {
			boolean hovered = isOver(window, mouseX, mouseY);
			int border = window.enabled
					? (hovered ? 0xFFFFFF55 : 0xFF55FF55)
					: 0xFF808080;

			graphics.fill(window.x, window.y, window.x + window.width, window.y + window.height,
					ARGB.black(Math.max(0.35F, window.backgroundOpacity)));

			// Rahmen
			graphics.fill(window.x, window.y, window.x + window.width, window.y + 1, border);
			graphics.fill(window.x, window.y + window.height - 1, window.x + window.width, window.y + window.height, border);
			graphics.fill(window.x, window.y, window.x + 1, window.y + window.height, border);
			graphics.fill(window.x + window.width - 1, window.y, window.x + window.width, window.y + window.height, border);

			// Anfasser unten rechts
			graphics.fill(window.x + window.width - HANDLE_SIZE, window.y + window.height - HANDLE_SIZE,
					window.x + window.width, window.y + window.height, border);

			graphics.text(this.font, Component.literal(window.name), window.x + 3, window.y + 3, 0xFFFFFFFF);
			graphics.text(this.font,
					Component.literal(window.width + "x" + window.height + " @ " + window.x + "," + window.y),
					window.x + 3, window.y + 14, 0xFFA0A0A0);

			graphics.text(this.font,
					Component.literal("Skalierung " + String.format("%.2f", window.scale)),
					window.x + 3, window.y + 25, 0xFF808080);
		}
	}

	private boolean isOver(ChatWindowConfig window, double mouseX, double mouseY) {
		return mouseX >= window.x && mouseX <= window.x + window.width
				&& mouseY >= window.y && mouseY <= window.y + window.height;
	}

	private boolean isOverHandle(ChatWindowConfig window, double mouseX, double mouseY) {
		return mouseX >= window.x + window.width - HANDLE_SIZE && mouseX <= window.x + window.width
				&& mouseY >= window.y + window.height - HANDLE_SIZE && mouseY <= window.y + window.height;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}

		List<ChatWindowConfig> windows = ConfigManager.get().windows;

		// Von hinten nach vorne: das zuletzt gezeichnete Fenster liegt oben.
		for (int i = windows.size() - 1; i >= 0; i--) {
			ChatWindowConfig window = windows.get(i);

			if (!isOver(window, event.x(), event.y())) {
				continue;
			}

			this.dragged = window;
			this.resizing = isOverHandle(window, event.x(), event.y());
			this.grabX = event.x() - window.x;
			this.grabY = event.y() - window.y;
			return true;
		}

		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.dragged == null) {
			return super.mouseDragged(event, dx, dy);
		}

		if (this.resizing) {
			this.dragged.width = Mth.clamp((int) (event.x() - this.dragged.x), 40, this.width);
			this.dragged.height = Mth.clamp((int) (event.y() - this.dragged.y), 20, this.height);
		} else {
			this.dragged.x = Mth.clamp((int) (event.x() - this.grabX), -this.dragged.width + 20, this.width - 20);
			this.dragged.y = Mth.clamp((int) (event.y() - this.grabY), 0, this.height - 10);
			snap(this.dragged);
		}

		return true;
	}

	private void snap(ChatWindowConfig window) {
		if (Math.abs(window.x) <= SNAP_DISTANCE) {
			window.x = 0;
		} else if (Math.abs(window.x + window.width - this.width) <= SNAP_DISTANCE) {
			window.x = this.width - window.width;
		}

		if (Math.abs(window.y) <= SNAP_DISTANCE) {
			window.y = 0;
		} else if (Math.abs(window.y + window.height - this.height) <= SNAP_DISTANCE) {
			window.y = this.height - window.height;
		}
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.dragged != null) {
			this.dragged = null;
			this.resizing = false;
			ConfigManager.save();
			return true;
		}

		return super.mouseReleased(event);
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		ChatWindowManager.sync();
		this.minecraft.gui.setScreen(this.parent);
	}
}
