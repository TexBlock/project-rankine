package com.cannolicatfish.rankine.items.alloys;

import com.cannolicatfish.rankine.init.Config;
import com.cannolicatfish.rankine.init.RankineEnchantments;
import com.cannolicatfish.rankine.recipe.AlloyingRecipe;
import com.cannolicatfish.rankine.recipe.ElementRecipe;
import com.cannolicatfish.rankine.util.alloys.AlloyEnchantmentUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public interface IAlloyShield extends IAlloyItem {
    @Override
    default void createAlloyNBT(ItemStack stack, World worldIn, String composition, @Nullable ResourceLocation alloyRecipe, @Nullable String nameOverride) {
        if (stack.getTag() != null && stack.getTag().getBoolean("RegenerateAlloy")) {
            stack.getTag().remove("RegenerateAlloy");
        }
        ListNBT alloyData = IAlloyItem.getAlloyNBT(stack);
        List<ElementRecipe> elements = this.getElementRecipes(composition,worldIn);
        List<Integer> percents = this.getPercents(composition);

        CompoundNBT listnbt = new CompoundNBT();
        int dur = 0;
        int ench = 0;
        float cr = 0;
        float hr = 0;
        float tough = 0;

        for (int i = 0; i < elements.size(); i++) {
            ElementRecipe element = elements.get(i);
            int percentage = percents.get(i);

            dur += element.getDurability(percentage);
            ench += element.getEnchantability(percentage);
            cr += element.getCorrosionResistance(percentage);
            hr += element.getHeatResistance(percentage);
            tough += element.getToughness(percentage);
        }



        if (alloyRecipe != null) {
            Optional<? extends IRecipe<?>> opt = worldIn.getRecipeManager().getRecipe(alloyRecipe);
            if (opt.isPresent()) {
                AlloyingRecipe recipe = (AlloyingRecipe) opt.get();
                dur += recipe.getBonusDurability();
                ench += recipe.getBonusEnchantability();
                cr += recipe.getBonusCorrosionResistance();
                hr += recipe.getBonusHeatResistance();
                tough += recipe.getBonusToughness();
            }
        }

        dur = Math.max(1,dur);
        ench = Math.max(0,ench);
        cr = Math.min(Math.max(0,cr),1);
        hr = Math.min(Math.max(0,hr),1);
        tough = Math.min(Math.max(-1,tough),1);
        listnbt.putString("comp",composition);
        if (alloyRecipe != null) {
            listnbt.putString("recipe",alloyRecipe.toString());
        }
        listnbt.putInt("durability",dur);
        listnbt.putInt("enchantability",ench);
        listnbt.putFloat("corrResist",Math.round(cr*100)/100f);
        listnbt.putFloat("heatResist",Math.round(hr*100)/100f);
        listnbt.putFloat("toughness",Math.round(tough*100)/100f);
        alloyData.add(listnbt);
        stack.getOrCreateTag().put("StoredAlloy", listnbt);

        if (nameOverride != null && stack.getTag() != null) {
            stack.getTag().putString("nameOverride",nameOverride);
        }
    }

    default void applyAlloyEnchantments(ItemStack stack, World worldIn) {
        int start = 10;
        int interval = 5;
        int maxLvl = 5;
        ResourceLocation rs = IAlloyItem.getAlloyRecipe(stack);
        if (rs != null && worldIn.getRecipeManager().getRecipe(rs).isPresent()) {
            AlloyingRecipe recipe = (AlloyingRecipe) worldIn.getRecipeManager().getRecipe(rs).get();
            start = recipe.getMinEnchantability();
            interval = recipe.getEnchantInterval();
            maxLvl = recipe.getMaxEnchantLevelIn();
            for (Enchantment e: AlloyEnchantmentUtils.getAlloyEnchantments(recipe,stack,worldIn))
            {
                int enchLvl = Math.min(Math.floorDiv(Math.max(getAlloyEnchantability(stack) - start + interval,0),interval),maxLvl);
                if (enchLvl > 0 && EnchantmentHelper.getEnchantmentLevel(e,stack) == 0) {
                    stack.addEnchantment(e,Math.min(e.getMaxLevel(),enchLvl));
                }
            }
        }
        for (Enchantment e: AlloyEnchantmentUtils.getElementEnchantments(getElementRecipes(IAlloyItem.getAlloyComposition(stack),worldIn),getPercents(IAlloyItem.getAlloyComposition(stack)),stack,worldIn))
        {
            int enchLvl = Math.min(Math.floorDiv(Math.max(getAlloyEnchantability(stack) - start + interval,0),interval),maxLvl);
            if (enchLvl > 0 && EnchantmentHelper.getEnchantmentLevel(e,stack) == 0) {
                stack.addEnchantment(e,Math.min(e.getMaxLevel(),enchLvl));
            }
        }

    }

    default int getAlloyDurability(ItemStack stack)
    {
        if (stack.getTag() != null) {
            return stack.getTag().getCompound("StoredAlloy").getInt("durability");
        } else {
            return 1;
        }

    }

    default int getAlloyEnchantability(ItemStack stack) {
        if (stack.getTag() != null) {
            return stack.getTag().getCompound("StoredAlloy").getInt("enchantability");
        } else {
            return 1;
        }
    }

    default float getCorrResist(ItemStack stack)
    {
        if (!Config.ALLOYS.ALLOY_CORROSION.get())
        {
            return 100;
        }
        if (stack.getTag() != null) {
            return stack.getTag().getCompound("StoredAlloy").getFloat("corrResist");
        } else {
            return 0;
        }

    }


    default float getHeatResist(ItemStack stack)
    {
        if (!Config.ALLOYS.ALLOY_HEAT.get())
        {
            return 100;
        }
        if (stack.getTag() != null) {
            return stack.getTag().getCompound("StoredAlloy").getFloat("heatResist");
        } else {
            return 0;
        }
    }

    default float getToughness(ItemStack stack)
    {
        if (!Config.ALLOYS.ALLOY_TOUGHNESS.get())
        {
            return 0;
        }
        if (stack.getTag() != null) {
            return stack.getTag().getCompound("StoredAlloy").getFloat("toughness");
        } else {
            return 0;
        }
    }

    default int calcDurabilityLoss(ItemStack stack, World worldIn, LivingEntity entityLiving, boolean isEfficient)
    {
        boolean memory = false;
        Random rand = worldIn.getRandom();
        int i = 1;
        if (rand.nextFloat() > getHeatResist(stack) && (entityLiving.isInLava() || entityLiving.getFireTimer() > 0 || worldIn.getDimensionKey() == World.THE_NETHER)) {
            i += Config.ALLOYS.ALLOY_HEAT_AMT.get();
            memory = true;
        }
        if ((rand.nextFloat() > getCorrResist(stack) && entityLiving.isWet()))
        {
            i += Config.ALLOYS.ALLOY_CORROSION_AMT.get();
        }
        if (!isEfficient)
        {
            i *= 2;
        }

        if (memory && EnchantmentHelper.getEnchantmentLevel(RankineEnchantments.SHAPE_MEMORY,stack) >= 1) {
            stack.setDamage(Math.max(stack.getDamage() - i,0));
            i = 0;
        }
        return i;
    }
}
