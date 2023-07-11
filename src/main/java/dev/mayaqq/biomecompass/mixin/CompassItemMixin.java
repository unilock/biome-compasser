package dev.mayaqq.biomecompass.mixin;

import com.mojang.serialization.DataResult;
import dev.mayaqq.biomecompass.CompassExtension;
import dev.mayaqq.biomecompass.gui.BiomeSelectionGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompassItem.class)
public abstract class CompassItemMixin extends Item implements CompassExtension {

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract String getTranslationKey(ItemStack stack);

    public CompassItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "getTranslationKey", at = @At("RETURN"), cancellable = true)
    private void getTranslationKey(ItemStack stack, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(isBiome(stack) ? "Biome Compass" : cir.getReturnValue());
    }

    @Inject(method = "hasGlint", at = @At("RETURN"), cancellable = true)
    private void hasGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(isBiome(stack) ? true : cir.getReturnValue());
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        // had to do this because im dummy and it works and shouldnt really cause any problems :clueless:
        ci.cancel();
    }

    @Unique
    private void writeBiomeNbt(RegistryKey<World> worldKey, BlockPos pos, NbtCompound nbt, String biomeName) {
        nbt.put("LodestonePos", NbtHelper.fromBlockPos(pos));
        if (biomeName != null) nbt.putString("BiomeName", biomeName);
        DataResult var10000 = World.CODEC.encodeStart(NbtOps.INSTANCE, worldKey);
        var10000.resultOrPartial(null).ifPresent((nbtElement) -> {
            nbt.put("LodestoneDimension", (NbtElement) nbtElement);
        });
        nbt.putBoolean("IsBiome", true);
        nbt.putBoolean("LodestoneTracked", true);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        super.use(world, user, hand);
        if (world.isClient || !isBiome(user.getStackInHand(hand))) return TypedActionResult.pass(user.getStackInHand(hand));
        ServerPlayerEntity player = (ServerPlayerEntity) user;
        BiomeSelectionGui.open(player, 0, hand);
        return TypedActionResult.success(user.getStackInHand(hand));
    }


    @Unique
    private static boolean isBiome(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        return nbtCompound != null && (nbtCompound.contains("IsBiome"));
    }

    @Override
    public void track(BlockPos pos, World world, PlayerEntity playerEntity, ItemStack itemStack, String biomeName) {
        world.playSound(null, playerEntity.getBlockPos(), SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
        boolean bl = itemStack.getCount() == 1;
        if (bl) {
            this.writeBiomeNbt(world.getRegistryKey(), pos, itemStack.getOrCreateNbt(), biomeName);
        } else {
            ItemStack itemStack2 = new ItemStack(Items.COMPASS, 1);
            NbtCompound nbtCompound = itemStack.hasNbt() ? itemStack.getNbt().copy() : new NbtCompound();
            itemStack2.setNbt(nbtCompound);
            itemStack.decrement(1);
            this.writeBiomeNbt(world.getRegistryKey(), pos, nbtCompound, biomeName);
            if (!playerEntity.getInventory().insertStack(itemStack2)) {
                playerEntity.dropItem(itemStack2, false);
            }
        }
    }
}
