package de.biggamba.chatfilter;

import com.mojang.brigadier.arguments.StringArgumentType;
import de.biggamba.chatfilter.config.ChatfilterConfig;
import de.biggamba.chatfilter.config.ConfigManager;
import de.biggamba.chatfilter.filter.FilterEngine;
import de.biggamba.chatfilter.filter.FilterResult;
import de.biggamba.chatfilter.filter.MessageInterceptor;
import de.biggamba.chatfilter.gui.ChatfilterScreen;
import de.biggamba.chatfilter.window.ChatWindowHudElement;
import de.biggamba.chatfilter.window.ChatWindowManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatfilterMod implements ClientModInitializer {
	public static final String MOD_ID = "chatfilter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Component PREFIX = Component.literal("[Chatfilter] ").withStyle(ChatFormatting.AQUA);

	public static KeyMapping openConfigKey;

	@Override
	public void onInitializeClient() {
		ConfigManager.load();
		ChatWindowManager.sync();

		MessageInterceptor.register();

		HudElementRegistry.attachElementAfter(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(MOD_ID, "chat_windows"),
				new ChatWindowHudElement());

		openConfigKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.chatfilter.open", InputConstants.KEY_K, KeyMapping.Category.MISC));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKey.consumeClick()) {
				client.gui.setScreen(new ChatfilterScreen(null));
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommands.literal("chatfilter")
						.executes(context -> {
							Minecraft.getInstance().execute(
									() -> Minecraft.getInstance().gui.setScreen(new ChatfilterScreen(null)));
							return 1;
						})
						.then(ClientCommands.literal("toggle").executes(context -> {
							ChatfilterConfig config = ConfigManager.get();
							config.enabled = !config.enabled;
							ConfigManager.save();
							feedback(Component.literal(config.enabled ? "Chatfilter aktiviert." : "Chatfilter deaktiviert."));
							return 1;
						}))
						.then(ClientCommands.literal("reload").executes(context -> {
							ConfigManager.load();
							ChatWindowManager.sync();
							feedback(Component.literal("Konfiguration neu geladen ("
									+ ConfigManager.get().rules.size() + " Regeln, "
									+ ConfigManager.get().windows.size() + " Fenster)."));
							return 1;
						}))
						.then(ClientCommands.literal("defaults").executes(context -> {
							ConfigManager.resetToDefaults();
							ChatWindowManager.sync();
							feedback(Component.literal("Mitgelieferte Standardkonfiguration wiederhergestellt "
									+ "(alte Datei liegt als chatfilter.json.bak daneben)."));
							return 1;
						}))
						.then(ClientCommands.literal("clear").executes(context -> {
							ChatWindowManager.clearAll();
							feedback(Component.literal("Alle eigenen Chat-Fenster geleert."));
							return 1;
						}))
						.then(ClientCommands.literal("test")
								.then(ClientCommands.argument("text", StringArgumentType.greedyString())
										.executes(context -> {
											String text = StringArgumentType.getString(context, "text");
											FilterResult result = FilterEngine.process(ConfigManager.get(), text);

											if (!result.matched()) {
												feedback(Component.literal("Keine Regel trifft zu.").withStyle(ChatFormatting.GRAY));
											} else {
												feedback(Component.literal("Regel \"" + result.firstMatch.name + "\" trifft zu → "
														+ result.firstMatch.action.getDisplayName()
														+ (result.hideFromMainChat ? " (aus dem Hauptchat entfernt)" : "")));
											}

											return 1;
										})))));
	}

	public static void feedback(Component message) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.player != null) {
			minecraft.gui.hud.getChat().addClientSystemMessage(Component.empty().append(PREFIX).append(message));
		}
	}
}
