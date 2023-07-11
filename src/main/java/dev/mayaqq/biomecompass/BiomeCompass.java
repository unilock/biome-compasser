package dev.mayaqq.biomecompass;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.*;

public class BiomeCompass implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("BiomeCompass");

    @Override
    public void onInitialize() {
    }

    public static Identifier id(String id) {
        return new Identifier("biomecompass", id);
    }
}
