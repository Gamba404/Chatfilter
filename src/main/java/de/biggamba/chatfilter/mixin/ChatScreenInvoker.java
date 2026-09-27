package de.biggamba.chatfilter.mixin;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Zugriff auf die privaten Klick-Methoden von {@link ChatScreen}, damit angeklickter
 * Text in den eigenen Fenstern exakt wie im Hauptchat behandelt wird.
 */
@Mixin(ChatScreen.class)
public interface ChatScreenInvoker {
	@Invoker("handleComponentClicked")
	boolean chatfilter$handleComponentClicked(Style style, boolean allowInsertions);

	@Invoker("insertionClickMode")
	boolean chatfilter$insertionClickMode();
}
