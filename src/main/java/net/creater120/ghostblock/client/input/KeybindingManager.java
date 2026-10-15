package net.creater120.ghostblock.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.creater120.ghostblock.client.GhostblockClient;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class KeybindingManager {
    private static boolean wasUnpressed = true;
    private static boolean wasContinuousUnpressed = true;
    private static boolean continuousActive = false;

    public static KeyBinding placeBlockKeyBinding;
    public static KeyBinding continuousPlaceKeyBinding;
    public static KeyBinding removeAllKeyBinding;

    public static void registerKeybindings() {
        placeBlockKeyBinding = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.ghostblock.place_block",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        "key.category.ghostblock"
                )
        );

        continuousPlaceKeyBinding = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.ghostblock.continuous_place",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_GRAVE_ACCENT,
                        "key.category.ghostblock"
                )
        );

        removeAllKeyBinding = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.ghostblock.remove_all",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_R,
                        "key.category.ghostblock"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (placeBlockKeyBinding.wasPressed()) {
                placeGhostBlockUnderPlayer(client);
                wasUnpressed = false;
            } else {
                wasUnpressed = true;
            }

            if (continuousPlaceKeyBinding.isPressed()) {
                if (wasContinuousUnpressed) {
                    continuousActive = true;
                    wasContinuousUnpressed = false;
                }
                if (continuousActive) {
                    placeGhostBlockUnderPlayer(client);
                }
            } else {
                wasContinuousUnpressed = true;
                continuousActive = false;
            }

            if (removeAllKeyBinding.wasPressed()) {
                removeAllGhostBlocks(client);
            }
        });
    }

    private static void removeAllGhostBlocks(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        int removed = 0;
        for (var entry : GhostblockClient.ghostBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState originalState = GhostblockClient.originalStates.get(pos);
            if (originalState != null) {
                client.world.setBlockState(pos, originalState);
                removed++;
            }
        }

        GhostblockClient.ghostBlocks.clear();
        GhostblockClient.originalStates.clear();

        client.player.sendMessage(Text.literal("§aRemoved " + removed + " ghost blocks!"), false);
    }

    private static void placeGhostBlockUnderPlayer(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        boolean isInBoat = client.player.getVehicle() instanceof BoatEntity;
        boolean isInMinecart = client.player.getVehicle() instanceof MinecartEntity;

        ItemStack heldItem = client.player.getMainHandStack();

        if (heldItem.isEmpty()) {
            if (!continuousActive) {
                client.player.sendMessage(Text.literal("You are not holding a block."), false);
            }
            return;
        }

        if (!(heldItem.getItem() instanceof BlockItem)) {
            if (!continuousActive) {
                client.player.sendMessage(Text.literal("You are not holding a block."), false);
            }
            return;
        }

        BlockItem blockItem = (BlockItem) heldItem.getItem();
        BlockState blockState = blockItem.getBlock().getDefaultState();

        if (blockState == null) {
            return;
        }

        if (isInBoat) {
            BlockPos vehiclePos = client.player.getVehicle().getBlockPos();
            if (isInBoat)  {
                place3x3Area(client, vehiclePos, blockState);
            }
        } else {
            BlockPos feetPos = client.player.getBlockPos();
            BlockPos playerPos = new BlockPos(feetPos.getX(), feetPos.getY() - 1, feetPos.getZ());
            placeSingleBlock(client, playerPos, blockState);
        }
    }

    private static void placeSingleBlock(MinecraftClient client, BlockPos pos, BlockState blockState) {
        if (!continuousActive && GhostblockClient.ghostBlocks.get(pos) == blockState && wasUnpressed) {
            BlockState originalState = GhostblockClient.originalStates.get(pos);
            if (originalState != null) {
                client.world.setBlockState(pos, originalState);
            }
            GhostblockClient.originalStates.remove(pos);
            GhostblockClient.ghostBlocks.remove(pos);
            return;
        }

        if (GhostblockClient.originalStates.get(pos) == null) {
            GhostblockClient.originalStates.put(pos, client.world.getBlockState(pos));
        }

        client.world.setBlockState(pos, blockState);
        GhostblockClient.ghostBlocks.put(pos, blockState);
    }

    private static void place3x3Area(MinecraftClient client, BlockPos centerPos, BlockState blockState) {
        int placed = 0;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = new BlockPos(
                        centerPos.getX() + x,
                        centerPos.getY() - 1,
                        centerPos.getZ() + z
                );

                if (GhostblockClient.originalStates.get(pos) == null) {
                    GhostblockClient.originalStates.put(pos, client.world.getBlockState(pos));
                }

                client.world.setBlockState(pos, blockState);
                GhostblockClient.ghostBlocks.put(pos, blockState);
                placed++;
            }
        }

        if (!continuousActive && placed > 0) {
            client.player.sendMessage(Text.literal("§aPlaced " + placed + " blocks in 3x3 area!"), false);
        }
    }
}