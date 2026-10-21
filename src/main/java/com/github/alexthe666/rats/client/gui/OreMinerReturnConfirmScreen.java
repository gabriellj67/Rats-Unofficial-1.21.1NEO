package com.github.alexthe666.rats.client.gui;

import com.github.alexthe666.rats.client.OreMinerReturnSelector;
import com.github.alexthe666.rats.server.message.SetOreMinerReturnPacket;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class OreMinerReturnConfirmScreen extends Screen {

	private static final int BUTTON_WIDTH = 90;

	private final int handOrdinal;
	private final BlockPos pos;

	public OreMinerReturnConfirmScreen(int handOrdinal, BlockPos pos) {
		super(Component.translatable(RatsLangConstants.ORE_MINER_SELECT_RETURN));
		this.handOrdinal = handOrdinal;
		this.pos = pos;
	}

	@Override
	protected void init() {
		super.init();
		int centerX = this.width / 2;
		int y = this.height / 2;
		this.addRenderableWidget(Button.builder(Component.translatable(RatsLangConstants.ORE_MINER_CONFIRM), button -> {
			PacketDistributor.sendToServer(new SetOreMinerReturnPacket(this.handOrdinal, this.pos));
			OreMinerReturnSelector.stop();
			this.onClose();
		}).bounds(centerX - BUTTON_WIDTH - 5, y, BUTTON_WIDTH, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable(RatsLangConstants.ORE_MINER_CANCEL), button -> {
			OreMinerReturnSelector.stop();
			this.onClose();
		}).bounds(centerX + 5, y, BUTTON_WIDTH, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		graphics.drawCenteredString(this.font, Component.translatable(RatsLangConstants.ORE_MINER_RETURN_SET, this.pos.toShortString()), this.width / 2, this.height / 2 - 20, 0xFFFFFF);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
