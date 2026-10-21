package com.github.alexthe666.rats.server.items.upgrades.interfaces;

import net.minecraft.world.item.ItemStack;

public interface ItemStorageUpgrade {

	boolean hasRoom(ItemStack upgrade);

	ItemStack storeItem(ItemStack upgrade, ItemStack stack);
}
