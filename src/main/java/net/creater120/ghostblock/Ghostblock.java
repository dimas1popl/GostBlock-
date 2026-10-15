package net.creater120.ghostblock;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ghostblock implements ModInitializer {
    public static final String MOD_ID = "ghostblock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("GhostBlock Mod initialized!");
    }
}