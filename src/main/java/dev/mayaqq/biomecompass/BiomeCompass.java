package dev.mayaqq.biomecompass;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BiomeCompass implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("BiomeCompass");

    @Override
    public void onInitialize() {
    }

    public static Identifier id(String id) {
        return new Identifier("biomecompass", id);
    }
}
