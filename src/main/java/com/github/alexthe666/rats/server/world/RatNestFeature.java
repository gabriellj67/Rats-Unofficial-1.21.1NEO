package com.github.alexthe666.rats.server.world;

import com.github.alexthe666.rats.registry.RatsBlockRegistry;
import com.github.alexthe666.rats.registry.RatsEntityRegistry;
import com.github.alexthe666.rats.server.entity.rat.Rat;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RatNestFeature extends Feature<NoneFeatureConfiguration> {

	private static final int SMALL_RADIUS = 1;
	private static final int MEDIUM_RADIUS = 2;
	private static final int LARGE_RADIUS = 3;
	private static final float SMALL_CHANCE = 0.5F;
	private static final float MEDIUM_CHANCE = 0.85F;
	private static final int ORIGIN_SEARCH_DOWN = 4;
	private static final int ORIGIN_SEARCH_UP = 2;
	private static final int FLOOR_SEARCH_UP = 1;
	private static final int FLOOR_SEARCH_DOWN = 2;
	private static final float GARBAGE_CHANCE = 0.35F;
	private static final float CHEESE_CHANCE = 0.2F;
	private static final float PLAGUE_CHANCE = 0.3F;
	private static final int HOME_RADIUS = 8;

	public RatNestFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BlockPos origin = this.locateFloor(level, context.origin());
		if (origin == null) {
			return false;
		}
		BlockPos center = origin.above();
		int radius = this.randomRadius(random);

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radius * radius + 1) {
					continue;
				}
				BlockPos floor = this.findFloor(level, origin.offset(dx, 0, dz));
				if (floor != null) {
					this.buildColumn(level, floor, random);
				}
			}
		}
		this.spawnRats(level, center, radius, random);
		return true;
	}

	private BlockPos locateFloor(WorldGenLevel level, BlockPos origin) {
		for (int offset = 0; offset >= -ORIGIN_SEARCH_DOWN; offset--) {
			if (this.hasHeadroom(level, origin.above(offset))) {
				return origin.above(offset);
			}
		}
		for (int offset = 1; offset <= ORIGIN_SEARCH_UP; offset++) {
			if (this.hasHeadroom(level, origin.above(offset))) {
				return origin.above(offset);
			}
		}
		return null;
	}

	private boolean hasHeadroom(WorldGenLevel level, BlockPos floor) {
		return this.isSolidFloor(level, floor) && level.isEmptyBlock(floor.above()) && level.isEmptyBlock(floor.above(2));
	}

	private int randomRadius(RandomSource random) {
		float roll = random.nextFloat();
		if (roll < SMALL_CHANCE) {
			return SMALL_RADIUS;
		}
		return roll < MEDIUM_CHANCE ? MEDIUM_RADIUS : LARGE_RADIUS;
	}

	private void buildColumn(WorldGenLevel level, BlockPos floor, RandomSource random) {
		level.setBlock(floor, this.randomFloorBlock(random), Block.UPDATE_CLIENTS);
		BlockPos above = floor.above();
		if (!level.isEmptyBlock(above)) {
			return;
		}
		if (random.nextFloat() < CHEESE_CHANCE) {
			level.setBlock(above, RatsBlockRegistry.BLOCK_OF_CHEESE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
		} else if (random.nextFloat() < GARBAGE_CHANCE) {
			level.setBlock(above, RatsBlockRegistry.GARBAGE_PILE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	private BlockState randomFloorBlock(RandomSource random) {
		return switch (random.nextInt(3)) {
			case 0 -> Blocks.COARSE_DIRT.defaultBlockState();
			case 1 -> Blocks.PODZOL.defaultBlockState();
			default -> Blocks.MOSS_BLOCK.defaultBlockState();
		};
	}

	private BlockPos findFloor(WorldGenLevel level, BlockPos base) {
		for (int dy = FLOOR_SEARCH_UP; dy >= -FLOOR_SEARCH_DOWN; dy--) {
			BlockPos candidate = base.above(dy);
			if (this.isSolidFloor(level, candidate) && level.isEmptyBlock(candidate.above())) {
				return candidate;
			}
		}
		return null;
	}

	private boolean isSolidFloor(WorldGenLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isFaceSturdy(level, pos, Direction.UP) && !state.hasBlockEntity() && state.getDestroySpeed(level, pos) >= 0.0F;
	}

	private void spawnRats(WorldGenLevel level, BlockPos center, int radius, RandomSource random) {
		int count = radius + random.nextInt(radius + 1);
		for (int i = 0; i < count; i++) {
			Rat rat = RatsEntityRegistry.RAT.get().create(level.getLevel());
			if (rat == null) {
				continue;
			}
			rat.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
			rat.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.STRUCTURE, null);
			rat.setPlagued(random.nextFloat() < PLAGUE_CHANCE);
			rat.setPersistenceRequired();
			rat.restrictTo(center, HOME_RADIUS);
			level.addFreshEntityWithPassengers(rat);
		}
	}
}
