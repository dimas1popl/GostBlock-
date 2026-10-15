package net.creater120.ghostblock.client.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.creater120.ghostblock.client.GhostblockClient;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")public class SchematicManager {
    private static SchematicManager instance;
    private final Path schematicsDir;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private SchematicManager() {
        schematicsDir = FabricLoader.getInstance().getGameDir().resolve("ghostblock_schematics");
        if (!schematicsDir.toFile().exists()) {
            schematicsDir.toFile().mkdirs();
        }
    }

    public static SchematicManager getInstance() {
        if (instance == null) {
            instance = new SchematicManager();
        }
        return instance;
    }

    public void saveSchematic(ClientPlayerEntity player, String name) {
        if (GhostblockClient.ghostBlocks.isEmpty()) {
            player.sendMessage(Text.literal("§cNo ghost blocks to save!"), false);
            return;
        }

        Map<String, Object> schematic = new HashMap<>();
        schematic.put("name", name);
        schematic.put("author", player.getName().getString());

        Map<String, String> blocks = new HashMap<>();
        for (var entry : GhostblockClient.ghostBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
            String posKey = pos.getX() + "," + pos.getY() + "," + pos.getZ();
            blocks.put(posKey, blockId);
        }

        schematic.put("blocks", blocks);
        schematic.put("count", blocks.size());

        File file = schematicsDir.resolve(name + ".json").toFile();
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(schematic, writer);
            player.sendMessage(Text.literal("§aSaved schematic '" + name + "' with " + blocks.size() + " blocks!"), false);
        } catch (IOException e) {
            player.sendMessage(Text.literal("§cFailed to save schematic: " + e.getMessage()), false);
            e.printStackTrace();
        }
    }

    public void loadSchematic(ClientPlayerEntity player, String name) {
        File file = schematicsDir.resolve(name + ".json").toFile();
        if (!file.exists()) {
            player.sendMessage(Text.literal("§cSchematic '" + name + "' not found!"), false);
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> schematic = gson.fromJson(reader, type);

            Map<String, String> blocks = (Map<String, String>) schematic.get("blocks");

            BlockPos playerPos = player.getBlockPos();
            BlockPos offset = findOffset(playerPos, blocks);

            int loaded = 0;
            for (var entry : blocks.entrySet()) {
                String[] coords = entry.getKey().split(",");
                int x = Integer.parseInt(coords[0]) + offset.getX();
                int y = Integer.parseInt(coords[1]) + offset.getY();
                int z = Integer.parseInt(coords[2]) + offset.getZ();
                BlockPos pos = new BlockPos(x, y, z);

                Block block = Registries.BLOCK.get(Identifier.tryParse(entry.getValue()));
                if (block != null) {
                    BlockState state = block.getDefaultState();

                    if (GhostblockClient.originalStates.get(pos) == null) {
                        GhostblockClient.originalStates.put(pos, player.getEntityWorld().getBlockState(pos));
                    }

                    player.getEntityWorld().setBlockState(pos, state);
                    GhostblockClient.ghostBlocks.put(pos, state);
                    loaded++;
                }
            }

            player.sendMessage(Text.literal("§aLoaded schematic '" + name + "' with " + loaded + " blocks!"), false);
        } catch (IOException e) {
            player.sendMessage(Text.literal("§cFailed to load schematic: " + e.getMessage()), false);
            e.printStackTrace();
        }
    }

    private BlockPos findOffset(BlockPos playerPos, Map<String, String> blocks) {
        if (blocks.isEmpty()) return BlockPos.ORIGIN;

        String firstKey = blocks.keySet().iterator().next();
        String[] coords = firstKey.split(",");
        int minX = Integer.parseInt(coords[0]);
        int minY = Integer.parseInt(coords[1]);
        int minZ = Integer.parseInt(coords[2]);

        return new BlockPos(
                playerPos.getX() - minX,
                playerPos.getY() - minY,
                playerPos.getZ() - minZ
        );
    }

    public List<String> getSchematicList() {
        List<String> schematics = new ArrayList<>();
        File[] files = schematicsDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File file : files) {
                schematics.add(file.getName().replace(".json", ""));
            }
        }
        return schematics;
    }
}