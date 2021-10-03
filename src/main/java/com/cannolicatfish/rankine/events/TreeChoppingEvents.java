package com.cannolicatfish.rankine.events;

import com.cannolicatfish.rankine.init.Config;
import com.cannolicatfish.rankine.init.RankineBlocks;
import com.cannolicatfish.rankine.init.RankineTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;


@Mod.EventBusSubscriber
public class TreeChoppingEvents {

    @SubscribeEvent
    public static void treeChop(PlayerEvent.BreakSpeed event) {
        BlockPos pos = event.getPos();
        PlayerEntity player = event.getPlayer();
        World worldIn = player.world;
        BlockState state = event.getState();

        if (Config.GENERAL.TREE_CHOPPING.get() && !player.isCreative() && !player.isSneaking() && player.getHeldItemMainhand().getItem().getTags().contains(new ResourceLocation("rankine:tree_choppers")) && state.isIn(RankineTags.Blocks.TREE_LOGS)) {
            Set<BlockPos> checkedBlocks = new HashSet<>();
            Stack<BlockPos> toCheck = new Stack<>();
            boolean alive = false;

            toCheck.add(pos);
            while (!toCheck.isEmpty()) {
                BlockPos cp = toCheck.pop();
                if (!checkedBlocks.contains(cp)) {
                    checkedBlocks.add(cp);
                    for (BlockPos b : BlockPos.getAllInBoxMutable(cp.add(-1,-1,-1), cp.add(1,1,1))) {
                        BlockState target = worldIn.getBlockState(b.toImmutable());
                        if (worldIn.getBlockState(cp).isIn(RankineTags.Blocks.TREE_LOGS) && target.isIn(RankineTags.Blocks.TREE_LOGS)) {
                            toCheck.add(b.toImmutable());
                        } else if (target.isIn(RankineTags.Blocks.TREE_LEAVES)) {
                            if (target.getBlock() instanceof LeavesBlock) {
                                if (!target.get(LeavesBlock.PERSISTENT)) { /*&& target.get(LeavesBlock.DISTANCE) <= 5*/
                                    alive = true;
                                }
                            } else {
                                alive = true;
                            }
                        }
                    }
                }
            }
            if (alive) event.setNewSpeed((event.getNewSpeed() / checkedBlocks.size()) * Config.GENERAL.TREE_CHOP_SPEED.get().floatValue());
        }

    }


    @SubscribeEvent
    public static void treeChop(BlockEvent.BreakEvent event) {
        ServerWorld worldIn = (ServerWorld) event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = worldIn.getBlockState(pos);
        PlayerEntity player = event.getPlayer();

        if (Config.GENERAL.TREE_CHOPPING.get() && !player.isCreative() && !player.isSneaking() && !worldIn.isRemote && player.getHeldItemMainhand().getItem().isIn(RankineTags.Items.TREE_CHOPPERS) && state.isIn(RankineTags.Blocks.TREE_LOGS)) {
            Set<BlockPos> checkedBlocks = new HashSet<>();
            Set<BlockPos> logs = new HashSet<>();
            List<BlockPos> leaves = new ArrayList<>();
            Stack<BlockPos> toCheck = new Stack<>();
            boolean alive = false;

            toCheck.add(pos);
            while (!toCheck.isEmpty()) {
                BlockPos cp = toCheck.pop();
                if (!checkedBlocks.contains(cp)) {
                    checkedBlocks.add(cp);
                    for (BlockPos b : BlockPos.getAllInBoxMutable(cp.add(-1,-1,-1), cp.add(1,1,1))) {
                        BlockState target = worldIn.getBlockState(b.toImmutable());
                        if (worldIn.getBlockState(cp).isIn(RankineTags.Blocks.TREE_LOGS) && target.isIn(RankineTags.Blocks.TREE_LOGS)) {
                            toCheck.add(b.toImmutable());
                            logs.add(b.toImmutable());
                        } else if (target.isIn(RankineTags.Blocks.TREE_LEAVES)) {
                            if (target.getBlock() instanceof LeavesBlock) {
                                if (!target.get(LeavesBlock.PERSISTENT) /*&& target.get(LeavesBlock.DISTANCE) <= 5*/) {
                                    for (BlockPos log : logs) {
                                        if (log.distanceSq(b) <= 10) {
                                            toCheck.add(b.toImmutable());
                                            leaves.add(b.toImmutable());
                                            alive = true;
                                        }
                                    }
                                }
                            } else {
                                for (BlockPos log : logs) {
                                    if (log.distanceSq(b) <= 10) {
                                        toCheck.add(b.toImmutable());
                                        leaves.add(b.toImmutable());
                                        alive = true;
                                    }
                                }
                            }
                        }
                    }
                    if (logs.size() > Config.GENERAL.MAX_TREE.get() || logs.size() > player.getHeldItemMainhand().getMaxDamage() - player.getHeldItemMainhand().getDamage()) {
                        break;
                    }
                }
            }

            if (alive) {
                if (worldIn.getBlockState(pos.down()).getBlock().isIn(Tags.Blocks.DIRT)) {
                    worldIn.setBlockState(pos, RankineBlocks.STUMP.get().getDefaultState(),3);
                }
                for (BlockPos b : logs) {
                    if (worldIn.getBlockState(b.down()).getBlock().isIn(Tags.Blocks.DIRT)) {
                        worldIn.setBlockState(b, RankineBlocks.STUMP.get().getDefaultState(),3);
                    } else {
                        worldIn.destroyBlock(b, true);
                    }

                }
                for (BlockPos b : leaves) {
                    worldIn.destroyBlock(b, true);
                    //worldIn.getBlockState(b).randomTick(worldIn, b, worldIn.getRandom());
                    //worldIn.getPendingBlockTicks().scheduleTick(b,worldIn.getBlockState(b).getBlock(),1);
                }
                //worldIn.playSound(player, pos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);

            }

            if (state.getBlockHardness(worldIn, pos) != 0.0F) {
                player.getHeldItemMainhand().damageItem(logs.size()-1, player, (p_220038_0_) -> {
                    p_220038_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);
                });
            }

        }
    }


/*
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World worldIn = event.world;

        if (!todo.isEmpty() && !worldIn.isRemote) {
            int toBreak = Math.min(leaves.size()-1, Config.GENERAL.LEAF_DECAY_SPEED.get());
            //worldIn.playSound(worldIn.getClosestPlayer(leaves.get(0).getX(),leaves.get(0).getY(),leaves.get(0).getZ(),32.0D,true), leaves.get(0), SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            int i = 0;
            while (i < toBreak) {
                BlockPos p = leaves.get(0);
                worldIn.removeBlock(p, false);
                if (worldIn.getGameRules().getBoolean(GameRules.DO_TILE_DROPS) && !worldIn.restoringBlockSnapshots) {
                    for (ItemStack items : Block.getDrops(worldIn.getBlockState(p), (ServerWorld) worldIn, p, null)) {
                        spawnAsEntity(worldIn, p, new ItemStack(items.getItem(), 1));
                    }
                }
                leaves.remove(p);
                ++i;
            }

        }


    }



 */







}
