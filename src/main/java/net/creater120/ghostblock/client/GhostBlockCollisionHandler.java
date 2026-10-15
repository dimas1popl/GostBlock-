package net.creater120.ghostblock.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class GhostBlockCollisionHandler {

    private static boolean wasCollisionEnabled = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            handlePlayerCollision(client);
            handleVehicleCollision(client);
        });
    }

    private static void handlePlayerCollision(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return;

        BlockPos belowPlayer = player.getBlockPos().down();
        BlockState state = GhostblockClient.ghostBlocks.get(belowPlayer);

        if (state != null && !state.isAir()) {
            if (player.getVelocity().y < 0 && player.getY() - belowPlayer.getY() < 0.6) {
                player.setVelocity(player.getVelocity().x, 0, player.getVelocity().z);
                player.setOnGround(true);
            }
        }

        BlockPos feetPos = player.getBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = new BlockPos(feetPos.getX() + x, feetPos.getY(), feetPos.getZ() + z);
                BlockState stateAt = GhostblockClient.ghostBlocks.get(checkPos);
                if (stateAt != null && !stateAt.isAir()) {
                    Box blockBox = new Box(checkPos);
                    Box playerBox = player.getBoundingBox();
                    if (playerBox.intersects(blockBox)) {
                        Vec3d push = new Vec3d(
                                (checkPos.getX() + 0.5 - player.getX()) * 0.5,
                                0,
                                (checkPos.getZ() + 0.5 - player.getZ()) * 0.5
                        );
                        player.addVelocity(push.x, 0, push.z);
                    }
                }
            }
        }
    }

    private static void handleVehicleCollision(MinecraftClient client) {
        Entity vehicle = client.player.getVehicle();
        if (vehicle == null) return;

        BlockPos underVehicle = vehicle.getBlockPos().down();

        BlockState ghostBlock = GhostblockClient.ghostBlocks.get(underVehicle);

        if (ghostBlock != null && !ghostBlock.isAir()) {


            if (vehicle instanceof BoatEntity) {
                if (vehicle.getVelocity().y < 0 && vehicle.getY() - underVehicle.getY() < 0.6) {
                    vehicle.setVelocity(vehicle.getVelocity().x, 0, vehicle.getVelocity().z);
                    vehicle.setOnGround(true);
                }
            }
        }

        if (vehicle instanceof AbstractMinecartEntity minecart) {
            checkMinecartMovement(minecart, client);
        }
    }

    private static void checkMinecartMovement(AbstractMinecartEntity minecart, MinecraftClient client) {
        BlockPos currentPos = minecart.getBlockPos().down();
        BlockState currentGhost = GhostblockClient.ghostBlocks.get(currentPos);

        if (currentGhost != null && !currentGhost.isAir()) {
            Vec3d vel = minecart.getVelocity();
            if (Math.abs(vel.x) + Math.abs(vel.z) < 0.5) {
                if (client.options.forwardKey.isPressed()) {
                    double direction = Math.toRadians(minecart.getYaw());
                    minecart.addVelocity(-Math.sin(direction) * 0.1, 0, Math.cos(direction) * 0.1);
                }
            }
        }
    }
}