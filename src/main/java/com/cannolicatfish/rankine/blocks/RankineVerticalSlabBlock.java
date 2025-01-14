package com.cannolicatfish.rankine.blocks;

import com.cannolicatfish.rankine.blocks.states.VerticalSlabStates;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class RankineVerticalSlabBlock extends Block implements IWaterLoggable {
    public static final DirectionProperty HORIZONTAL_FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final EnumProperty<VerticalSlabStates> TYPE = EnumProperty.create("type", VerticalSlabStates .class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape STRAIGHT_N = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    protected static final VoxelShape STRAIGHT_S = Block.makeCuboidShape(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape STRAIGHT_W = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);
    protected static final VoxelShape STRAIGHT_E = Block.makeCuboidShape(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape INNER_N = Block.makeCuboidShape(0.0D, 0.0D, 8.0D, 8.0D, 16.0D, 16.0D);
    protected static final VoxelShape INNER_S = Block.makeCuboidShape(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    protected static final VoxelShape INNER_E = Block.makeCuboidShape(0.0D, 0.0D, 8.0D, 8.0D, 16.0D, 16.0D);
    protected static final VoxelShape INNER_W = Block.makeCuboidShape(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);


    public RankineVerticalSlabBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(TYPE, VerticalSlabStates.STRAIGHT).with(HORIZONTAL_FACING, Direction.NORTH).with(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public boolean isTransparent(BlockState state) {
        return state.get(TYPE) != VerticalSlabStates.DOUBLE;
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction facing = state.get(HORIZONTAL_FACING);
        switch(state.get(TYPE)) {
            case DOUBLE:
                return VoxelShapes.fullCube();
            case STRAIGHT:
                switch(facing) {
                    case NORTH:
                        return STRAIGHT_N;
                    case SOUTH:
                        return STRAIGHT_S;
                    case WEST:
                        return STRAIGHT_W;
                    case EAST:
                        return STRAIGHT_E;
                }
            case INNER:
                switch(facing) {
                    case SOUTH:
                        return INNER_S;
                    case WEST:
                        return INNER_W;
                    case EAST:
                        return INNER_E;
                    case NORTH:
                        return INNER_N;
                }
            case OUTER:
                switch(facing) {
                    case SOUTH:
                        return VoxelShapes.or(STRAIGHT_S,INNER_S);
                    case WEST:
                        return VoxelShapes.or(STRAIGHT_W,INNER_W);
                    case EAST:
                        return VoxelShapes.or(STRAIGHT_E,INNER_E);
                    case NORTH:
                        return VoxelShapes.or(STRAIGHT_N,INNER_N);
                }
        }
        return VoxelShapes.fullCube();
    }

    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos blockpos = context.getPos();
        BlockState blockstate = context.getWorld().getBlockState(blockpos);
        if (blockstate.matchesBlock(this)) {
            return blockstate.with(TYPE, VerticalSlabStates.DOUBLE).with(WATERLOGGED, Boolean.FALSE);
        } else {
            //blockstate1 = getType(context.getWorld(),context.getPos(),context.getPlacementHorizontalFacing());
            FluidState fluidstate = context.getWorld().getFluidState(blockpos);
            BlockState blockstate1 = this.getDefaultState().with(TYPE, VerticalSlabStates.STRAIGHT).with(WATERLOGGED, fluidstate.getFluid() == Fluids.WATER);
            Direction direction = context.getFace();
            if (direction.getAxis() == Direction.Axis.Y) {
                Vector3d vec = context.getHitVec().subtract(new Vector3d(blockpos.getX(), blockpos.getY(), blockpos.getZ())).subtract(0.5, 0, 0.5);
                double angle = Math.atan2(vec.x, vec.z) * -180.0 / Math.PI;
                blockstate1 = blockstate1.with(HORIZONTAL_FACING, Direction.fromAngle(angle));
            } else {
                blockstate1 = blockstate1.with(HORIZONTAL_FACING, direction.getOpposite());
            }

            return blockstate1;
        }
    }

    @Override
    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
        ItemStack itemstack = useContext.getItem();
        VerticalSlabStates slabtype = state.get(TYPE);
        if (slabtype != VerticalSlabStates.DOUBLE && itemstack.getItem() == this.asItem()) {
            if (useContext.replacingClickedOnBlock()) {
                return useContext.getFace() == state.get(HORIZONTAL_FACING).getOpposite();
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        if (facing != Direction.DOWN && facing != Direction.UP && facing != stateIn.get(HORIZONTAL_FACING).getOpposite()) {
            return stateIn;
        } else {
            return stateIn;
        }
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(HORIZONTAL_FACING, rot.rotate(state.get(HORIZONTAL_FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(HORIZONTAL_FACING)));
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(TYPE, HORIZONTAL_FACING, WATERLOGGED);
    }


    public BlockState getType(World worldIn, BlockPos pos, Direction facing) {

        BlockState forwardBS = worldIn.getBlockState(pos.offset(facing));
        BlockState backwardBS = worldIn.getBlockState(pos.offset(facing.getOpposite()));
        BlockState leftBS = worldIn.getBlockState(pos.offset(facing.rotateYCCW().rotateYCCW().rotateYCCW()));
        BlockState rightBS = worldIn.getBlockState(pos.offset(facing.rotateYCCW()));

        boolean forward = forwardBS.matchesBlock(this);
        boolean backward = backwardBS.matchesBlock(this);
        boolean left = leftBS.matchesBlock(this);
        boolean right = rightBS.matchesBlock(this);

        if (left && right) {
            return this.getDefaultState().with(TYPE, VerticalSlabStates.STRAIGHT).with(HORIZONTAL_FACING, facing);
        } else if (backward && left) {
            return this.getDefaultState().with(TYPE, VerticalSlabStates.OUTER).with(HORIZONTAL_FACING, facing);
        } else if (backward && right) {
            return this.getDefaultState().with(TYPE, VerticalSlabStates.OUTER).with(HORIZONTAL_FACING, facing).mirror(Mirror.LEFT_RIGHT);
        }

/*
        if (forward) {
            if (forwardBS.get(HORIZONTAL_FACING).equals(RDir)) {
                return this.getDefaultState().with(TYPE, VerticalSlabStates.OUTER).with(HORIZONTAL_FACING, facing);
            } else if (forwardBS.get(HORIZONTAL_FACING).equals(LDir)) {
                return this.getDefaultState().with(TYPE, VerticalSlabStates.OUTER).with(HORIZONTAL_FACING, facing.rotateY());
            }
        } else if (backward) {
            if (backwardBS.get(HORIZONTAL_FACING).equals(RDir)) {
                return this.getDefaultState().with(TYPE, VerticalSlabStates.INNER).with(HORIZONTAL_FACING, facing);
            } else if (backwardBS.get(HORIZONTAL_FACING).equals(LDir)) {
                return this.getDefaultState().with(TYPE, VerticalSlabStates.INNER).with(HORIZONTAL_FACING, facing.rotateY());
            }
        } else {
            return this.getDefaultState().with(TYPE, VerticalSlabStates.STRAIGHT).with(HORIZONTAL_FACING, facing);

        }

 */


        return this.getDefaultState().with(TYPE, VerticalSlabStates.STRAIGHT).with(HORIZONTAL_FACING, facing);

    }


}
