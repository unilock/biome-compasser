package dev.mayaqq.biomecompass;

import dev.mayaqq.biomecompass.item.BiomeCompassItem;
import dev.mayaqq.biomecompass.registry.BiomeCompassItems;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.CompassAnglePredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.CompassItem;
import net.minecraft.util.Identifier;

public class BiomeCompassClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelPredicateProviderRegistry.register(
				BiomeCompassItems.BIOME_COMPASS,
				new Identifier("angle"),
				new CompassAnglePredicateProvider(
						(world, stack, entity) -> BiomeCompassItem.hasBiome(stack) ? BiomeCompassItem.createBiomePos(stack.getOrCreateNbt()) : CompassItem.createSpawnPos(world)
				)
		);
	}
}
