package com.github.alexthe666.rats.client.gui;

import com.github.alexthe666.rats.client.OreMinerReturnSelector;
import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.items.upgrades.OreMinerRatUpgradeItem;
import com.github.alexthe666.rats.server.message.SetOreMinerTimePacket;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class OreMinerScreen extends Screen {

	private static final int MIN_MINUTES = OreMinerRatUpgradeItem.MIN_MINING_SECONDS / 60;
	private static final int MAX_MINUTES = OreMinerRatUpgradeItem.MAX_MINING_SECONDS / 60;
	private static final int WIDGET_WIDTH = 200;

	private final int handOrdinal;
	@Nullable
	private final BlockPos returnPos;
	private int minutes;

	public OreMinerScreen(int handOrdinal, int seconds, @Nullable BlockPos returnPos) {
		super(Component.translatable(RatsItemRegistry.RAT_UPGRADE_MINER_ORE.get().getDescriptionId()));
		this.handOrdinal = handOrdinal;
		this.returnPos = returnPos;
		this.minutes = Mth.clamp(seconds / 60, MIN_MINUTES, MAX_MINUTES);
	}

	@Override
	protected void init() {
		super.init();
		int left = this.width / 2 - WIDGET_WIDTH / 2;
		int top = this.height / 2 - 30;
		double initialValue = (double) (this.minutes - MIN_MINUTES) / (MAX_MINUTES - MIN_MINUTES);
		this.addRenderableWidget(new AbstractSliderButton(left, top, WIDGET_WIDTH, 20, this.getTimeLabel(), initialValue) {
			@Override
			protected void updateMessage() {
				this.setMessage(OreMinerScreen.this.getTimeLabel());
			}

			@Override
			protected void applyValue() {
				OreMinerScreen.this.minutes = MIN_MINUTES + Mth.floor(this.value * (MAX_MINUTES - MIN_MINUTES) + 0.5D);
			}

			@Override
			public void onRelease(double mouseX, double mouseY) {
				super.onRelease(mouseX, mouseY);
				PacketDistributor.sendToServer(new SetOreMinerTimePacket(OreMinerScreen.this.handOrdinal, OreMinerScreen.this.minutes * 60));
			}
		});
		this.addRenderableWidget(Button.builder(Component.translatable(RatsLangConstants.ORE_MINER_SELECT_RETURN), button -> {
			OreMinerReturnSelector.start(this.handOrdinal);
			this.onClose();
		}).bounds(left, top + 28, WIDGET_WIDTH, 20).build());
	}

	private Component getTimeLabel() {
		return Component.translatable(RatsLangConstants.ORE_MINER_TIME, this.minutes);
	}

	private Component getReturnLabel() {
		if (this.returnPos == null) {
			return Component.translatable(RatsLangConstants.ORE_MINER_RETURN_NONE);
		}
		return Component.translatable(RatsLangConstants.ORE_MINER_RETURN_SET, this.returnPos.toShortString());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 55, 0xFFFFFF);
		graphics.drawCenteredString(this.font, this.getReturnLabel(), this.width / 2, this.height / 2 + 26, 0xA0A0A0);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
