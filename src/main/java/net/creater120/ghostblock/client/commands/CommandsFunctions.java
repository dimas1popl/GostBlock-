package net.creater120.ghostblock.client.commands;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.creater120.ghostblock.client.GhostblockClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class CommandsFunctions {

    private static final Random RANDOM = new Random();


    public static int oneGiveClientCommand(CommandContext<FabricClientCommandSource> context) {
        return executeGiveClientCommand(context, 1);
    }

    public static int giveClientCommand(CommandContext<FabricClientCommandSource> context) {
        return executeGiveClientCommand(context, context.getArgument("amount", Integer.class));
    }


    public static int ghostNameCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        String name = context.getArgument("name", String.class);

        ItemStack stack = source.getPlayer().getMainHandStack();

        if (stack.isEmpty()) {
            source.sendFeedback(Text.literal("§cYou need to hold an item in your hand!"));
            return 0;
        }

        String coloredName = name.replace("&", "§");
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(coloredName));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        source.sendFeedback(Text.literal("§aRenamed item to: §f" + coloredName));
        return 1;
    }

    public static int ghostRenameCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        ItemStack stack = source.getPlayer().getMainHandStack();

        if (stack.isEmpty()) {
            source.sendFeedback(Text.literal("§cYou need to hold an item in your hand!"));
            return 0;
        }

        stack.remove(DataComponentTypes.CUSTOM_NAME);
        source.sendFeedback(Text.literal("§aRemoved custom name from item!"));
        return 1;
    }


    public static int ghostLoreCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        String text = context.getArgument("text", String.class);

        ItemStack stack = source.getPlayer().getMainHandStack();

        if (stack.isEmpty()) {
            source.sendFeedback(Text.literal("§cYou need to hold an item in your hand!"));
            return 0;
        }

        String coloredText = text.replace("&", "§");
        List<Text> lore = new ArrayList<>();

        String[] lines = coloredText.split("\\|");
        for (String line : lines) {
            lore.add(Text.literal(line.trim()));
        }

        LoreComponent loreComponent = new LoreComponent(lore);
        stack.set(DataComponentTypes.LORE, loreComponent);
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        source.sendFeedback(Text.literal("§aAdded lore to item! (" + lines.length + " lines)"));
        return 1;
    }

    public static int ghostClearLoreCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        ItemStack stack = source.getPlayer().getMainHandStack();

        if (stack.isEmpty()) {
            source.sendFeedback(Text.literal("§cYou need to hold an item in your hand!"));
            return 0;
        }

        stack.remove(DataComponentTypes.LORE);
        source.sendFeedback(Text.literal("§aCleared lore from item!"));
        return 1;
    }





    public static int executeRemoveGhostBlockCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();

        if (source.getClient().world != null && source.getPlayer() != null) {
            int removed = 0;
            for (var entry : GhostblockClient.ghostBlocks.entrySet()) {
                BlockPos pos = entry.getKey();
                if (source.getClient().world.getBlockState(pos) == entry.getValue() &&
                        GhostblockClient.originalStates.containsKey(pos)) {
                    source.getClient().world.setBlockState(pos, GhostblockClient.originalStates.get(pos));
                    removed++;
                }
            }

            GhostblockClient.ghostBlocks.clear();
            GhostblockClient.originalStates.clear();
            source.sendFeedback(Text.literal("§aRemoved " + removed + " ghost blocks and restored their original states."));
            return 1;
        } else {
            source.sendFeedback(Text.literal("§cPlayer or world not found."));
            return 0;
        }
    }


    public static int executeUndoCommand(CommandContext<FabricClientCommandSource> context, int amount) {
        FabricClientCommandSource source = context.getSource();
        source.sendFeedback(Text.literal("§eUndo feature coming soon!"));
        return 1;
    }

    public static int executeFillCommand(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        source.sendFeedback(Text.literal("§eFill feature coming soon!"));
        return 1;
    }


    private static int executeGiveClientCommand(CommandContext<FabricClientCommandSource> context, int amount) {
        FabricClientCommandSource source = context.getSource();
        String itemName = context.getArgument("item", String.class);

        if (!itemName.contains(":")) {
            itemName = "minecraft:" + itemName;
        }

        Item item = Registries.ITEM.get(Identifier.tryParse(itemName));

        if (item != Items.AIR) {
            ItemStack stack = new ItemStack(item, Math.max(1, Math.min(amount, 64)));
            source.getPlayer().getInventory().offerOrDrop(stack);
            source.sendFeedback(Text.literal("§aGave " + amount + " of " + itemName));
            return 1;
        } else {
            source.sendFeedback(Text.literal("§cItem not found: " + itemName));
            return 0;
        }
    }


    private static String getRomanNumeral(int number) {
        if (number >= 1000) return "M" + number;
        if (number >= 500) return "D" + number;
        if (number >= 100) return "C" + number;
        if (number >= 50) return "L" + number;
        if (number >= 40) return "XL";
        if (number >= 10) return "X" + (number > 10 ? getRomanNumeral(number - 10) : "");
        if (number >= 9) return "IX";
        if (number >= 5) return "V" + (number > 5 ? getRomanNumeral(number - 5) : "");
        if (number >= 4) return "IV";
        if (number >= 1) return "I" + (number > 1 ? getRomanNumeral(number - 1) : "");
        return String.valueOf(number);
    }
}