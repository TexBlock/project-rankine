package com.cannolicatfish.rankine.items.pendants;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class HealthPendantItem extends Item{
    public HealthPendantItem(Properties properties) {
        super(properties);
    }

    int TICKS;
    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entityIn;
            if (player.getHeldItemOffhand().getItem() == this) {
                if (TICKS == 20*60) {
                    ((PlayerEntity) entityIn).addPotionEffect(new EffectInstance(Effects.ABSORPTION, 60 * 20, 4));
                    TICKS = 0;
                }

                TICKS += TICKS;
            }
        }
    }

}