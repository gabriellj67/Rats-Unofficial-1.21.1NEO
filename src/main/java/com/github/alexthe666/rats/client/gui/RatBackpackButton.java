package com.github.alexthe666.rats.client.gui;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RatBackpackButton extends Button {

	private static final int SIZE = 12;
	private static final int ICON_SIZE = 10;
	private static final int ICON_TEXTURE_SIZE = 16;
	private static final ResourceLocation SPRITE = ResourceLocation.withDefaultNamespace("widget/button");
	private static final ResourceLocation SPRITE_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("widget/button_highlighted");
	private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "textures/item/rat_upgrade_travelers_backpack.png");

	public RatBackpackButton(int x, int y, OnPress onPress) {
		super(x, y, SIZE, SIZE, Component.translatable(RatsLangConstants.RAT_BACKPACK_BUTTON), onPress, DEFAULT_NARRATION);
		this.setTooltip(Tooltip.create(Component.translatable(RatsLangConstants.RAT_BACKPACK_TITLE)));
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.blitSprite(this.isHoveredOrFocused() ? SPRITE_HIGHLIGHTED : SPRITE, this.getX(), this.getY(), this.width, this.height);
		int offset = (SIZE - ICON_SIZE) / 2;
		graphics.blit(ICON, this.getX() + offset, this.getY() + offset, ICON_SIZE, ICON_SIZE, 0.0F, 0.0F, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
	}
}
