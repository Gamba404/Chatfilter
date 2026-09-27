package de.biggamba.chatfilter.mixin;

import de.biggamba.chatfilter.config.ConfigManager;
import de.biggamba.chatfilter.window.ChatWindow;
import de.biggamba.chatfilter.window.ChatWindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bindet die eigenen Chat-Fenster an den geöffneten Chat an: Scrollen mit dem Mausrad
 * und Klicken auf Text mit Klick-Event (z. B. per Befehl in den Chat geschickte Items).
 */
@Mixin(ChatScreen.class)
public class ChatScreenMixin {
	@Inject(method = "mouseScrolled(DDDD)Z", at = @At("HEAD"), cancellable = true)
	private void chatfilter$scrollOwnWindows(double x, double y, double scrollX, double scrollY,
			CallbackInfoReturnable<Boolean> callback) {
		if (!ConfigManager.get().enabled || scrollY == 0.0) {
			return;
		}

		for (ChatWindow window : ChatWindowManager.windows()) {
			if (!window.config().enabled || !window.isMouseOver(x, y)) {
				continue;
			}

			int step = Minecraft.getInstance().hasShiftDown() ? 1 : 3;
			window.scroll((int) Math.signum(scrollY) * step);
			callback.setReturnValue(true);
			return;
		}
	}

	@Inject(method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z",
			at = @At("HEAD"), cancellable = true)
	private void chatfilter$clickOwnWindows(MouseButtonEvent event, boolean doubleClick,
			CallbackInfoReturnable<Boolean> callback) {
		if (!ConfigManager.get().enabled || event.button() != 0) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		ChatScreenInvoker screen = (ChatScreenInvoker) this;
		boolean insertions = screen.chatfilter$insertionClickMode();

		for (ChatWindow window : ChatWindowManager.windows()) {
			if (!window.config().enabled || !window.isMouseOver(event.x(), event.y())) {
				continue;
			}

			ActiveTextCollector.ClickableStyleFinder finder =
					new ActiveTextCollector.ClickableStyleFinder(minecraft.font, (int) event.x(), (int) event.y())
							.includeInsertions(insertions);

			window.captureClickableText(finder, minecraft.font, minecraft.gui.hud.getGuiTicks());
			Style clicked = finder.result();

			if (clicked != null && screen.chatfilter$handleComponentClicked(clicked, insertions)) {
				callback.setReturnValue(true);
			}

			return;
		}
	}
}
