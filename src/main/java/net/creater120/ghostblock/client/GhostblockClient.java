package net.creater120.ghostblock.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.creater120.ghostblock.client.commands.Commands;
import net.creater120.ghostblock.client.input.KeybindingManager;
import net.creater120.ghostblock.client.utils.ConfigManager;
import net.creater120.ghostblock.client.utils.SchematicManager;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class GhostblockClient implements ClientModInitializer {
    public static final Map<BlockPos, BlockState> ghostBlocks = new HashMap<>();
    public static final Map<BlockPos, BlockState> originalStates = new HashMap<>();
    public static GameMode originalGameMode = null;
    private boolean wasInWorld = false;

    @Override
    public void onInitializeClient() {
        ConfigManager.getInstance();
        SchematicManager.getInstance();

        KeybindingManager.registerKeybindings();
        Commands.registerCommands();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean inWorld = client.world != null && client.player != null;
            if (inWorld && !wasInWorld) {
                ghostBlocks.clear();
                originalStates.clear();
            }
            wasInWorld = inWorld;
        });
    }

}