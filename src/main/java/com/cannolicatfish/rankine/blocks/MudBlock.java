package com.cannolicatfish.rankine.blocks;

import com.cannolicatfish.rankine.init.RankineLists;
import com.cannolicatfish.rankine.init.VanillaIntegration;
import com.cannolicatfish.rankine.util.WorldgenUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;

import java.util.Random;

public class MudBlock extends Block {
    public MudBlock() {
        super(AbstractBlock.Properties.create(Material.EARTH, MaterialColor.DIRT).sound(SoundType.GROUND).harvestTool(ToolType.SHOVEL).hardnessAndResistance(0.5F).harvestLevel(0));
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return true;
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        if (!WorldgenUtils.isWet((ISeedReader) worldIn, pos)) {
            worldIn.setBlockState(pos, RankineLists.SOIL_BLOCKS.get(RankineLists.MUD_BLOCKS.indexOf(state.getBlock())).getDefaultState(), 2);
        }
    }

}
