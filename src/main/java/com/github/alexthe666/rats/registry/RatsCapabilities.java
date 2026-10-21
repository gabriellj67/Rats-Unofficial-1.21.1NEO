package com.github.alexthe666.rats.registry;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.block.entity.AutoCurdlerBlockEntity;
import com.github.alexthe666.rats.server.block.entity.RatCageWheelBlockEntity;
import com.github.alexthe666.rats.server.block.entity.RatCraftingTableBlockEntity;
import com.github.alexthe666.rats.server.block.entity.RatQuarryBlockEntity;
import com.github.alexthe666.rats.server.block.entity.UpgradeCombinerBlockEntity;
import com.github.alexthe666.rats.server.entity.rat.InventoryRat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = RatsMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class RatsCapabilities {

    private RatsCapabilities() {}

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        // Energy on the rat-cage wheel
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                RatsBlockEntityRegistry.RAT_CAGE_WHEEL.get(),
                (be, side) -> be.energyStorage);

        // AutoCurdler — sided item handler + fluid handler
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RatsBlockEntityRegistry.AUTO_CURDLER.get(),
                (be, side) -> be.itemHandler(side));
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                RatsBlockEntityRegistry.AUTO_CURDLER.get(),
                (be, side) -> be.fluidHandler(side));

        // RatQuarry — output-only item handler
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RatsBlockEntityRegistry.RAT_QUARRY.get(),
                (be, side) -> be.itemHandler);

        // UpgradeCombiner — three-sided item handler
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RatsBlockEntityRegistry.UPGRADE_COMBINER.get(),
                (be, side) -> be.itemHandler(side));

        // RatCraftingTable — buffer (top/sides), result (bottom)
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RatsBlockEntityRegistry.RAT_CRAFTING_TABLE.get(),
                (be, side) -> be.itemHandler(side));

        // Rat carrying inventory (entity-attached item handler) — only TamedRat has the InventoryRat-backed handler.
        event.registerEntity(
                Capabilities.ItemHandler.ENTITY,
                RatsEntityRegistry.TAMED_RAT.get(),
                (rat, ctx) -> rat.itemHandler);
    }
}
