package dev.mayaqq.biomecompass;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface CompassExtension {
    void track(BlockPos pos, World world, PlayerEntity playerEntity, ItemStack itemStack, String biomeName);
}
