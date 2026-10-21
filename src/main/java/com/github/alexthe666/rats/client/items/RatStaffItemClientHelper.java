package com.github.alexthe666.rats.client.items;

import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class RatStaffItemClientHelper {
	public static void appendStaffTooltip(List<Component> tooltip) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && mc.player != null) {
			TamedRat rat = com.github.alexthe666.rats.server.capability.SelectedRat.get(mc.player);
			if (rat != null) {
				tooltip.add(Component.translatable(RatsLangConstants.CHEESE_STAFF_SELECTED,
						rat.getDisplayName(), rat.getUUID().toString())
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			}
		}
	}
}
