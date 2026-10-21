package com.github.alexthe666.rats.server.message;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.items.upgrades.TravelersBackpackRatUpgradeItem;
import com.github.alexthe666.rats.server.misc.RatUpgradeUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenRatBackpackPacket(int ratId) implements CustomPacketPayload {

	private static final double MAX_DISTANCE_SQR = 64.0D;

	public static final Type<OpenRatBackpackPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "open_rat_backpack"));

	public static final StreamCodec<FriendlyByteBuf, OpenRatBackpackPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, OpenRatBackpackPacket::ratId,
			OpenRatBackpackPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(OpenRatBackpackPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !(player.level().getEntity(packet.ratId()) instanceof TamedRat rat)) {
				return;
			}
			if (!rat.isOwnedBy(player) || rat.distanceToSqr(player) > MAX_DISTANCE_SQR) {
				return;
			}
			RatUpgradeUtils.forEachUpgrade(rat, item -> item instanceof TravelersBackpackRatUpgradeItem, (stack, slot) -> TravelersBackpackRatUpgradeItem.openBackpack(player, stack));
		});
	}
}
