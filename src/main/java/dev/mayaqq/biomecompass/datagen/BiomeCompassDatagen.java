package dev.mayaqq.biomecompass.datagen;

import dev.mayaqq.biomecompass.datagen.provider.BiomeCompassItemTagProvider;
import dev.mayaqq.biomecompass.datagen.provider.BiomeCompassModelProvider;
import dev.mayaqq.biomecompass.datagen.provider.BiomeCompassRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class BiomeCompassDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fdg) {
        FabricDataGenerator.Pack pack = fdg.createPack();
        pack.addProvider(BiomeCompassItemTagProvider::new);
        pack.addProvider(BiomeCompassModelProvider::new);
        pack.addProvider(BiomeCompassRecipeProvider::new);
    }
}
