package com.github.alexthe666.rats.client;

import com.github.alexthe666.rats.client.gui.*;
import com.github.alexthe666.rats.client.util.RatRecordSoundInstance;
import com.github.alexthe666.rats.server.capability.SelectedRat;
import com.github.alexthe666.rats.server.entity.rat.TamedRat;
import com.github.alexthe666.rats.server.inventory.RatMenu;
import com.github.alexthe666.rats.server.message.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientPacketHandlers {

    private ClientPacketHandlers() {}
	// ManageRatStaffPacket
    public static void handleManageRatStaff(ManageRatStaffPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (packet.clear()) {
            SelectedRat.clear(mc.player);
        } else {
            Entity e = mc.player.level().getEntity(packet.entityId());
            if (e instanceof TamedRat rat) {
                if (packet.openGUI()) {
                    switch (packet.staffToOpen()) {
                        case 1 -> mc.setScreen(new RadiusStaffScreen(rat, packet.pos()));
                        case 2 -> mc.setScreen(new PatrolStaffScreen(rat, packet.pos()));
                        default -> mc.setScreen(new CheeseStaffScreen(rat, packet.pos(), Direction.values()[packet.dirOrd()]));
                    }
                }
            }
        }
    }

	public static void handleOpenOreMinerScreen(OpenOreMinerScreenPacket packet) {
		Minecraft.getInstance().setScreen(new OreMinerScreen(packet.handOrdinal(), packet.seconds(), packet.hasReturnPos() ? packet.returnPos() : null));
	}

	// OpenRatScreenPacket
	public static void handleOpenRatScreen(OpenRatScreenPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = mc.level.getEntity(packet.entityId());
        if (!(entity instanceof TamedRat rat)) return;
        LocalPlayer localplayer = mc.player;

        RatMenu menu;
        if (localplayer.containerMenu instanceof RatMenu existing && existing.containerId == packet.containerId()) {
            menu = existing;
        } else {
            menu = new RatMenu(packet.containerId(), new SimpleContainer(6), localplayer.getInventory());
            localplayer.containerMenu = menu;
        }
        mc.setScreen(new RatScreen(menu, localplayer.getInventory(), rat));
    }

    // SyncPlaguePacket
    public static void handleSyncPlague(SyncPlaguePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = mc.level.getEntity(packet.entityId());
        if (entity instanceof LivingEntity living) {
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect =
                    net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getHolder(packet.effectId()).orElse(null);
            if (effect != null) {
                if (packet.duration() == 0) {
                    living.removeEffect(effect);
                } else {
                    net.minecraft.world.effect.MobEffectInstance instance = new net.minecraft.world.effect.MobEffectInstance(
                            effect, packet.duration(), packet.amplifier(),
                            packet.isEffectAmbient(), packet.isEffectVisible(), packet.effectShowsIcon());
                    living.forceAddEffect(instance, null);
                }
            }
        }
    }

    // UpdateCurdlerFluidPacket
    public static void handleUpdateCurdlerFluid(UpdateCurdlerFluidPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.of(packet.blockPos());
        net.minecraft.world.level.Level level = mc.level;
        if (level != null && level.getBlockEntity(pos) instanceof com.github.alexthe666.rats.server.block.entity.AutoCurdlerBlockEntity curdler) {
            curdler.getTank().setFluid(packet.fluid());
        }
    }

    // UpdateRatMusicPacket
    public static void handleUpdateRatMusic(UpdateRatMusicPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = mc.level.getEntity(packet.id());
        if (entity instanceof TamedRat rat) {
            mc.getSoundManager().queueTickingSound(new RatRecordSoundInstance(rat, packet.record().value()));
            mc.gui.setNowPlaying(packet.record().value().getDescription());
        }
    }

    // UpdateSelectedRatPacket
    public static void handleUpdateSelectedRat(UpdateSelectedRatPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = mc.level.getEntity(packet.entityId());
        if (entity instanceof LivingEntity living) {
            SelectedRat.setLocal(living, packet.ratId());
        }
    }
}
