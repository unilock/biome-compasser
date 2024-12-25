package dev.mayaqq.biomecompass.datagen.provider;

import dev.mayaqq.biomecompass.registry.BiomeCompassItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class BiomeCompassItemTagProvider extends FabricTagProvider.ItemTagProvider {
	public BiomeCompassItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
		super(output, completableFuture);
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
		this.getOrCreateTagBuilder(ItemTags.COMPASSES).add(BiomeCompassItems.BIOME_COMPASS);
	}
}
