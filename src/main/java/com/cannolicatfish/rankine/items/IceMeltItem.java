package com.cannolicatfish.rankine.items;

import com.cannolicatfish.rankine.init.Config;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class IceMeltItem extends Item {
    public IceMeltItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World worldIn = context.getWorld();
        BlockPos pos = context.getPos();

        int radius = Config.GENERAL.ICEMELT_RANGE.get();

        if (!worldIn.isRemote) {
            for (BlockPos b : BlockPos.getAllInBoxMutable(pos.add(-radius, -radius, -radius), pos.add(radius, radius, radius))) {
                Block blk = worldIn.getBlockState(b).getBlock();
                if (blk.isIn(BlockTags.ICE) && b.distanceSq(pos) <= radius*radius) {
                    worldIn.setBlockState(b, Blocks.WATER.getDefaultState(), 3);
                } else if (blk.matchesBlock(Blocks.SNOW) && b.distanceSq(pos) <= radius*radius) {
                    worldIn.destroyBlock(b,false);
                }
            }
        }
        worldIn.playSound(null,pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,0.5f,0.3f);
        context.getItem().shrink(1);
        return ActionResultType.SUCCESS;

    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
            tooltip.add(new StringTextComponent("Melts snow and ice").mergeStyle(TextFormatting.GRAY));
    }


}