package com.github.alexthe666.rats.server.items;

import com.github.alexthe666.rats.registry.RatlantisBlockRegistry;
import com.github.alexthe666.rats.server.block.entity.RatlantisTokenBlockEntity;
import com.github.alexthe666.rats.server.misc.RatsLangConstants;
import com.github.alexthe666.rats.server.world.SquarePortalShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SquareChunkyCheeseTokenItem extends LoreTagItem {

	public SquareChunkyCheeseTokenItem(Properties properties) {
		super(properties, 2);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		BlockPos origin = context.getClickedPos().relative(context.getClickedFace());
		Direction right = context.getHorizontalDirection().getClockWise();

		if (!new SquarePortalShape(origin, right).hasSpace(level)) {
			if (player != null) {
				player.displayClientMessage(Component.translatable(RatsLangConstants.SQUARE_PORTAL_NO_SPACE), true);
			}
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			level.setBlockAndUpdate(origin, RatlantisBlockRegistry.CHUNKY_CHEESE_TOKEN.get().defaultBlockState());
			if (level.getBlockEntity(origin) instanceof RatlantisTokenBlockEntity token) {
				token.setSquare(right);
			}
			if (player == null || !player.getAbilities().instabuild) {
				context.getItemInHand().shrink(1);
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide());
	}
}
