package com.github.alexthe666.rats.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.BiConsumer;

/**
 * Chest loot table sub-provider.
 *
 * The actual JSON content is committed under src/generated/resources and is the
 * authoritative source. This class is intentionally empty so that running
 * ./gradlew runData does NOT overwrite the manually-tuned JSONs.
 *
 * If you need to regenerate the chest loot tables from code, implement generate()
 * here and delete the corresponding JSON files first.
 */
public class RatsChestLootTables implements LootTableSubProvider {

    public RatsChestLootTables(HolderLookup.Provider registries) {
        // no-op
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        // intentionally empty — chest loot tables are maintained as JSON files
        // in src/generated/resources/data/rats/loot_table/chest/
    }
}
