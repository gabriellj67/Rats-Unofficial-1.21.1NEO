package com.github.alexthe666.rats.server.world;

import com.github.alexthe666.rats.registry.worldgen.RatlantisConfiguredFeatureRegistry;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public final class PiratTreeGrower {
	public static final TreeGrower INSTANCE = new TreeGrower(
			"rats:pirat",
			0.5F,
			Optional.of(RatlantisConfiguredFeatureRegistry.LARGE_GHOST_PIRAT_TREE),
			Optional.empty(),
			Optional.of(RatlantisConfiguredFeatureRegistry.GHOST_PIRAT_TREE),
			Optional.empty(),
			Optional.empty(),
			Optional.empty()
	);

	private PiratTreeGrower() {
	}
}
