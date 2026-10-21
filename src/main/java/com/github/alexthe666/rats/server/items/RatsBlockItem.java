package com.github.alexthe666.rats.server.items;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.render.RatsBEWLR;
import com.github.alexthe666.rats.registry.RatlantisBlockRegistry;
import com.github.alexthe666.rats.server.block.CustomItemRarity;
import com.github.alexthe666.rats.server.block.WearableOnHead;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class RatsBlockItem extends BlockItem {

	public RatsBlockItem(Block block, Properties properties) {
		super(block, applyRarity(block, properties));
	}

	private static Properties applyRarity(Block block, Properties properties) {
		if (block instanceof CustomItemRarity rarity) {
			properties = properties.rarity(rarity.getRarity());
		}
		return properties;
	}

	@Override
	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
		return this.getBlock() instanceof WearableOnHead && armorType == EquipmentSlot.HEAD;
	}

	@Override
	public EquipmentSlot getEquipmentSlot(ItemStack stack) {
		return this.getBlock() instanceof WearableOnHead ? EquipmentSlot.HEAD : super.getEquipmentSlot(stack);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (this.getBlock() == RatlantisBlockRegistry.CHUNKY_CHEESE_TOKEN.get() && !RatsMod.RATLANTIS_DATAPACK_ENABLED) {
			return InteractionResult.PASS;
		}
		return super.useOn(context);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new ClientExtensions());
	}

	@OnlyIn(Dist.CLIENT)
	private static final class ClientExtensions implements IClientItemExtensions {
		@Override
		public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new RatsBEWLR();
		}
	}
}
