package net.creater120.ghostblock.client.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

@Environment(EnvType.CLIENT)
public class Commands {

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {


            dispatcher.register(ClientCommandManager.literal("ghostgive")
                    .then(ClientCommandManager.argument("item", StringArgumentType.string())
                            .suggests(Suggestions.getAllItemIds)
                            .executes(CommandsFunctions::oneGiveClientCommand)
                            .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                    .executes(CommandsFunctions::giveClientCommand)
                            )
                    )
            );





            dispatcher.register(ClientCommandManager.literal("ghostname")
                    .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                            .executes(CommandsFunctions::ghostNameCommand)
                    )
            );

            dispatcher.register(ClientCommandManager.literal("ghostrename")
                    .executes(CommandsFunctions::ghostRenameCommand)
            );

            dispatcher.register(ClientCommandManager.literal("ghostlore")
                    .then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
                            .executes(CommandsFunctions::ghostLoreCommand)
                    )
            );

            dispatcher.register(ClientCommandManager.literal("ghostclearlore")
                    .executes(CommandsFunctions::ghostClearLoreCommand)
            );


            dispatcher.register(ClientCommandManager.literal("ghostremoveall")
                    .executes(CommandsFunctions::executeRemoveGhostBlockCommand)
            );


        });
    }
}