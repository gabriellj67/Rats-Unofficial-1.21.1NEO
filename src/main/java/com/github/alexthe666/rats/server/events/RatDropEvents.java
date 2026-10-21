package com.github.alexthe666.rats.server.events;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.entity.mount.RatMountBase;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.misc.RatDropMarker;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = RatsMod.MODID)
public final class RatDropEvents {

	private RatDropEvents() {}

	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event) {
		if (isRatAttacker(event.getSource().getEntity()) || isRatAttacker(event.getSource().getDirectEntity())) {
			event.getDrops().forEach(RatDropMarker::mark);
		}
	}

	private static boolean isRatAttacker(Entity entity) {
		return entity instanceof TamedRat || entity instanceof RatMountBase mount && mount.getRat() != null;
	}
}
