package com.github.alexthe666.rats.server.items.upgrades;

import com.github.alexthe666.rats.client.model.entity.RatModel;
import com.github.alexthe666.rats.server.entity.ai.goal.harvest.RatMineGoal;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ChangesAIUpgrade;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.DamageImmunityUpgrade;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.HoldsItemUpgrade;
import com.github.alexthe666.rats.server.message.OpenOreMinerScreenPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public class OreMinerRatUpgradeItem extends BaseRatUpgradeItem implements ChangesAIUpgrade, HoldsItemUpgrade, DamageImmunityUpgrade {
	public static final int MIN_MINING_SECONDS = 60;
	public static final int MAX_MINING_SECONDS = 900;
	private static final int DEFAULT_MINING_SECONDS = 300;
	private static final String MINING_SECONDS_KEY = "MiningSeconds";
	private static final String RETURN_X_KEY = "ReturnX";
	private static final String RETURN_Y_KEY = "ReturnY";
	private static final String RETURN_Z_KEY = "ReturnZ";
	private static final String RETURN_DIMENSION_KEY = "ReturnDimension";

	public OreMinerRatUpgradeItem(Properties properties) {
		super(properties, 1, 2);
	}

	public OreMinerRatUpgradeItem(Properties properties, int textLength) {
		super(properties, 1, textLength);
	}

	public boolean hasMiningTimer() {
		return true;
	}

	public static int getMiningSeconds(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		return tag.contains(MINING_SECONDS_KEY) ? tag.getInt(MINING_SECONDS_KEY) : DEFAULT_MINING_SECONDS;
	}

	public static void setMiningSeconds(ItemStack stack, int seconds) {
		int clamped = Mth.clamp(seconds, MIN_MINING_SECONDS, MAX_MINING_SECONDS);
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(MINING_SECONDS_KEY, clamped));
	}

	public static Optional<GlobalPos> getReturnPos(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!tag.contains(RETURN_DIMENSION_KEY)) {
			return Optional.empty();
		}
		ResourceLocation dimension = ResourceLocation.tryParse(tag.getString(RETURN_DIMENSION_KEY));
		if (dimension == null) {
			return Optional.empty();
		}
		BlockPos pos = new BlockPos(tag.getInt(RETURN_X_KEY), tag.getInt(RETURN_Y_KEY), tag.getInt(RETURN_Z_KEY));
		return Optional.of(GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimension), pos));
	}

	public static void setReturnPos(ItemStack stack, GlobalPos pos) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
			tag.putInt(RETURN_X_KEY, pos.pos().getX());
			tag.putInt(RETURN_Y_KEY, pos.pos().getY());
			tag.putInt(RETURN_Z_KEY, pos.pos().getZ());
			tag.putString(RETURN_DIMENSION_KEY, pos.dimension().location().toString());
		});
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!this.hasMiningTimer() || player.isShiftKeyDown()) {
			return super.use(level, player, hand);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			Optional<GlobalPos> returnPos = getReturnPos(stack);
			PacketDistributor.sendToPlayer(serverPlayer, new OpenOreMinerScreenPacket(hand.ordinal(), getMiningSeconds(stack), returnPos.isPresent(), returnPos.map(GlobalPos::pos).orElse(BlockPos.ZERO)));
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public boolean playIdleAnimation(TamedRat rat) {
		return false;
	}

	@Override
	public void renderHeldItem(EntityRendererProvider.Context context, TamedRat rat, RatModel<?> model, PoseStack stack, MultiBufferSource buffer, int light, float ageInTicks) {
		stack.pushPose();
		this.translateToHand(model, false, stack);
		stack.mulPose(Axis.ZP.rotationDegrees(180F));
		stack.translate(0.0F, -0.075F, -0.1F);
		stack.scale(0.65F, 0.65F, 0.65F);
		context.getItemRenderer().renderStatic(new ItemStack(Items.IRON_PICKAXE), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, light, OverlayTexture.NO_OVERLAY, stack, buffer, null, rat.getId());
		stack.popPose();
	}

	@Override
	public List<Goal> addNewWorkGoals(TamedRat rat) {
		return List.of(new RatMineGoal(rat));
	}

	@Override
	public boolean isImmuneToDamageSource(TamedRat rat, DamageSource source) {
		return source.is(DamageTypes.IN_WALL) || source.is(DamageTypeTags.IS_FALL);
	}
}
