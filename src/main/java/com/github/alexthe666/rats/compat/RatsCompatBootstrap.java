package com.github.alexthe666.rats.compat;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.compat.carryon.CarryOnCompat;
import com.github.alexthe666.rats.compat.create.CreateCompat;
import com.github.alexthe666.rats.compat.createaeronautics.AeronauticsCompat;
import com.github.alexthe666.rats.compat.gregtech.GregTechCompat;
import net.neoforged.fml.ModList;

public final class RatsCompatBootstrap {

	private RatsCompatBootstrap() {}

	public static void init() {
		if (ModList.get().isLoaded("create")) {
			tryInit("Create", CreateCompat::init);
		}
		if (ModList.get().isLoaded("create_aeronautics")) {
			tryInit("Create: Aeronautics", AeronauticsCompat::init);
		}
		if (ModList.get().isLoaded("carryon")) {
			tryInit("Carry On", CarryOnCompat::init);
		}
		if (ModList.get().isLoaded("gtceu")) {
			tryInit("GregTech", GregTechCompat::init);
		}
		if (ModList.get().isLoaded("earthworks")) {
			RatsMod.LOGGER.info("Rats compat: Earthworks Reborn detected - surface blocks scatter in Ratlantis");
		}
	}

	private static void tryInit(String name, Runnable initializer) {
		try {
			initializer.run();
			RatsMod.LOGGER.info("Rats compat: {} integration enabled", name);
		} catch (Throwable t) {
			RatsMod.LOGGER.warn("Rats compat: {} integration failed to initialise; rats will still work standalone", name, t);
		}
	}
}
