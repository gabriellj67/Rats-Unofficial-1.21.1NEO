package com.github.alexthe666.rats.server.message;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.client.ClientPacketHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenOreMinerScreenPacket(int handOrdinal, int seconds, boolean hasReturnPos, BlockPos returnPos) implements CustomPacketPayload {

	public static final Type<OpenOreMinerScreenPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RatsMod.MODID, "open_ore_miner_screen"));

	public static final StreamCodec<FriendlyByteBuf, OpenOreMinerScreenPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, OpenOreMinerScreenPacket::handOrdinal,
			ByteBufCodecs.VAR_INT, OpenOreMinerScreenPacket::seconds,
			ByteBufCodecs.BOOL, OpenOreMinerScreenPacket::hasReturnPos,
			BlockPos.STREAM_CODEC, OpenOreMinerScreenPacket::returnPos,
			OpenOreMinerScreenPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(OpenOreMinerScreenPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> ClientPacketHandlers.handleOpenOreMinerScreen(packet));
	}
}
