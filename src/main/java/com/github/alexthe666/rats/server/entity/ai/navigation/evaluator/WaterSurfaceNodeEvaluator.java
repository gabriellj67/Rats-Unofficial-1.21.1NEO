package com.github.alexthe666.rats.server.entity.ai.navigation.evaluator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;

public class WaterSurfaceNodeEvaluator extends SwimNodeEvaluator {

	private static final float SURFACE_BONUS_MALUS = 8.0F;

	public WaterSurfaceNodeEvaluator() {
		super(true);
	}

	@Override
	public PathType getPathType(PathfindingContext context, int x, int y, int z) {
		PathType base = super.getPathType(context, x, y, z);
		if (base == PathType.WATER) {
			BlockPos here = new BlockPos(x, y, z);
			BlockPos above = here.above();
			boolean atSurface = !context.level().getFluidState(here).isEmpty()
					&& context.level().getFluidState(above).isEmpty();
			if (!atSurface) {
				return PathType.WATER;
			}
		}
		return base;
	}

	@Override
	public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
		PathType type = super.getPathTypeOfMob(context, x, y, z, mob);

		if (type == PathType.WATER) {
			BlockPos here = new BlockPos(x, y, z);
			boolean isSurface = !context.level().getFluidState(here).isEmpty()
				&& context.level().getFluidState(here.above()).isEmpty();

			if (!isSurface) {
				mob.setPathfindingMalus(PathType.WATER, SURFACE_BONUS_MALUS);
			}
		}

		return type;
	}

	@SuppressWarnings("unused")
	private float surfaceBonus() {
		return SURFACE_BONUS_MALUS;
	}
}
