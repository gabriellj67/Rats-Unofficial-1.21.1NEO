package com.github.alexthe666.rats.client.gui;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.inventory.AutoCurdlerMenu;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid; // ✅ FIXED IMPORT
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.List;

public class AutoCurdlerScreen extends AbstractContainerScreen<AutoCurdlerMenu> {

	private static final ResourceLocation TEXTURE =
		ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "textures/gui/container/auto_curdler.png");

	private final AutoCurdlerMenu curdler;

	public AutoCurdlerScreen(AutoCurdlerMenu container, Inventory inv, Component name) {
		super(container, inv, name);
		this.curdler = container;
	}

	// =========================
	// FLUID RENDER (SAFE)
	// =========================
	public static void renderFluidStack(PoseStack stack,
										int xPosition,
										int yPosition,
										int desiredWidth,
										int desiredHeight,
										Fluid fluid) {

		// 🔥 HARD SAFETY
		if (fluid == null || desiredWidth <= 0 || desiredHeight <= 0) return;

		TextureAtlasSprite sprite = Minecraft.getInstance()
			.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
			.apply(IClientFluidTypeExtensions.of(fluid).getStillTexture());

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

		int xTileCount = desiredWidth / 16;
		int xRemainder = desiredWidth - (xTileCount * 16);

		int yTileCount = desiredHeight / 16;
		int yRemainder = desiredHeight - (yTileCount * 16);

		float uMin = sprite.getU0();
		float uMax = sprite.getU1();
		float vMin = sprite.getV0();
		float vMax = sprite.getV1();

		float uDif = uMax - uMin;
		float vDif = vMax - vMin;

		RenderSystem.enableBlend();

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		Matrix4f matrix = stack.last().pose();

		boolean hasVertices = false;

		for (int xTile = 0; xTile <= xTileCount; xTile++) {

			int width = (xTile == xTileCount) ? xRemainder : 16;
			if (width <= 0) continue;

			int x = xPosition + (xTile * 16);
			int maskRight = 16 - width;
			int shiftedX = x + 16 - maskRight;

			float uLocalDif = uDif * maskRight / 16f;

			for (int yTile = 0; yTile <= yTileCount; yTile++) {

				int height = (yTile == yTileCount) ? yRemainder : 16;
				if (height <= 0) continue;

				int y = yPosition - ((yTile + 1) * 16);
				int maskTop = 16 - height;

				float vLocalDif = vDif * maskTop / 16f;

				buffer.addVertex(matrix, x, y + 16, 0).setUv(uMin + uLocalDif, vMax);
				buffer.addVertex(matrix, shiftedX, y + 16, 0).setUv(uMax, vMax);
				buffer.addVertex(matrix, shiftedX, y + maskTop, 0).setUv(uMax, vMin + vLocalDif);
				buffer.addVertex(matrix, x, y + maskTop, 0).setUv(uMin + uLocalDif, vMin + vLocalDif);

				hasVertices = true;
			}
		}

		// 🔥 CRITICAL FIX: never draw empty buffer
		if (hasVertices) {
			BufferUploader.drawWithShader(buffer.buildOrThrow());
		}

		RenderSystem.disableBlend();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {

		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;

		graphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);

		int progress = this.curdler.getCookProgressionScaled();
		graphics.blit(TEXTURE, i + 63, j + 35, 176, 0, progress + 1, 16);

		int tankWidth = 24;
		int tankHeight = 63;

		int capacity = this.curdler.getTankCapacity();
		int amount = 0;

		if (capacity > 0) {
			amount = Math.round((this.curdler.getFluidAmount() / (float) capacity) * (tankHeight - 4));
		}

		if (amount > 0) {
			renderFluidStack(
				graphics.pose(),
				i + 29,
				j + 73,
				tankWidth,
				amount,
				NeoForgeMod.MILK.get()
			);
		}

		graphics.blit(TEXTURE, i + 29, j + 12, 0, 166, tankWidth, tankHeight);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {

		int screenW = (this.width - this.imageWidth) / 2;
		int screenH = (this.height - this.imageHeight) / 2;

		if (this.isHovering(29, 15, 24, 58, mouseX, mouseY)) {

			String fluidName = new FluidStack(
				NeoForgeMod.MILK.get(),
				Math.max(this.curdler.getTankCapacity(), 1)
			).getHoverName().getString(); // ✅ FIXED (modern replacement)

			String fluidSize =
				this.curdler.getFluidAmount() + " " +
					Component.translatable(RatsLangConstants.CURDLER_MB).getString();

			List<Component> list = Arrays.asList(
				Component.literal(fluidName).withStyle(ChatFormatting.BLUE),
				Component.literal(fluidSize).withStyle(ChatFormatting.GRAY)
			);

			graphics.renderTooltip(
				this.font,
				Lists.transform(list, Component::getVisualOrderText),
				mouseX - screenW,
				mouseY - screenH
			);
		}
	}
}
