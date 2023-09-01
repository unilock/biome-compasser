package dev.mayaqq.biomecompass.gui;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import dev.mayaqq.biomecompass.item.BiomeCompassItem;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.command.argument.RegistryPredicateArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class BiomeSelectionGui {

    public static Registry<Biome> getRegistry(ServerWorld world) {
        return world.getRegistryManager().get(RegistryKeys.BIOME);
    }

    public static void open(ServerPlayerEntity player, int page, Hand hand) {
        open(player, page, hand, "");
    }

    public static void open(ServerPlayerEntity player, int page, Hand hand, String filter) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);

        NbtCompound nbt = player.getStackInHand(hand).getNbt();
        StringBuilder title = new StringBuilder();

        title.append("Biome Compass");

        if (nbt != null && nbt.contains("BiomeName")) {
            String biomeName = nbt.getString("BiomeName");
            title.append(" §8| ");
            title.append(biomeName);
        }

        gui.setTitle(Text.of(title.toString()));

        Registry<Biome> biomes = getRegistry(player.getServerWorld());

        ArrayList<Biome> biomesList = new ArrayList<>(biomes.stream().toList());

        for (int i = 0; i < biomesList.size(); i++) {
            Biome biome = biomesList.get(i);
            Identifier biomeId = biomes.getId(biome);
            String name = Text.translatable("biome." + biomeId.getNamespace() + "." + biomeId.getPath()).getString();
            if (!name.contains(filter) && !biomeId.toString().contains(filter)) {
                biomesList.remove(i);
                i--;
            }
        }

        for (int i = page * 45; i < biomesList.size(); i++) {
            if (i >= 45 * page + 45) {
                break;
            }
            Biome biome = biomesList.get(i);
            Identifier biomeId = biomes.getId(biome);
            Text biomeName = Text.of("§2" + Text.translatable("biome." + biomeId.getNamespace() + "." + biomeId.getPath()).getString());

            Item biomeItem = biome.getTemperature() > 1.0 ? Items.LAVA_BUCKET : biome.getTemperature() < 0.15 ? Items.SNOWBALL : Items.GRASS_BLOCK;

            gui.setSlot(i - 45 * page, new GuiElementBuilder()
                    .setItem(biomeItem)
                    .setName(biomeName)
                    .setLore(new ArrayList<>(List.of(
                            Text.of("§7Identifier: " + biomeId),
                            Text.of("§7Warmth: " + biome.getTemperature()),
                            Text.of("§7Humidity: " + biome.getPrecipitation(BlockPos.ORIGIN))
                    )))
                    .setCallback((index, type, action) -> {
                        gui.close();
                        try {
                            Pair<BlockPos, RegistryEntry<Biome>> pair = executeLocateBiome(player.getBlockPos(), player.getServerWorld(), biome);
                            player.sendMessage(Text.of("Found " + biomeName.getString() + "§f at §6" + pair.getFirst().getX() + "§f, §6" + pair.getFirst().getY() + "§f, §6" + pair.getFirst().getZ() + " §fwhich is §6" +
                                    (int) getDistance(player.getBlockX(), player.getBlockZ(), pair.getFirst().getX(), pair.getFirst().getZ()) + " §fblocks away."), false);
                            ((BiomeCompassItem) player.getStackInHand(hand).getItem()).track(pair.getFirst(), player.getServerWorld(), player, player.getStackInHand(hand), biomeName.getString());
                        } catch (Exception e) {
                            player.sendMessage(Text.of("§4Could not find " + biomeName.getString()), false);
                        }
                    })
            );
        }

        for (int i = 45; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder()
                    .setItem(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.of(""))
            );
        }

        gui.setSlot(50, new GuiElementBuilder()
                .setItem(Items.OAK_SIGN)
                .setName(Text.of("Search"))
                .addLoreLine(Text.of("§7Current filter: " + filter))
                .addLoreLine(Text.of("§7Right click to reset"))
                .setCallback((index, type, action) -> {
                    if (type.isRight) {
                        open(player, page, hand);
                    } else {
                        filterInput(player, hand);
                    }
                })
        );
        gui.setSlot(49, new GuiElementBuilder()
                .setItem(Items.BARRIER)
                .setName(Text.of("§4Close"))
                .setLore(new ArrayList<>())
                .setCallback((index, type, action) -> gui.close())
        );

        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder()
                    .setItem(Items.ARROW)
                    .setName(Text.of("Previous Page"))
                    .setLore(new ArrayList<>())
                    .setCallback((index, type, action) -> open(player, page - 1, hand))
            );
        }

        if (page < biomesList.size() / 45) {
            gui.setSlot(53, new GuiElementBuilder()
                    .setItem(Items.ARROW)
                    .setName(Text.of("Next Page"))
                    .setLore(new ArrayList<>())
                    .setCallback((index, type, action) -> open(player, page + 1, hand))
            );
        }

        gui.open();
    }

    private static Pair<BlockPos, RegistryEntry<Biome>> executeLocateBiome(BlockPos pos, ServerWorld world, Biome biome) {
        Predicate<RegistryEntry<Biome>> predicate = new RegistryKeyBased<>(RegistryKey.of(RegistryKeys.BIOME, getRegistry(world).getId(biome)));
        return world.locateBiome(predicate, pos, 6400, 32, 64);
    }

    record RegistryKeyBased<T>(RegistryKey<T> key) implements RegistryPredicateArgumentType.RegistryPredicate<T> {

        public Either<RegistryKey<T>, TagKey<T>> getKey() {
            return Either.left(this.key);
        }

        public <E> Optional<RegistryPredicateArgumentType.RegistryPredicate<E>> tryCast(RegistryKey<? extends Registry<E>> registryRef) {
            return this.key.tryCast(registryRef).map(RegistryKeyBased::new);
        }

        public boolean test(RegistryEntry<T> registryEntry) {
            return registryEntry.matchesKey(this.key);
        }

        public String asString() {
            return this.key.getValue().toString();
        }
    }

    private static void filterInput(ServerPlayerEntity player, Hand hand) {
        try {
            SignGui gui = new SignGui(player) {
                @Override
                public void onClose() {
                    BiomeSelectionGui.open(player, 0, hand, this.getLine(0).getString());
                }
            };
            gui.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float getDistance(int x1, int y1, int x2, int y2) {
        int i = x2 - x1;
        int j = y2 - y1;
        return MathHelper.sqrt((float)(i * i + j * j));
    }
}
