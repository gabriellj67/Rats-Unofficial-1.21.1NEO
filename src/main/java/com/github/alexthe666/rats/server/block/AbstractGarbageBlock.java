package com.github.alexthe666.rats.server.block;

import com.github.alexthe666.rats.RatConfig;
import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.entity.monster.PiedPiper;
import com.github.alexthe666.rats.server.entity.rat.AbstractRat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
public abstract class AbstractGarbageBlock extends FallingBlock {

	public final double spawnRateModifier;

	public AbstractGarbageBlock(BlockBehaviour.Properties properties, double spawnRateModifier) {
		super(properties);
		this.spawnRateModifier = spawnRateModifier;
	}

	protected abstract EntityType<? extends PathfinderMob> getEntityToSpawn();

	protected void postInitSpawn(PathfinderMob mob, RandomSource random) {

	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (random.nextFloat() > 0.15F) return;

		if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;

		if (random.nextFloat() > (RatConfig.garbageSpawnRate * spawnRateModifier)) return;

		if (level.getBlockState(pos.above()).isSuffocating(level, pos)) return;

		PathfinderMob mob = this.getEntityToSpawn().create(level);
		if (mob == null) return;
		mob.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 0, 0);

		if (mob.isInWall() || !mob.checkSpawnObstruction(level)) return;

		if (mob instanceof AbstractRat) {
			if (!level.getGameRules().getBoolean(RatsMod.SPAWN_RATS)) return;
			int ratCount = 0;
			for (net.minecraft.world.entity.Entity entity : level.getEntities(mob, mob.getBoundingBox().inflate(128))) {
				if (entity instanceof AbstractRat) ratCount++;
			}
			if (ratCount > 20) return;
			if (RatConfig.ratsSpawnLikeMonsters && !this.isDarkEnoughForMonsterSpawns(level, mob.blockPosition(), random))
				return;
		} else if (mob instanceof PiedPiper) {
			if (!level.getGameRules().getBoolean(RatsMod.SPAWN_PIPERS)) return;
			int piperCount = 0;
			for (net.minecraft.world.entity.Entity entity : level.getEntities(mob, mob.getBoundingBox().inflate(128))) {
				if (entity instanceof PiedPiper) piperCount++;
			}
			if (piperCount > 5) return;
			if (!this.isDarkEnoughForMonsterSpawns(level, mob.blockPosition(), random)) return;
		}

		EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, null);
		this.postInitSpawn(mob, random);
		level.tryAddFreshEntityWithPassengers(mob);
	}

	private boolean isDarkEnoughForMonsterSpawns(ServerLevelAccessor accessor, BlockPos pos, RandomSource random) {
		DimensionType dimensiontype = accessor.dimensionType();
		int i = dimensiontype.monsterSpawnBlockLightLimit();
		if (i < 15 && accessor.getBrightness(LightLayer.BLOCK, pos) > i) {
			return false;
		} else {
			int j = accessor.getLevel().isThundering() ? accessor.getMaxLocalRawBrightness(pos, 10) : accessor.getMaxLocalRawBrightness(pos);
			return j <= dimensiontype.monsterSpawnLightTest().sample(random);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
	}


	public boolean canMobSpawnHere(EntityType<?> entityType) {
		return entityType == this.getEntityToSpawn();
	}
}
