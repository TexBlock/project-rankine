package com.cannolicatfish.rankine.items.tools;

import com.cannolicatfish.rankine.init.Config;
import com.cannolicatfish.rankine.init.RankineItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class MagnetometerItem extends Item {
    private final int range;
    public MagnetometerItem(int range, Properties properties) {
        super(properties);
        this.range = range;
    }

    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity playerentity = context.getPlayer();
        IWorld iworld = context.getWorld();
        Direction e = context.getFace();
        IBlockReader reader = context.getWorld();
        BlockPos blockpos = context.getPos();
        BlockState blockstate = iworld.getBlockState(blockpos);
        BlockState ORE = null;
        boolean found = false;
        for (int x = 0; x <= range; x++) {
            if (e.getOpposite() == Direction.DOWN) {
                if (reader.getBlockState(blockpos.down(x)).getBlock().getTags().contains(new ResourceLocation("forge:ores"))){
                    found = true;
                    ORE = reader.getBlockState(blockpos.down(x));
                    break;
                }
            }
            if (e.getOpposite() == Direction.UP) {
                if (reader.getBlockState(blockpos.up(x)).getBlock().getTags().contains(new ResourceLocation("forge:ores"))){
                    found = true;
                    ORE = reader.getBlockState(blockpos.up(x));
                    break;
                }
            }
            if (e.getOpposite() == Direction.NORTH) {
                if (reader.getBlockState(blockpos.north(x)).getBlock().getTags().contains(new ResourceLocation("forge:ores"))){
                    found = true;
                    ORE = reader.getBlockState(blockpos.north(x));
                    break;
                }
            }
            if (e.getOpposite() == Direction.SOUTH) {
                if (reader.getBlockState(blockpos.south(x)).getBlock().getTags().contains(new ResourceLocation("forge:ores"))){
                    found = true;
                    ORE = reader.getBlockState(blockpos.south(x));
                    break;
                }
            }
            if (e.getOpposite() == Direction.EAST) {
                if (reader.getBlockState(blockpos.east(x)).getBlock().getTags().contains(new ResourceLocation("forge:ores"))){
                    found = true;
                    ORE = reader.getBlockState(blockpos.east(x));
                    break;
                }
            }
            if (e.getOpposite() == Direction.WEST) {
                if (reader.getBlockState(blockpos.west(x)).getBlock().getTags().contains(new ResourceLocation("forge:ores"))){
                    found = true;
                    ORE = reader.getBlockState(blockpos.west(x));
                    break;
                }
            }

        }
        if (found && playerentity != null) {
            playerentity.sendStatusMessage(new StringTextComponent("An ore of harvest level "+ ORE.getBlock().getHarvestLevel(ORE) +" is nearby"), true);
            if (this.isDamageable() && !iworld.isRemote()) {
                context.getItem().damageItem(1, playerentity, (p_219998_1_) -> {
                    p_219998_1_.sendBreakAnimation(context.getHand());
                });
            }
        }
        return ActionResultType.SUCCESS;
    }
}
