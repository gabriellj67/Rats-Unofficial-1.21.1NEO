package com.github.alexthe666.rats.compat.travelersbackpack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.lang.reflect.Method;
import java.util.Optional;

public final class TravelersBackpackCompat {

	public static final String MOD_ID = "travelersbackpack";

	public static final ResourceLocation STANDARD_BACKPACK = ResourceLocation.fromNamespaceAndPath(MOD_ID, "standard");
	private static final String WRAPPER_CLASS = "com.tiviacz.travelersbackpack.inventory.BackpackWrapper";

	private static Method fromStack;
	private static Method getStorage;
	private static boolean resolved;

	private TravelersBackpackCompat() {}

	public static boolean isLoaded() {
		return ModList.get().isLoaded(MOD_ID);
	}

	public static boolean isStandardBackpack(ItemStack stack) {
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(STANDARD_BACKPACK);
	}

	public static ItemStack createBackpack() {
		ItemStack backpack = new ItemStack(BuiltInRegistries.ITEM.get(STANDARD_BACKPACK));
		getStorage(backpack);
		return backpack;
	}

	public static Optional<ItemStackHandler> getStorage(ItemStack backpack) {
		if (!isLoaded() || !isStandardBackpack(backpack) || !resolveMethods()) {
			return Optional.empty();
		}
		try {
			Object wrapper = fromStack.invoke(null, backpack);
			return Optional.ofNullable((ItemStackHandler) getStorage.invoke(wrapper));
		} catch (ReflectiveOperationException | ClassCastException e) {
			return Optional.empty();
		}
	}

	private static boolean resolveMethods() {
		if (!resolved) {
			resolved = true;
			try {
				Class<?> wrapperClass = Class.forName(WRAPPER_CLASS);
				fromStack = wrapperClass.getMethod("fromStack", ItemStack.class);
				getStorage = wrapperClass.getMethod("getStorage");
			} catch (ReflectiveOperationException e) {
				fromStack = null;
				getStorage = null;
			}
		}
		return fromStack != null && getStorage != null;
	}
}
