package com.github.alexthe666.rats.server.message;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.items.upgrades.OreMinerRatUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetOreMinerReturnPacket(int handOrdinal, BlockPos pos) implements CustomPacketPayload {

	private static final double MAX_DISTANCE_SQR = 256.0D * 256.0D;

	public static final Type<SetOreMinerReturnPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "set_ore_miner_return"));

	public static final StreamCodec<FriendlyByteBuf, SetOreMinerReturnPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SetOreMinerReturnPacket::handOrdinal,
			BlockPos.STREAM_CODEC, SetOreMinerReturnPacket::pos,
			SetOreMinerReturnPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(SetOreMinerReturnPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			Player player = context.player();
			if (player == null || packet.handOrdinal() < 0 || packet.handOrdinal() >= InteractionHand.values().length) {
				return;
			}
			if (packet.pos().distSqr(player.blockPosition()) > MAX_DISTANCE_SQR) {
				return;
			}
			ItemStack stack = player.getItemInHand(InteractionHand.values()[packet.handOrdinal()]);
			if (stack.getItem() instanceof OreMinerRatUpgradeItem item && item.hasMiningTimer()) {
				OreMinerRatUpgradeItem.setReturnPos(stack, GlobalPos.of(player.level().dimension(), packet.pos()));
			}
		});
	}
}
