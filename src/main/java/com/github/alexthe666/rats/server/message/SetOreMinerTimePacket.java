package com.github.alexthe666.rats.server.message;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.items.upgrades.OreMinerRatUpgradeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetOreMinerTimePacket(int handOrdinal, int seconds) implements CustomPacketPayload {

	public static final Type<SetOreMinerTimePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "set_ore_miner_time"));

	public static final StreamCodec<FriendlyByteBuf, SetOreMinerTimePacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SetOreMinerTimePacket::handOrdinal,
			ByteBufCodecs.VAR_INT, SetOreMinerTimePacket::seconds,
			SetOreMinerTimePacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(SetOreMinerTimePacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			Player player = context.player();
			if (player == null || packet.handOrdinal() < 0 || packet.handOrdinal() >= InteractionHand.values().length) {
				return;
			}
			ItemStack stack = player.getItemInHand(InteractionHand.values()[packet.handOrdinal()]);
			if (stack.getItem() instanceof OreMinerRatUpgradeItem item && item.hasMiningTimer()) {
				OreMinerRatUpgradeItem.setMiningSeconds(stack, packet.seconds());
			}
		});
	}
}
