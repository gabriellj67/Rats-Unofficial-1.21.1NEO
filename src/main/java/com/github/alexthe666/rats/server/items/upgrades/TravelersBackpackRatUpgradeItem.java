package com.github.alexthe666.rats.server.items.upgrades;

import com.github.alexthe666.rats.client.model.entity.RatModel;
import com.github.alexthe666.rats.compat.travelersbackpack.TravelersBackpackCompat;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.HoldsItemUpgrade;
import com.github.alexthe666.rats.server.items.upgrades.interfaces.ItemStorageUpgrade;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;
import java.util.Optional;

public class TravelersBackpackRatUpgradeItem extends BaseRatUpgradeItem implements ItemStorageUpgrade, HoldsItemUpgrade {

	private static final int STORAGE_SLOTS = 27;
	private static ItemStack renderedBackpack;

	public TravelersBackpackRatUpgradeItem(Properties properties) {
		super(properties, 1, 3);
	}

	public static void openBackpack(ServerPlayer player, ItemStack upgrade) {
		ItemStack backpack = getBackpack(upgrade);
		Optional<ItemStackHandler> storage = TravelersBackpackCompat.getStorage(backpack);
		if (storage.isEmpty() || storage.get().getSlots() != STORAGE_SLOTS) {
			return;
		}
		ItemStackHandler handler = storage.get();
		SimpleContainer view = new SimpleContainer(STORAGE_SLOTS) {
			@Override
			public boolean canPlaceItem(int slot, ItemStack stack) {
				return handler.isItemValid(slot, stack);
			}
		};
		for (int slot = 0; slot < STORAGE_SLOTS; slot++) {
			view.setItem(slot, handler.getStackInSlot(slot).copy());
		}
		view.addListener(container -> {
			for (int slot = 0; slot < STORAGE_SLOTS; slot++) {
				handler.setStackInSlot(slot, container.getItem(slot).copy());
			}
			setBackpack(upgrade, backpack);
		});
		player.openMenu(new SimpleMenuProvider((id, inventory, menuPlayer) -> new ChestMenu(MenuType.GENERIC_9x3, id, inventory, view, 3), Component.translatable(RatsLangConstants.RAT_BACKPACK_TITLE)));
	}

	@Override
	public void renderHeldItem(EntityRendererProvider.Context context, TamedRat rat, RatModel<?> model, PoseStack stack, MultiBufferSource buffer, int light, float ageInTicks) {
		stack.pushPose();
		model.body1.translateRotate(stack);
		stack.translate(0.0F, -0.13F, 0.02F);
		stack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		stack.scale(0.6F, 0.6F, 0.6F);
		context.getItemRenderer().renderStatic(getRenderedBackpack(), ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, stack, buffer, null, rat.getId());
		stack.popPose();
	}

	@Override
	public boolean isFakeHandRender() {
		return true;
	}

	@Override
	public boolean hasRoom(ItemStack upgrade) {
		Optional<ItemStackHandler> storage = TravelersBackpackCompat.getStorage(getBackpack(upgrade));
		if (storage.isEmpty()) {
			return false;
		}
		ItemStackHandler handler = storage.get();
		for (int slot = 0; slot < handler.getSlots(); slot++) {
			ItemStack stack = handler.getStackInSlot(slot);
			if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack storeItem(ItemStack upgrade, ItemStack stack) {
		ItemStack backpack = getBackpack(upgrade);
		Optional<ItemStackHandler> storage = TravelersBackpackCompat.getStorage(backpack);
		if (storage.isEmpty()) {
			return stack;
		}
		ItemStack remainder = ItemHandlerHelper.insertItem(storage.get(), stack, false);
		setBackpack(upgrade, backpack);
		return remainder;
	}

	private static ItemStack getRenderedBackpack() {
		if (renderedBackpack == null) {
			renderedBackpack = TravelersBackpackCompat.createBackpack();
		}
		return renderedBackpack;
	}

	private static ItemStack getBackpack(ItemStack upgrade) {
		ItemStack backpack = upgrade.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne();
		if (backpack.isEmpty()) {
			backpack = TravelersBackpackCompat.createBackpack();
			setBackpack(upgrade, backpack);
		}
		return backpack;
	}

	private static void setBackpack(ItemStack upgrade, ItemStack backpack) {
		if (!backpack.isEmpty()) {
			upgrade.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(backpack)));
		}
	}
}
