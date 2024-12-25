package dev.mayaqq.biomecompass.datagen.provider;

import dev.mayaqq.biomecompass.registry.BiomeCompassItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;

public class BiomeCompassModelProvider extends FabricModelProvider {
	public BiomeCompassModelProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {}

	@Override
	public void generateItemModels(ItemModelGenerator itemModelGenerator) {
		itemModelGenerator.registerCompass(BiomeCompassItems.BIOME_COMPASS);
	}
}
