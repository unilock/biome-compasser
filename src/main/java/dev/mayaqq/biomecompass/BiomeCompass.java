package dev.mayaqq.biomecompass;

import dev.mayaqq.biomecompass.registry.BiomeCompassItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BiomeCompass implements ModInitializer {
    public static final String MOD_ID = "biomecompass";
    public static final Logger LOGGER = LoggerFactory.getLogger("BiomeCompass");

    @Override
    public void onInitialize() {
        BiomeCompassItems.register();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
