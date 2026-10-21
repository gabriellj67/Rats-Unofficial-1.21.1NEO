package com.github.alexthe666.rats.compat.earthworks;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.registry.worldgen.RatlantisDimensionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@EventBusSubscriber(modid = RatsMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class EarthworksCompat {

    private static final Object[][] VEINS = {
            {"rammed_earth",     8,  80,  8, 6},
            {"adobe",            8,  70,  6, 4},
            {"cob",              8,  90,  7, 5},
            {"wattle_and_daub",  8,  60,  5, 3},
            {"wicker",          12,  85,  5, 4},
            {"lath_and_plaster",10,  75,  5, 3},
            {"blue_slate",       4,  50,  7, 4},
            {"green_slate",      4,  50,  7, 4},
            {"purple_slate",     4,  50,  6, 3},
            {"chalk",           20, 100,  9, 5},
            {"mud",             40, 110, 10, 6},
            {"dry_fitted_stone", 4,  60,  7, 4},
            {"cinder_blocks",    4,  40,  5, 2},
            {"cordwood",        10,  80,  5, 3},
            {"dirt_gabion",     30, 110,  8, 5},
            {"gravel_gabion",   10,  90,  7, 4},
            {"sand_gabion",     20, 100,  7, 4},
    };

    private static final String[] SURFACE_NAMES = {}; // no surface scatter

    private record VeinDef(BlockState state, int minY, int maxY, int size, int count) {}

    private static List<VeinDef>   veins        = null;
    private static List<BlockState> surfaceStates = null;
    private static boolean          initialized   = false;

    private static void init() {
        if (initialized) return;
        initialized = true;
        veins = new ArrayList<>();
        for (Object[] row : VEINS) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("earthworks", (String) row[0]);
            if (BuiltInRegistries.BLOCK.containsKey(id)) {
                veins.add(new VeinDef(
                        BuiltInRegistries.BLOCK.get(id).defaultBlockState(),
                        (int) row[1], (int) row[2], (int) row[3], (int) row[4]
                ));
            }
        }
        surfaceStates = new ArrayList<>();
        for (String name : SURFACE_NAMES) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("earthworks", name);
            if (BuiltInRegistries.BLOCK.containsKey(id)) {
                surfaceStates.add(BuiltInRegistries.BLOCK.get(id).defaultBlockState());
            }
        }
        if (!veins.isEmpty()) {
            RatsMod.LOGGER.info("Rats × Earthworks: {} vein types, {} surface types for Ratlantis",
                    veins.size(), surfaceStates.size());
        } else {
            RatsMod.LOGGER.info("Rats × Earthworks: no Earthworks blocks found — compat inactive");
        }
    }

    private static final Queue<long[]> pending = new ConcurrentLinkedQueue<>();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk()) return;
        LevelAccessor level = event.getLevel();
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(RatlantisDimensionRegistry.DIMENSION_KEY)) return;
        init();
        if (veins.isEmpty()) return;
        pending.add(new long[]{event.getChunk().getPos().x, event.getChunk().getPos().z});
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(RatlantisDimensionRegistry.DIMENSION_KEY)) return;
        if (pending.isEmpty()) return;

        int done = 0;
        while (!pending.isEmpty() && done < 2) {
            long[] entry = pending.poll();
            if (entry == null) break;
            int cx = (int) entry[0], cz = (int) entry[1];
            if (!serverLevel.hasChunk(cx, cz)) {
                pending.offer(entry);
                break;
            }
            generate(serverLevel, serverLevel.getChunk(cx, cz), cx, cz);
            done++;
        }
    }

    private static void generate(ServerLevel level, LevelChunk chunk, int cx, int cz) {
        RandomSource rand = level.getRandom();
        int baseX = cx * 16;
        int baseZ = cz * 16;
        int minBuild = level.getMinBuildHeight() + 1;
        int maxBuild = level.getMaxBuildHeight() - 1;

        for (VeinDef vein : veins) {
            for (int v = 0; v < vein.count(); v++) {
                int rangeY = Math.max(1, vein.maxY() - vein.minY());
                int oy = vein.minY() + rand.nextInt(rangeY);
                int ox = baseX + rand.nextInt(16);
                int oz = baseZ + rand.nextInt(16);
                oy = Math.max(minBuild, Math.min(oy, maxBuild));

                for (int i = 0; i < vein.size(); i++) {

                    int bx = ox + (rand.nextInt(3) - 1);
                    int by = oy + (rand.nextInt(3) - 1);
                    int bz = oz + (rand.nextInt(3) - 1);
                    if (by < minBuild || by > maxBuild) continue;

                    BlockPos pos = new BlockPos(bx, by, bz);
                    BlockState cur = level.getBlockState(pos);
                    if (cur.is(Blocks.STONE) || cur.is(Blocks.DEEPSLATE) || cur.is(Blocks.TUFF)) {
                        try {
                            level.setBlock(pos, vein.state(), 2);
                            ox = bx; oy = by; oz = bz;
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

    }
}
