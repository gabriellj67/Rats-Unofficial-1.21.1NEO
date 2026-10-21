package com.github.alexthe666.rats.client;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.gui.OreMinerReturnConfirmScreen;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = RatsMod.MODID, value = Dist.CLIENT)
public final class OreMinerReturnSelector {

	private static final double OUTLINE_GROWTH = 0.002D;

	private static boolean active;
	private static int handOrdinal;

	private OreMinerReturnSelector() {}

	public static void start(int hand) {
		active = true;
		handOrdinal = hand;
		Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(RatsLangConstants.ORE_MINER_SELECT_HINT), false);
	}

	public static void stop() {
		active = false;
	}

	@Nullable
	private static BlockPos getTargetedPos() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
			return hit.getBlockPos();
		}
		return null;
	}

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		if (!active || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
			return;
		}
		BlockPos pos = getTargetedPos();
		if (pos == null) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
		PoseStack poseStack = event.getPoseStack();
		Vec3 camera = event.getCamera().getPosition();
		AABB box = new AABB(pos).inflate(OUTLINE_GROWTH).move(-camera.x, -camera.y, -camera.z);

		VertexConsumer fill = buffers.getBuffer(RenderType.debugFilledBox());
		LevelRenderer.addChainedFilledBoxVertices(poseStack, fill, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, 0.0F, 1.0F, 0.0F, 0.4F);
		buffers.endBatch(RenderType.debugFilledBox());

		VertexConsumer lines = buffers.getBuffer(RenderType.lines());
		LevelRenderer.renderLineBox(poseStack, lines, box, 1.0F, 1.0F, 1.0F, 1.0F);
		buffers.endBatch(RenderType.lines());
	}

	@SubscribeEvent
	public static void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
		if (!active || !(event.isUseItem() || event.isAttack())) {
			return;
		}
		BlockPos pos = getTargetedPos();
		event.setCanceled(true);
		event.setSwingHand(false);
		if (pos != null) {
			Minecraft.getInstance().setScreen(new OreMinerReturnConfirmScreen(handOrdinal, pos));
		}
	}

	@SubscribeEvent
	public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		stop();
	}
}
