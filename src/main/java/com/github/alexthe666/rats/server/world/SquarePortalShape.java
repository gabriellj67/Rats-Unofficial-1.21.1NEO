package com.github.alexthe666.rats.server.world;

import com.github.alexthe666.rats.registry.RatlantisBlockRegistry;
import com.github.alexthe666.rats.registry.RatsBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public final class SquarePortalShape {

	private static final int FRAME_WIDTH = 4;
	private static final int FRAME_HEIGHT = 5;

	private final BlockPos bottomLeft;
	private final List<BlockPos> frame = new ArrayList<>();
	private final List<BlockPos> interior = new ArrayList<>();

	public SquarePortalShape(BlockPos origin, Direction right) {
		this.bottomLeft = origin.relative(right, -1);
		for (int width = 0; width < FRAME_WIDTH; width++) {
			for (int height = 0; height < FRAME_HEIGHT; height++) {
				BlockPos pos = this.bottomLeft.relative(right, width).above(height);
				boolean isInterior = width > 0 && width < FRAME_WIDTH - 1 && height > 0 && height < FRAME_HEIGHT - 1;
				(isInterior ? this.interior : this.frame).add(pos);
			}
		}
	}

	public static boolean isSquarePortal(BlockGetter getter, BlockPos portalPos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (getter.getBlockState(portalPos.relative(direction)).is(RatlantisBlockRegistry.RATLANTIS_PORTAL.get())) {
				return true;
			}
		}
		return false;
	}

	public static BlockPos getLowestPortalBlock(BlockGetter getter, BlockPos portalPos) {
		BlockPos lowest = portalPos;
		while (getter.getBlockState(lowest.below()).is(RatlantisBlockRegistry.RATLANTIS_PORTAL.get())) {
			lowest = lowest.below();
		}
		return lowest;
	}

	public boolean hasSpace(Level level) {
		return this.frame.stream().allMatch(pos -> this.isFree(level, pos)) && this.interior.stream().allMatch(pos -> this.isFree(level, pos));
	}

	public void build(Level level) {
		this.frame.forEach(pos -> level.setBlockAndUpdate(pos, RatsBlockRegistry.MARBLED_CHEESE_RAW.get().defaultBlockState()));
		this.interior.forEach(pos -> level.setBlock(pos, RatlantisBlockRegistry.RATLANTIS_PORTAL.get().defaultBlockState(), Block.UPDATE_CLIENTS));
	}

	public void buildOverTerrain(Level level) {
		this.interior.forEach(pos -> level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS));
		this.build(level);
		this.frame.stream().filter(pos -> pos.getY() == this.bottomLeft.getY()).forEach(pos -> {
			BlockPos below = pos.below();
			if (level.getBlockState(below).canBeReplaced()) {
				level.setBlockAndUpdate(below, RatlantisBlockRegistry.MARBLED_CHEESE_TILE.get().defaultBlockState());
			}
		});
	}

	public BlockPos getInteriorBottom() {
		return this.interior.get(0);
	}

	private boolean isFree(Level level, BlockPos pos) {
		return level.isInWorldBounds(pos) && level.getBlockState(pos).canBeReplaced();
	}
}
