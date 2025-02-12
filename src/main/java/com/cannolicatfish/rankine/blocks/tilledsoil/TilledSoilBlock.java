package com.cannolicatfish.rankine.blocks.tilledsoil;

import com.cannolicatfish.rankine.blocks.states.TilledSoilTypes;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class TilledSoilBlock extends Block {
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE_0_7;
    public static final EnumProperty<TilledSoilTypes> SOIL_TYPE = EnumProperty.create("soil_type", TilledSoilTypes.class);

    protected static final VoxelShape SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    public TilledSoilBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(MOISTURE, 0).with(SOIL_TYPE, TilledSoilTypes.DIRT));
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE, SOIL_TYPE);
    }

    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.UP && !stateIn.isValidPosition(worldIn, currentPos)) {
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 1);
        }

        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState blockstate = worldIn.getBlockState(pos.up());
        return !blockstate.getMaterial().isSolid() || blockstate.getBlock() instanceof FenceGateBlock || blockstate.getBlock() instanceof MovingPistonBlock;
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return !this.getDefaultState().isValidPosition(context.getWorld(), context.getPos()) ? Blocks.DIRT.getDefaultState() : super.getStateForPlacement(context);
    }

    public boolean isTransparent(BlockState state) {
        return true;
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing, IPlantable plantable) {
        return true;
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return true;
    }

    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        if (!state.isValidPosition(worldIn, pos)) {
            turnToDirt(state, worldIn, pos);
        }

    }

    /**
     * Performs a random tick on a block.
     */
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        int i = state.get(MOISTURE);
        if (!hasWater(worldIn, pos) && !worldIn.isRainingAt(pos.up())) {
            if (i > 0) {
                worldIn.setBlockState(pos, state.with(MOISTURE, i - 1).with(SOIL_TYPE, state.get(SOIL_TYPE)), 2);
            } else if (!hasCrops(worldIn, pos)) {
                turnToDirt(state, worldIn, pos);
            }
        } else if (i < 7) {
            worldIn.setBlockState(pos, state.with(MOISTURE, 7).with(SOIL_TYPE, state.get(SOIL_TYPE)), 2);
        }

    }

    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        if (!worldIn.isRemote && net.minecraftforge.common.ForgeHooks.onFarmlandTrample(worldIn, pos, Blocks.DIRT.getDefaultState(), fallDistance, entityIn)) { // Forge: Move logic to Entity#canTrample
            turnToDirt(worldIn.getBlockState(pos), worldIn, pos);
        }

        super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
    }

    public static void turnToDirt(BlockState state, World worldIn, BlockPos pos) {
        Block SOIL;
        if (state.get(SOIL_TYPE).equals(TilledSoilTypes.DIRT)) {
            SOIL = Blocks.DIRT;
        } else if (state.get(SOIL_TYPE).equals(TilledSoilTypes.SOUL_SOIL)) {
            SOIL = Blocks.SOUL_SOIL;
        } else {
            SOIL = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate("rankine:"+state.get(SOIL_TYPE).getString()));
        }
        worldIn.setBlockState(pos, nudgeEntitiesWithNewState(state, SOIL.getDefaultState(), worldIn, pos));
    }

    private boolean hasCrops(IBlockReader worldIn, BlockPos pos) {
        BlockState plant = worldIn.getBlockState(pos.up());
        BlockState state = worldIn.getBlockState(pos);
        return plant.getBlock() instanceof net.minecraftforge.common.IPlantable && state.canSustainPlant(worldIn, pos, Direction.UP, (net.minecraftforge.common.IPlantable)plant.getBlock());
    }

    private static boolean hasWater(IWorldReader worldIn, BlockPos pos) {
        for(BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-4, 0, -4), pos.add(4, 1, 4))) {
            if (worldIn.getFluidState(blockpos).isTagged(FluidTags.WATER)) {
                return true;
            }
        }

        return net.minecraftforge.common.FarmlandWaterManager.hasBlockWaterTicket(worldIn, pos);
    }

    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }
}
