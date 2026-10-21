package com.github.alexthe666.rats.server.entity.ai.goal;

import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class WildRatEatCropsGoal extends Goal {
	private final AbstractRat rat;
	private BlockPos targetCrop;
	protected int nextStartTick;

	public WildRatEatCropsGoal(AbstractRat rat) {
		this.rat = rat;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!this.rat.canMove() || this.rat.getOwner() != null || this.rat.isEating())
			return false;

		if (this.nextStartTick > 0) {
			--this.nextStartTick;
			return false;
		}
		this.nextStartTick = this.adjustedTickDelay(RatConfig.ratUpdateDelay);

		List<BlockPos> crops = this.findNearestCrops();
		if (crops.isEmpty()) return false;

		this.targetCrop = crops.get(0);
		return true;
	}

	private List<BlockPos> findNearestCrops() {
		List<BlockPos> crops = new ArrayList<>();
		int searchRadius = 16;
		BlockPos ratPos = this.rat.blockPosition();
		
		for (BlockPos pos : BlockPos.betweenClosed(
				ratPos.offset(-searchRadius, -1, -searchRadius),
				ratPos.offset(searchRadius, 1, searchRadius))) {
			BlockState state = this.rat.level().getBlockState(pos);
			Block block = state.getBlock();
			
			if (this.isEatableCrop(block, state)) {
				crops.add(pos.immutable());
			}
		}

		crops.sort(Comparator.comparingDouble(pos -> this.rat.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)));
		return crops;
	}

	private boolean isEatableCrop(Block block, BlockState state) {
		if (block instanceof CropBlock crop) {
			return crop.isMaxAge(state) || crop.getAge(state) >= crop.getMaxAge() - 1;
		}
		return block == Blocks.MELON || block == Blocks.PUMPKIN || 
		       block == Blocks.COCOA || block == Blocks.SWEET_BERRY_BUSH;
	}

	@Override
	public boolean canContinueToUse() {
		return this.rat.isAlive() && this.targetCrop != null && !this.rat.level().isEmptyBlock(this.targetCrop);
	}

	@Override
	public void start() {
		if (this.targetCrop != null) {
			this.rat.getNavigation().moveTo(this.targetCrop.getX() + 0.5, this.targetCrop.getY() + 0.5, this.targetCrop.getZ() + 0.5, 1.2);
		}
	}

	@Override
	public void tick() {
		if (this.targetCrop != null && this.rat.distanceToSqr(this.targetCrop.getCenter()) < 2.0) {
			BlockState state = this.rat.level().getBlockState(this.targetCrop);
			Block block = state.getBlock();
			
			if (this.isEatableCrop(block, state)) {
				this.rat.level().destroyBlock(this.targetCrop, true);
				if (this.rat instanceof Rat wildRat) {
					wildRat.setFleePos(this.targetCrop);
				}
			}
			this.stop();
		}
	}

	@Override
	public void stop() {
		this.targetCrop = null;
		this.rat.getNavigation().stop();
	}
}
