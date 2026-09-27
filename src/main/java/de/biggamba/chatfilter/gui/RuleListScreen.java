package de.biggamba.chatfilter.gui;

import de.biggamba.chatfilter.config.ChatfilterConfig;
import de.biggamba.chatfilter.config.ConfigManager;
import de.biggamba.chatfilter.config.FilterRule;
import de.biggamba.chatfilter.config.MatchType;
import de.biggamba.chatfilter.config.RuleAction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Liste aller Filterregeln. Die Reihenfolge ist wichtig: Regeln werden von oben
 * nach unten geprüft.
 */
public class RuleListScreen extends Screen {
	private static final int PAGE_SIZE = 8;

	private final Screen parent;
	private int page;

	public RuleListScreen(Screen parent) {
		super(Component.literal("Filterregeln"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ChatfilterConfig config = ConfigManager.get();
		List<FilterRule> rules = config.rules;
		int pages = Math.max(1, (rules.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		this.page = Math.min(this.page, pages - 1);

		int left = this.width / 2 - 166;
		int top = 36;

		for (int i = 0; i < PAGE_SIZE; i++) {
			int index = this.page * PAGE_SIZE + i;

			if (index >= rules.size()) {
				break;
			}

			FilterRule rule = rules.get(index);
			int y = top + i * 22;
			int position = index;

			Component label = Component.literal(rule.name)
					.withStyle(rule.isValid() ? ChatFormatting.WHITE : ChatFormatting.RED);

			this.addRenderableWidget(Button.builder(label,
							button -> this.minecraft.gui.setScreen(new RuleEditScreen(this, rule)))
					.bounds(left, y, 150, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("^"), button -> {
				if (position > 0) {
					rules.add(position - 1, rules.remove(position));
					ConfigManager.save();
					this.rebuildWidgets();
				}
			}).bounds(left + 154, y, 20, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("v"), button -> {
				if (position < rules.size() - 1) {
					rules.add(position + 1, rules.remove(position));
					ConfigManager.save();
					this.rebuildWidgets();
				}
			}).bounds(left + 178, y, 20, 20).build());

			this.addRenderableWidget(Button.builder(
							Component.literal(rule.enabled ? "An" : "Aus")
									.withStyle(rule.enabled ? ChatFormatting.GREEN : ChatFormatting.RED),
							button -> {
								rule.enabled = !rule.enabled;
								ConfigManager.save();
								this.rebuildWidgets();
							})
					.bounds(left + 202, y, 50, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Kopie"), button -> {
				rules.add(position + 1, rule.copy());
				ConfigManager.save();
				this.rebuildWidgets();
			}).bounds(left + 256, y, 50, 20).build());

			this.addRenderableWidget(Button.builder(
							Component.literal("X").withStyle(ChatFormatting.RED),
							button -> {
								rules.remove(rule);
								ConfigManager.save();
								this.rebuildWidgets();
							})
					.bounds(left + 310, y, 20, 20).build());
		}

		int bottom = this.height - 52;

		this.addRenderableWidget(Button.builder(Component.literal("Neue Regel"), button -> {
			FilterRule created = new FilterRule();
			created.name = "Regel " + (rules.size() + 1);

			if (!config.windows.isEmpty()) {
				created.targetWindowId = config.windows.getFirst().id;
			}

			rules.add(created);
			ConfigManager.save();
			this.minecraft.gui.setScreen(new RuleEditScreen(this, created));
		}).bounds(this.width / 2 - 102, bottom, 100, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Zurück"), button -> this.onClose())
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
		graphics.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFFFF);

		List<FilterRule> rules = ConfigManager.get().rules;

		if (rules.isEmpty()) {
			graphics.centeredText(this.font,
					Component.literal("Noch keine Regeln – erstelle eine mit \"Neue Regel\"."),
					this.width / 2, 60, 0xFF808080);
			return;
		}

		// Kurzbeschreibung rechts neben jeder Regel.
		int left = this.width / 2 - 166;

		for (int i = 0; i < PAGE_SIZE; i++) {
			int index = this.page * PAGE_SIZE + i;

			if (index >= rules.size()) {
				break;
			}

			FilterRule rule = rules.get(index);
			String description = describe(rule);
			graphics.text(this.font, Component.literal(description), left + 336, 36 + i * 22 + 6, 0xFF808080);
		}

		int pages = Math.max(1, (rules.size() + PAGE_SIZE - 1) / PAGE_SIZE);

		if (pages > 1) {
			graphics.centeredText(this.font, Component.literal("Seite " + (this.page + 1) + "/" + pages),
					this.width / 2, this.height - 22, 0xFFA0A0A0);
		}
	}

	private String describe(FilterRule rule) {
		MatchType type = rule.matchType;
		RuleAction action = rule.action;
		String target = "";

		if (action == RuleAction.ROUTE) {
			var window = ConfigManager.get().findWindow(rule.targetWindowId);
			target = " → " + (window != null ? window.name : "kein Fenster!");
		}

		return type.getDisplayName() + " \"" + rule.pattern + "\" · " + action.getDisplayName() + target;
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		this.minecraft.gui.setScreen(this.parent);
	}
}
