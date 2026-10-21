package com.github.alexthe666.rats.client.inventory;

import com.github.alexthe666.rats.registry.RatsBlockRegistry;
import com.github.alexthe666.rats.server.block.entity.RatCraftingTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RatCraftingTableMenuHelper {
	public static RatCraftingTableBlockEntity resolveBlockEntity(BlockPos pos) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null && level.getBlockEntity(pos) instanceof RatCraftingTableBlockEntity table) {
			return table;
		}
		return new RatCraftingTableBlockEntity(pos,
				RatsBlockRegistry.RAT_CRAFTING_TABLE.get().defaultBlockState());
	}
}
