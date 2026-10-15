package net.creater120.ghostblock.client.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class Suggestions {

    public static final SuggestionProvider<FabricClientCommandSource> getAllItemIds = (context, builder) -> {
        String remaining = builder.getRemaining().toLowerCase();

        for (Identifier id : Registries.ITEM.getIds()) {
            String full = id.toString();
            String path = id.getPath();
            String namespace = id.getNamespace();

            if (CommandSource.shouldSuggest(remaining, full) ||
                    CommandSource.shouldSuggest(remaining, path) ||
                    CommandSource.shouldSuggest(remaining, namespace)) {
                builder.suggest(path);
            }
        }

        return builder.buildFuture();
    };
}