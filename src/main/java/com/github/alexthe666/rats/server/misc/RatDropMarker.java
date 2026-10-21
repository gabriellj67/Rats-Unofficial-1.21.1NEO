package com.github.alexthe666.rats.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class RatDropMarker {

	private static final String WORK_DROP_KEY = "RatsWorkDrop";
	private static final int FRESH_AGE_TICKS = 20;
	private static final double SEARCH_GROWTH = 1.5D;

	private RatDropMarker() {}

	public static void mark(ItemEntity item) {
		item.getPersistentData().putBoolean(WORK_DROP_KEY, true);
	}

	public static boolean isMarked(ItemEntity item) {
		return item.getPersistentData().getBoolean(WORK_DROP_KEY);
	}

	public static void markDropsAround(Level level, BlockPos pos) {
		AABB area = new AABB(pos).inflate(SEARCH_GROWTH);
		level.getEntitiesOfClass(ItemEntity.class, area, item -> item.getAge() < FRESH_AGE_TICKS).forEach(RatDropMarker::mark);
	}
}
