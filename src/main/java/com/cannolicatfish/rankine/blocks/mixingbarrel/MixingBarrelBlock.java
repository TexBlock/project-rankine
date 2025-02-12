package com.cannolicatfish.rankine.blocks.mixingbarrel;

import com.cannolicatfish.rankine.blocks.mixingbarrel.MixingBarrelTile;
import com.cannolicatfish.rankine.init.RankineBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

public class MixingBarrelBlock extends Block {
    //public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static IntegerProperty ANGLE = IntegerProperty.create("angle",0,3);


    public MixingBarrelBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(ANGLE, 1));
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ANGLE);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        //return VoxelShapes.combineAndSimplify(makeCuboidShape(0,0,0,16,2,16),makeCuboidShape(2,2,2,14,16,14), IBooleanFunction.OR);
        return VoxelShapes.fullCube();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new MixingBarrelTile();
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            LazyOptional<IFluidHandlerItem> item = FluidUtil.getFluidHandler(player.getHeldItem(handIn));
            MixingBarrelTile mixingBarrelTile = (MixingBarrelTile) worldIn.getTileEntity(pos);
            if (item.isPresent() && mixingBarrelTile != null) {
                FluidActionResult emptyContainerAndStow = FluidUtil.tryEmptyContainerAndStow(player.getHeldItem(handIn), mixingBarrelTile.inputTank, new InvWrapper(player.inventory), mixingBarrelTile.inputTank.getCapacity(), player, true);
                if (emptyContainerAndStow.isSuccess()) {
                    player.setHeldItem(handIn, emptyContainerAndStow.getResult());
                    worldIn.markChunkDirty(pos, mixingBarrelTile);
                    return ActionResultType.CONSUME;
                }

                FluidActionResult fillContainerAndStowInput = FluidUtil.tryFillContainerAndStow(player.getHeldItem(handIn), mixingBarrelTile.inputTank, new InvWrapper(player.inventory), mixingBarrelTile.inputTank.getFluidAmount(), player, true);
                if (fillContainerAndStowInput.isSuccess()) {
                    player.setHeldItem(handIn, fillContainerAndStowInput.getResult());
                    worldIn.markChunkDirty(pos, mixingBarrelTile);
                    return ActionResultType.CONSUME;
                }
            }
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof INamedContainerProvider) {
                    if (player.isCrouching()) {
                        CompoundNBT nbt = tileEntity.getTileData();
                        /*int mixTime = nbt.getInt("MixTime");
                        int mixTimeTotal = nbt.getInt("MixTimeTotal");
                        System.out.println(mixTime + "/" + mixTimeTotal);
                        nbt.putInt("MixTime",Math.min(mixTime+8,mixTimeTotal));
                        nbt.putInt("RedstonePower",8);*/
                        int angle = worldIn.getBlockState(pos).get(MixingBarrelBlock.ANGLE);
                        if (angle == 3) {
                            worldIn.setBlockState(pos, RankineBlocks.MIXING_BARREL.get().getDefaultState().with(MixingBarrelBlock.ANGLE,0),3);
                        } else {
                            worldIn.setBlockState(pos, RankineBlocks.MIXING_BARREL.get().getDefaultState().with(MixingBarrelBlock.ANGLE,angle+1),3);
                        }
                    } else {
                        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, tileEntity.getPos());
                    }
                } else {
                    throw new IllegalStateException("Our named container provider is missing!");
                }
                return ActionResultType.CONSUME;
            } else {
                return ActionResultType.SUCCESS;
        }
    }

    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.matchesBlock(newState.getBlock())) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof MixingBarrelTile) {
                InventoryHelper.dropInventoryItems(worldIn, pos, (MixingBarrelTile)tileentity);
                worldIn.updateComparatorOutputLevel(pos, this);
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }


}
