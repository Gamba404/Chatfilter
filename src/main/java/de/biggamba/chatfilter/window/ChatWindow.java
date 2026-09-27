package de.biggamba.chatfilter.window;

import de.biggamba.chatfilter.config.ChatWindowConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Laufzeitzustand eines eigenen Chat-Fensters: gespeicherte Nachrichten,
 * umgebrochene Zeilen und das Rendering.
 *
 * <p>Nachrichten und Zeilen werden – wie im Vanilla-Chat – neueste zuerst gehalten.
 */
public class ChatWindow {
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
	private static final int LINE_HEIGHT = 9;
	private static final int TITLE_HEIGHT = 10;

	private ChatWindowConfig config;

	private final Deque<StoredMessage> messages = new ArrayDeque<>();
	private final List<RenderedLine> lines = new ArrayList<>();

	/** Breite, mit der die Zeilen zuletzt umgebrochen wurden (-1 = noch nie). */
	private int wrappedWidth = -1;
	private boolean wrappedTimestamps;
	private boolean dirty = true;
	private int scroll;
	private int unread;
	private Font font;

	public ChatWindow(ChatWindowConfig config) {
		this.config = config;
	}

	public ChatWindowConfig config() {
		return config;
	}

	public void updateConfig(ChatWindowConfig config) {
		this.config = config;
		this.dirty = true;
	}

	public int unread() {
		return unread;
	}

	public void clearUnread() {
		unread = 0;
	}

	public void clear() {
		messages.clear();
		lines.clear();
		scroll = 0;
		unread = 0;
	}

	public void addMessage(Component content, int tick) {
		messages.addFirst(new StoredMessage(content, tick, LocalTime.now().format(TIME_FORMAT)));

		while (messages.size() > Math.max(10, config.maxMessages)) {
			messages.removeLast();
		}

		unread++;

		if (font == null || wrappedWidth < 0) {
			// Fenster wurde noch nie gezeichnet – beim ersten Rendern komplett umbrechen.
			dirty = true;
		} else {
			// Nur die neue Nachricht umbrechen und vorne einfügen. Index 0 ist die
			// unterste Zeile, deshalb landet die *letzte* Zeile der Nachricht vorne.
			List<RenderedLine> newLines = wrap(messages.peekFirst(), wrappedWidth);

			for (RenderedLine line : newLines) {
				lines.addFirst(line);
			}

			if (scroll > 0) {
				scroll += newLines.size();
			}

			trimLines();
		}
	}

	private void trimLines() {
		int max = Math.max(10, config.maxMessages) * 4;

		while (lines.size() > max) {
			lines.removeLast();
		}
	}

	/** Bricht bei Bedarf alle Nachrichten neu um (z. B. nach Größenänderung). */
	private void ensureWrapped() {
		int width = config.contentWidth();

		if (!dirty && width == wrappedWidth && wrappedTimestamps == config.showTimestamps) {
			return;
		}

		wrappedWidth = width;
		wrappedTimestamps = config.showTimestamps;
		dirty = false;
		lines.clear();

		for (StoredMessage message : messages) {
			// Innerhalb einer Nachricht rückwärts, damit die erste Zeile oben steht.
			List<RenderedLine> wrapped = wrap(message, width);

			for (int i = wrapped.size() - 1; i >= 0; i--) {
				lines.add(wrapped.get(i));
			}
		}

		trimLines();
		scroll = Math.min(scroll, Math.max(0, lines.size() - visibleLineCount()));
	}

	private List<RenderedLine> wrap(StoredMessage message, int width) {
		Component display = message.content();

		if (config.showTimestamps) {
			display = Component.empty()
					.append(Component.literal("[" + message.timestamp() + "] ").withStyle(ChatFormatting.GRAY))
					.append(message.content());
		}

		List<RenderedLine> result = new ArrayList<>();

		if (font == null) {
			return result;
		}

		for (FormattedCharSequence sequence : ComponentRenderUtils.wrapComponents(display, width, font)) {
			result.add(new RenderedLine(sequence, message.addedTick()));
		}

		return result;
	}

	public int visibleLineCount() {
		int inner = config.contentHeight() - (config.showTitle ? TITLE_HEIGHT : 0);
		return Math.max(1, inner / LINE_HEIGHT);
	}

	public void scroll(int amount) {
		scroll = Mth.clamp(scroll + amount, 0, Math.max(0, lines.size() - visibleLineCount()));
	}

	public void resetScroll() {
		scroll = 0;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= config.x && mouseX <= config.x + config.width
				&& mouseY >= config.y && mouseY <= config.y + config.height;
	}

	/**
	 * Berechnet Position und Deckkraft aller sichtbaren Zeilen – gemeinsam genutzt
	 * von Rendering und Klick-Erkennung, damit beide garantiert dasselbe Layout sehen.
	 */
	private void layout(int ticks, boolean forceVisible, LineVisitor visitor) {
		ensureWrapped();

		int titleHeight = config.showTitle ? TITLE_HEIGHT : 0;
		int localHeight = (int) (config.height / Math.max(0.25F, config.scale));
		int bottom = localHeight - 2;

		// Anzahl tatsächlich sichtbarer Zeilen – nötig, damit der Block bei
		// "anchorTop" von oben nach unten wächst.
		int count = Math.min(visibleLineCount(), Math.max(0, lines.size() - scroll));

		for (int i = 0; i < count; i++) {
			RenderedLine line = lines.get(i + scroll);
			float alpha = alphaFor(line, ticks, forceVisible);

			if (alpha <= 0.01F) {
				continue;
			}

			// i == 0 ist die neueste Zeile und steht in beiden Modi unten.
			int y = config.anchorTop
					? titleHeight + 1 + (count - 1 - i) * LINE_HEIGHT
					: bottom - (i + 1) * LINE_HEIGHT;

			if (y < titleHeight || y + LINE_HEIGHT > localHeight) {
				continue;
			}

			visitor.accept(line, y, alpha);
		}
	}

	/** Transformation vom Fenster-Innenraum in Bildschirmkoordinaten. */
	private Matrix3x2f pose() {
		float scale = Math.max(0.25F, config.scale);
		Matrix3x2f pose = new Matrix3x2f();
		pose.translate((float) config.x, (float) config.y);
		pose.scale(scale, scale);
		return pose;
	}

	/**
	 * Zeichnet das Fenster.
	 *
	 * @param chatOpen ob gerade der Chat geöffnet ist (dann kein Ausblenden und
	 *                 Hover-Tooltips wie im Vanilla-Chat)
	 */
	public void render(GuiGraphicsExtractor graphics, Font font, int ticks, boolean chatOpen) {
		this.font = font;

		boolean forceVisible = chatOpen || config.alwaysVisible;
		float[] maxAlpha = {0.0F};
		layout(ticks, forceVisible, (line, y, alpha) -> maxAlpha[0] = Math.max(maxAlpha[0], alpha));

		if (forceVisible) {
			maxAlpha[0] = 1.0F;
		}

		if (maxAlpha[0] <= 0.01F) {
			return;
		}

		float scale = Math.max(0.25F, config.scale);
		int localWidth = (int) (config.width / scale);
		int localHeight = (int) (config.height / scale);

		graphics.pose().pushMatrix();
		graphics.pose().translate((float) config.x, (float) config.y);
		graphics.pose().scale(scale, scale);
		graphics.enableScissor(0, 0, localWidth, localHeight);

		graphics.fill(0, 0, localWidth, localHeight, ARGB.black(maxAlpha[0] * config.backgroundOpacity));

		if (config.showTitle) {
			graphics.fill(0, 0, localWidth, TITLE_HEIGHT, ARGB.black(maxAlpha[0] * config.backgroundOpacity * 0.8F));
			String title = config.name + (scroll > 0 ? " (+" + scroll + ")" : "");
			graphics.text(font, title, 2, 1, ARGB.color(maxAlpha[0], 0xA0A0A0));
		}

		// Über den ActiveTextCollector statt graphics.text(): damit erkennt Minecraft
		// Hover-Events (Item-Tooltips, Spielerinfos, ...) und Klick-Events genauso wie
		// im Hauptchat. Der Collector übernimmt Pose und Scissor beim Erzeugen.
		ActiveTextCollector collector = graphics.textRenderer(chatOpen
				? GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR
				: GuiGraphicsExtractor.HoveredTextEffects.NONE);
		ActiveTextCollector.Parameters parameters = collector.defaultParameters();

		layout(ticks, forceVisible, (line, y, alpha) ->
				collector.accept(TextAlignment.LEFT, 2, y, parameters.withOpacity(alpha), line.text()));

		graphics.disableScissor();
		graphics.pose().popMatrix();
	}

	/**
	 * Spielt das Layout gegen einen {@link ActiveTextCollector} durch, ohne zu zeichnen –
	 * so findet der {@code ClickableStyleFinder} anklickbare Stellen an derselben Position.
	 */
	public void captureClickableText(ActiveTextCollector collector, Font font, int ticks) {
		this.font = font;
		collector.defaultParameters(collector.defaultParameters().withPose(pose()));
		ActiveTextCollector.Parameters parameters = collector.defaultParameters();

		layout(ticks, true, (line, y, alpha) ->
				collector.accept(TextAlignment.LEFT, 2, y, parameters, line.text()));
	}

	private float alphaFor(RenderedLine line, int ticks, boolean forceVisible) {
		if (forceVisible) {
			return 1.0F;
		}

		int age = ticks - line.addedTick();
		int fade = config.fadeOutTicks();

		if (age >= fade) {
			return 0.0F;
		}

		double t = 1.0 - (double) age / fade;
		t = Mth.clamp(t * 10.0, 0.0, 1.0);
		return (float) (t * t);
	}

	@FunctionalInterface
	private interface LineVisitor {
		void accept(RenderedLine line, int y, float alpha);
	}

	private record StoredMessage(Component content, int addedTick, String timestamp) {
	}

	private record RenderedLine(FormattedCharSequence text, int addedTick) {
	}
}
