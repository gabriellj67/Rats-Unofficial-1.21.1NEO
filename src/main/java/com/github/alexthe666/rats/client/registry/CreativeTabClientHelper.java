package com.github.alexthe666.rats.client.registry;

import com.github.alexthe666.rats.registry.RatsItemRegistry;
import com.github.alexthe666.rats.server.items.OreRatNuggetItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CreativeTabClientHelper {
	@Nullable
	public static Level getClientLevel() {
		return Minecraft.getInstance().level;
	}

	public static List<ItemStack> getOreNuggets(Level level) {
		List<ItemStack> uniqueOres = new ArrayList<>();
		if (level != null) {
			for (Item item : BuiltInRegistries.ITEM.holders().filter(h -> h.is(Tags.Items.ORES)).map(net.minecraft.core.Holder::value).toList()) {
				ItemStack oreDrop = OreRatNuggetItem.getIngot(level, new ItemStack(item));
				if (!uniqueOres.contains(oreDrop) && !oreDrop.isEmpty()) {
					uniqueOres.add(oreDrop);
				}
			}
			uniqueOres.sort(Comparator.comparing(o -> o.getDisplayName().getString()));
		}
		return uniqueOres;
	}
}
