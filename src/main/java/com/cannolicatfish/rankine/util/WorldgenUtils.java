package com.cannolicatfish.rankine.util;

import com.cannolicatfish.rankine.init.RankineTags;
import com.cannolicatfish.rankine.init.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WorldgenUtils {

    public static List<ResourceLocation> GEN_BIOMES = new ArrayList<>();
    public static List<Block> O1 = new ArrayList<>();
    public static List<Block> A1 = new ArrayList<>();
    public static List<Block> B1 = new ArrayList<>();
    public static List<Block> O2 = new ArrayList<>();
    public static List<Block> A2 = new ArrayList<>();
    public static List<Block> B2 = new ArrayList<>();
    public static List<List<String>> INTRUSION_LISTS = new ArrayList<>();
    public static List<List<Block>> INTRUSION_BLOCKS = new ArrayList<>();
    public static List<List<Float>> INTRUSION_WEIGHTS = new ArrayList<>();
    public static List<List<Block>> INTRUSION_ORES = new ArrayList<>();
    public static List<List<Float>> INTRUSION_ORE_CHANCES = new ArrayList<>();
    public static List<WeightedCollection<BlockState>> INTRUSION_COLLECTIONS = new ArrayList<>();
    public static List<List<String>> LAYER_LISTS = new ArrayList<>();
    public static List<List<String>> VEGETATION_LISTS = new ArrayList<>();
    public static List<WeightedCollection<BlockState>> VEGETATION_COLLECTIONS = new ArrayList<>();
    public static List<Block> GRAVELS = new ArrayList<>();
    public static List<Block> ORE_STONES = new ArrayList<>();
    public static List<String> ORE_TEXTURES = new ArrayList<>();




    public static void initOreTextures() {
        for (String ORE : Config.MISC.ORE_STONES.get()) {
            String[] ores = ORE.split("\\|");
            if (ores.length > 1) {
                ORE_TEXTURES.add(ores[1]);
            } else {
                ORE_TEXTURES.add(ores[0]);
            }

        }

    }
    public static void initConfigs() {

        for (String ORE : Config.MISC.ORE_STONES.get()) {
            ORE_STONES.add(ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(ORE.split("\\|")[0])));
        }

        for (List<Object> L : Config.BIOME_GEN.BIOME_SETTINGS.get()) {
            String biomeToAdd = (String) L.get(0);
            List<String> biomeName = Arrays.asList(biomeToAdd.split(":"));
            if (biomeName.size() > 1) {
                Block gravel = ResourceLocation.tryCreate((String) L.get(5)) == null ? Blocks.AIR : ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate((String) L.get(5)));
                populateLists(ResourceLocation.tryCreate(biomeToAdd),(List<String>) L.get(1),(List<String>) L.get(2),(List<String>) L.get(3),(List<String>) L.get(4), gravel);
            } else {
                for (ResourceLocation RS : getBiomeNamesFromCategory(Collections.singletonList(Biome.Category.byName(biomeToAdd)), true)) {
                    Block gravel = ResourceLocation.tryCreate((String) L.get(5)) == null ? Blocks.AIR : ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate((String) L.get(5)));
                    populateLists(RS,(List<String>) L.get(1),(List<String>) L.get(2),(List<String>) L.get(3),(List<String>) L.get(4), gravel);
                }
            }

        }

        for (List<String> I : INTRUSION_LISTS) {
            int ind = 0;
            WeightedCollection<BlockState> col = new WeightedCollection<>();
            List<Block> tempIB = new ArrayList<>();
            List<Float> tempIW = new ArrayList<>();
            List<Block> tempIO = new ArrayList<>();
            List<Float> tempIC = new ArrayList<>();
            for (String entry : I) {
                tempIB.add(ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(entry.split("\\|")[0])));
                tempIW.add(Float.parseFloat(entry.split("\\|")[1]));
                tempIO.add(ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(entry.split("\\|")[2])));
                tempIC.add(Float.parseFloat(entry.split("\\|")[3]));
                col.add(tempIW.get(ind),tempIB.get(ind).getDefaultState());
                ind += 1;
            }
            INTRUSION_BLOCKS.add(tempIB);
            INTRUSION_WEIGHTS.add(tempIW);
            INTRUSION_ORES.add(tempIO);
            INTRUSION_ORE_CHANCES.add(tempIC);
            INTRUSION_COLLECTIONS.add(col);
        }

        for (List<String> V : VEGETATION_LISTS) {
            int ind = 0;
            WeightedCollection<BlockState> col = new WeightedCollection<>();
            List<Block> tempVB = new ArrayList<>();
            List<Float> tempVW = new ArrayList<>();
            for (String entry : V) {
                tempVB.add(ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(entry.split("\\|")[0])));
                tempVW.add(Float.parseFloat(entry.split("\\|")[1]));
                col.add(tempVW.get(ind),tempVB.get(ind).getDefaultState());
                ind += 1;
            }
            VEGETATION_COLLECTIONS.add(col);
        }

    }

    private static void populateLists(ResourceLocation BIOME, List<String> SOILS, List<String> INTRUSIONS, List<String> STONES, List<String> VEGETATION, Block GRAVEL) {
        GEN_BIOMES.add(BIOME);
        if (SOILS.isEmpty()) {
            O1.add(Blocks.AIR);
            A1.add(Blocks.AIR);
            B1.add(Blocks.AIR);
            O2.add(Blocks.AIR);
            A2.add(Blocks.AIR);
            B2.add(Blocks.AIR);
        } else {
            O1.add(ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(SOILS.get(0))));
            A1.add(ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(SOILS.get(1))));
            B1.add(ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(SOILS.get(2))));
            O2.add(ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(SOILS.get(3))));
            A2.add(ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(SOILS.get(4))));
            B2.add(ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryCreate(SOILS.get(5))));
        }
        INTRUSION_LISTS.add(INTRUSIONS);
        LAYER_LISTS.add(STONES);
        VEGETATION_LISTS.add(VEGETATION);
        GRAVELS.add(GRAVEL);

    }


    public static List<ResourceLocation> getBiomeNamesFromCategory(List<Biome.Category> biomeCats, boolean include) {
        List<ResourceLocation> b = new ArrayList<>();
        for (Biome biome : ForgeRegistries.BIOMES) {
            if (!biomeCats.isEmpty()) {
                for (Biome.Category cat : biomeCats) {
                    if (biome.getCategory() == cat && include){
                        b.add(biome.getRegistryName());
                    }
                    if (!include && biome.getCategory() != cat && biome.getCategory() != Biome.Category.NETHER && biome.getCategory() != Biome.Category.THEEND) {
                        b.add(biome.getRegistryName());
                    }
                }
            }
            else if (!include && biome.getCategory() != Biome.Category.NETHER && biome.getCategory() != Biome.Category.THEEND) {
                b.add(biome.getRegistryName());
            }
        }
        return b;
    }

    public static boolean isWet(ISeedReader reader, BlockPos pos) {
        for(BlockPos POS : BlockPos.getAllInBoxMutable(pos.add(-2,0,-2),pos.add(2,2,2))) {
            FluidState fluidstate = reader.getFluidState(POS);
            if (fluidstate.isTagged(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    public static int waterTableHeight(World worldIn, BlockPos pos) {
        Biome biome = worldIn.getBiome(pos);
        int surface = worldIn.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,pos.getX(),pos.getZ());

        return (int) (worldIn.getSeaLevel()- surface*0.3 + biome.getDepth()*30 + biome.getDownfall()*10);
    }

    public static boolean inArea(BlockPos b, double radius, BlockPos... targets) {
        for (BlockPos target : targets) {
            if (b.distanceSq(target) < Math.pow(radius,2)) {
                return true;
            }
        }
        return false;
    }


    public static Block getCeillingBlock(World worldIn, BlockPos pos, int height) {
        for (int i = 1; i<= height; ++i) {
            if (!worldIn.isAirBlock(pos.up(i)) && !(worldIn.getBlockState(pos.up(i)).getBlock() instanceof BushBlock)) {
                return worldIn.getBlockState(pos.up(height)).getBlock();
            }
        }
        return Blocks.AIR;
    }

}
