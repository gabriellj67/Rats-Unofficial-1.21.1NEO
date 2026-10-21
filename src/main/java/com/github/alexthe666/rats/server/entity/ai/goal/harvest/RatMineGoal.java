package com.github.alexthe666.rats.server.entity.ai.goal.harvest;

import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.OreMinerRatUpgradeItem;
import com.github.alexthe666.rats.server.misc.RatPathingHelper;
import com.github.alexthe666.rats.server.misc.RatUpgradeUtils;
import com.github.alexthe666.rats.server.misc.RatDropMarker;
import com.github.alexthe666.rats.server.misc.RatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class RatMineGoal extends BaseRatHarvestGoal {
	private static final int RETURN_TIMEOUT_TICKS = 600;
	private static final double RETURN_DISTANCE_SQR = 4.0D;
	private static final int COLLECT_TIMEOUT_TICKS = 600;

	private enum Phase {
		MINING,
		COLLECTING,
		RETURNING,
		RESTING
	}

	private final TamedRat rat;
	private int breakingTime;
	private int previousBreakProgress;
	private BlockState prevMiningState;
	private Phase phase = Phase.MINING;
	private int phaseTicks;
	private long collectDeadline;

	public RatMineGoal(TamedRat rat) {
		super(rat);
		this.rat = rat;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!super.canUse() || !this.checkTheBasics(false, false)) {
			return false;
		}
		if (this.phase != Phase.MINING) {
			return this.phase != Phase.COLLECTING || !this.hasPendingItems();
		}
		this.resetTarget();
		return this.getTargetBlock() != null;
	}

	@Override
	public boolean canContinueToUse() {
		if (!this.checkTheBasics(false, false)) {
			return false;
		}
		return switch (this.phase) {
			case MINING -> this.getTargetBlock() != null;
			case COLLECTING -> !this.hasPendingItems();
			default -> true;
		};
	}

	@Override
	public void tick() {
		if (this.updatePhase() || this.getTargetBlock() == null) {
			return;
		}
		BlockPos rayPos = RatPathingHelper.clipWithConditions(this.rat.level(), new ClipContext(this.rat.position(), Vec3.atCenterOf(this.getTargetBlock()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.rat), false).getBlockPos();
		this.rat.getNavigation().moveTo(rayPos.getX() + 0.5D, rayPos.getY(), rayPos.getZ() + 0.5D, 1.25D);
		if (this.isInPosition()) {
			this.mine(rayPos);
		}
	}

	@Override
	public void stop() {
		super.stop();
		this.rat.crafting = false;
	}

	private boolean isInPosition() {
		boolean arrived = !this.rat.getMoveControl().hasWanted() || this.rat.hasFlightUpgrade();
		boolean grounded = this.rat.onGround() || this.rat.isFlying() || this.rat.isRidingSpecialMount();
		return arrived && grounded;
	}

	private void mine(BlockPos rayPos) {
		BlockState block = this.rat.level().getBlockState(rayPos);
		if (block.isAir() || block.getCollisionShape(this.rat.level(), rayPos).isEmpty()) {
			return;
		}
		double distance = this.rat.getRatDistanceCenterSq(rayPos.getX() + 0.5D, rayPos.getY() + 0.5D, rayPos.getZ() + 0.5D);
		if (distance >= this.rat.getRatHarvestDistance(2.0D)) {
			return;
		}
		SoundType soundType = block.getSoundType();
		this.rat.crafting = block == this.prevMiningState;
		this.rat.level().broadcastEntityEvent(this.rat, (byte) (this.rat.crafting ? 85 : 86));
		this.prevMiningState = block;
		if (distance < this.rat.getRatHarvestDistance(-1.0D)) {
			this.rat.setDeltaMovement(Vec3.ZERO);
			this.rat.getNavigation().stop();
		}

		this.breakingTime++;
		int hardness = (int) (block.getDestroySpeed(this.rat.level(), rayPos) * (RatUpgradeUtils.hasUpgrade(this.rat, RatsItemRegistry.RAT_UPGRADE_MINER.get()) ? 10 : 20));
		int progress = hardness <= 0 ? 10 : (int) ((float) this.breakingTime / hardness * 10.0F);
		if (this.breakingTime % 5 == 0) {
			this.rat.playSound(soundType.getHitSound(), soundType.getVolume() + 1, soundType.getPitch());
		}
		if (progress != this.previousBreakProgress) {
			this.rat.level().destroyBlockProgress(this.rat.getId(), rayPos, progress);
			this.previousBreakProgress = progress;
		}
		if (this.breakingTime >= hardness) {
			this.breakBlock(rayPos, soundType);
		}
	}

	private void breakBlock(BlockPos rayPos, SoundType soundType) {
		this.rat.level().broadcastEntityEvent(this.rat, (byte) 86);
		this.rat.playSound(soundType.getBreakSound(), soundType.getVolume(), soundType.getPitch());
		this.breakingTime = 0;
		this.previousBreakProgress = -1;
		if (!RatUtils.isBlockProtected(this.rat.level(), rayPos, this.rat)) {
			this.rat.level().destroyBlock(rayPos, true);
			RatDropMarker.markDropsAround(this.rat.level(), rayPos);
		}
		if (this.rat.level().isEmptyBlock(this.getTargetBlock())) {
			this.stop();
		}
	}

	private boolean updatePhase() {
		Vec3 returnPos = this.getReturnPos();
		int limit = this.getMiningLimitTicks();
		if (returnPos == null || limit <= 0) {
			this.phase = Phase.MINING;
			this.phaseTicks = 0;
			return false;
		}
		this.phaseTicks++;
		switch (this.phase) {
			case MINING -> {
				if (this.phaseTicks >= limit) {
					this.startCollecting();
				}
			}
			case COLLECTING -> {
				if (!this.hasPendingItems()) {
					this.changePhase(Phase.RETURNING);
				}
			}
			case RETURNING -> {
				this.rat.getNavigation().moveTo(returnPos.x(), returnPos.y(), returnPos.z(), 1.0D);
				if (this.rat.distanceToSqr(returnPos) < RETURN_DISTANCE_SQR || this.phaseTicks > RETURN_TIMEOUT_TICKS) {
					this.rat.getNavigation().stop();
					this.changePhase(Phase.RESTING);
				}
			}
			case RESTING -> {
				if (this.phaseTicks >= limit) {
					this.changePhase(Phase.MINING);
				}
			}
		}
		return this.phase != Phase.MINING;
	}

	private void startCollecting() {
		if (this.getTargetBlock() != null) {
			this.rat.level().destroyBlockProgress(this.rat.getId(), this.getTargetBlock(), -1);
		}
		this.breakingTime = 0;
		this.previousBreakProgress = -1;
		this.rat.crafting = false;
		this.setTargetBlock(null);
		this.collectDeadline = this.rat.level().getGameTime() + COLLECT_TIMEOUT_TICKS;
		this.changePhase(Phase.COLLECTING);
	}

	private boolean hasPendingItems() {
		if (this.rat.getDepositPos().isEmpty() || this.rat.level().getGameTime() >= this.collectDeadline) {
			return false;
		}
		if (!this.rat.getMainHandItem().isEmpty() && this.rat.shouldDepositItem(this.rat.getMainHandItem())) {
			return true;
		}
		return !this.rat.level().getEntitiesOfClass(ItemEntity.class, this.getTargetableArea(), item ->
				(item.onGround() || item.isInWater()) && this.rat.shouldCollectItem(item.getItem()) && this.rat.canRatPickupItem(item.getItem())).isEmpty();
	}

	private void changePhase(Phase next) {
		this.phase = next;
		this.phaseTicks = 0;
	}

	private ItemStack getOreMinerUpgrade() {
		return RatUpgradeUtils.getUpgrade(this.rat, RatsItemRegistry.RAT_UPGRADE_MINER_ORE.get());
	}

	private int getMiningLimitTicks() {
		ItemStack upgrade = this.getOreMinerUpgrade();
		return upgrade.isEmpty() ? 0 : OreMinerRatUpgradeItem.getMiningSeconds(upgrade) * 20;
	}

	@Nullable
	private Vec3 getReturnPos() {
		ItemStack upgrade = this.getOreMinerUpgrade();
		Optional<GlobalPos> selected = upgrade.isEmpty() ? Optional.empty() : OreMinerRatUpgradeItem.getReturnPos(upgrade);
		if (selected.isPresent() && this.isInRatDimension(selected.get())) {
			return Vec3.atBottomCenterOf(selected.get().pos().above());
		}
		Optional<GlobalPos> fallback = this.rat.getHomePoint().or(() -> this.rat.getDepositPos());
		if (fallback.isPresent() && this.isInRatDimension(fallback.get())) {
			return Vec3.atBottomCenterOf(fallback.get().pos());
		}
		return null;
	}

	private boolean isInRatDimension(GlobalPos pos) {
		return pos.dimension().equals(this.rat.level().dimension());
	}

	private void resetTarget() {
		List<BlockPos> allBlocks = new ArrayList<>();
		NonNullList<ItemStack> mining = this.getMiningList();
		int radius = this.rat.getRadius();
		BlockPos center = this.rat.getSearchCenter();
		for (BlockPos pos : BlockPos.betweenClosedStream(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius)).map(BlockPos::immutable).toList()) {
			if (this.shouldMine(this.rat.level(), mining, pos)) {
				allBlocks.add(pos);
			}
		}
		if (!allBlocks.isEmpty()) {
			allBlocks.sort(this.getTargetSorter());
			this.setTargetBlock(allBlocks.get(0));
		}
	}

	@Nullable
	private NonNullList<ItemStack> getMiningList() {
		ItemStack upgrade = RatUpgradeUtils.getUpgrade(this.rat, RatsItemRegistry.RAT_UPGRADE_MINER.get());
		if (upgrade.isEmpty()) {
			return null;
		}
		CompoundTag tag = upgrade.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		NonNullList<ItemStack> list = NonNullList.withSize(27, ItemStack.EMPTY);
		if (tag.contains("Items", 9)) {
			ContainerHelper.loadAllItems(tag, list, this.rat.level().registryAccess());
		}
		return list;
	}

	private boolean shouldMine(Level level, @Nullable NonNullList<ItemStack> list, BlockPos pos) {
		if (level.isEmptyBlock(pos) || RatUtils.isBlockProtected(level, pos, this.rat)) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		if (list != null) {
			ItemStack stack = state.getBlock().getCloneItemStack(level, pos, state);
			for (ItemStack current : list) {
				if (!current.isEmpty() && ItemStack.isSameItem(current, stack)) {
					return true;
				}
			}
		}
		return RatUpgradeUtils.hasUpgrade(this.rat, RatsItemRegistry.RAT_UPGRADE_MINER_ORE.get()) && state.is(Tags.Blocks.ORES);
	}
}
