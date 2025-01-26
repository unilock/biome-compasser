package dev.mayaqq.biomecompass.item;

import dev.mayaqq.biomecompass.BiomeCompass;
import dev.mayaqq.biomecompass.gui.BiomeSelectionGui;
import dev.mayaqq.biomecompass.helper.TextHelper;
import dev.mayaqq.biomecompass.registry.BiomeCompassItems;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BiomeCompassItem extends Item implements PolymerItem {
    public static final String BIOME_NAME_KEY = BiomeCompass.id("biome_name").toString();
    public static final String BIOME_DIMENSION_KEY = BiomeCompass.id("biome_dimension").toString();
    public static final String BIOME_POS_KEY = BiomeCompass.id("biome_pos").toString();
    public static final String BIOME_TRACKED_KEY = BiomeCompass.id("biome_tracked").toString();

    public BiomeCompassItem(Settings settings) {
        super(settings);
    }

    private void writeNbt(RegistryKey<World> worldKey, BlockPos pos, NbtCompound nbt, String biomeName) {
        if (biomeName != null) nbt.putString(BIOME_NAME_KEY, biomeName);
        nbt.put(BIOME_POS_KEY, NbtHelper.fromBlockPos(pos));
        World.CODEC.encodeStart(NbtOps.INSTANCE, worldKey).resultOrPartial(BiomeCompass.LOGGER::error).ifPresent(nbtElement -> nbt.put(BIOME_DIMENSION_KEY, nbtElement));
        nbt.putBoolean(BIOME_TRACKED_KEY, true);
    }

    public void track(BlockPos pos, World world, PlayerEntity player, ItemStack oldCompass, String biomeName) {
        world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        if (oldCompass.getCount() == 1) {
            this.writeNbt(world.getRegistryKey(), pos, oldCompass.getOrCreateNbt(), biomeName);
        } else {
            oldCompass.decrement(1);
            ItemStack newCompass = BiomeCompassItems.BIOME_COMPASS.getDefaultStack();

            NbtCompound nbt = oldCompass.hasNbt() ? oldCompass.getNbt().copy() : new NbtCompound();
            this.writeNbt(world.getRegistryKey(), pos, nbt, biomeName);
            newCompass.setNbt(nbt);

            player.getInventory().offerOrDrop(newCompass);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (stack.hasNbt() && stack.getNbt().contains(BIOME_NAME_KEY) && stack.getNbt().contains(BIOME_POS_KEY)) {
            tooltip.add(Text.translatable("item.biomecompass.biome_compass.tooltip.biome_name", TextHelper.getBiomeNameFormatted(stack)));
            tooltip.add(Text.translatable("item.biomecompass.biome_compass.tooltip.biome_pos", TextHelper.getBlockPosFormatted(stack)));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            if (player.isCreative() && player.isSneaking() && player.getStackInHand(hand).hasNbt() && player.getStackInHand(hand).getNbt().contains(BIOME_POS_KEY)) {
                BlockPos pos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, NbtHelper.toBlockPos(player.getStackInHand(hand).getNbt().getCompound(BIOME_POS_KEY)));
                player.requestTeleport(pos.getX(), pos.getY(), pos.getZ());
                return TypedActionResult.success(user.getStackInHand(hand));
            }

            BiomeSelectionGui.open(player, 0, hand);
            return TypedActionResult.success(user.getStackInHand(hand));
        } else {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }
    }

    public static boolean hasBiome(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains(BIOME_NAME_KEY);
    }

    public static GlobalPos createBiomePos(NbtCompound nbt) {
        if (nbt.contains(BIOME_POS_KEY) && nbt.contains(BIOME_DIMENSION_KEY)) {
            Optional<RegistryKey<World>> optional = getBiomeDimension(nbt);
            if (optional.isPresent()) {
                BlockPos blockPos = NbtHelper.toBlockPos(nbt.getCompound(BIOME_POS_KEY));
                return GlobalPos.create(optional.get(), blockPos);
            }
        }

        return null;
    }

    private static Optional<RegistryKey<World>> getBiomeDimension(NbtCompound nbt) {
        return World.CODEC.parse(NbtOps.INSTANCE, nbt.get(BIOME_DIMENSION_KEY)).resultOrPartial(BiomeCompass.LOGGER::error);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity serverPlayerEntity) {
        return Items.COMPASS;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipContext context, @Nullable ServerPlayerEntity player) {
        ItemStack fake = PolymerItem.super.getPolymerItemStack(itemStack, context, player);
        if (hasBiome(itemStack)) {
            NbtCompound nbt = itemStack.getNbt().copy();
            nbt.put(CompassItem.LODESTONE_POS_KEY, nbt.getCompound(BIOME_POS_KEY));
            nbt.put(CompassItem.LODESTONE_DIMENSION_KEY, nbt.getCompound(BIOME_DIMENSION_KEY));
            nbt.putBoolean(CompassItem.LODESTONE_TRACKED_KEY, nbt.getBoolean(BIOME_TRACKED_KEY));
            fake.setNbt(nbt);
//          if (!PolymerResourcePackUtils.hasPack(player)) {
            fake.addEnchantment(Enchantments.INFINITY, 0);
//          }
        }
        return fake;
    }
}
