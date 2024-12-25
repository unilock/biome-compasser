package dev.mayaqq.biomecompass.registry;

import dev.mayaqq.biomecompass.BiomeCompass;
import dev.mayaqq.biomecompass.item.BiomeCompassItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BiomeCompassItems {
    public static final Item BIOME_COMPASS = register("biome_compass", new BiomeCompassItem(new Item.Settings()));

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(BIOME_COMPASS));
    }

    private static Item register(String path, Item item) {
        return Registry.register(Registries.ITEM, BiomeCompass.id(path), item);
    }
}