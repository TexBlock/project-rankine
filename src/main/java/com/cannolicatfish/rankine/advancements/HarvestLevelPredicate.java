package com.cannolicatfish.rankine.advancements;

import com.cannolicatfish.rankine.items.alloys.IAlloyTool;
import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;

public class HarvestLevelPredicate extends ItemPredicate {

    int level;
    Item item;

    public HarvestLevelPredicate(int level, Item itemIn) {
        this.level = level;
        this.item = itemIn;
    }

    public HarvestLevelPredicate(JsonObject jsonObject) {
        this(JSONUtils.getInt(jsonObject, "level"),JSONUtils.getItem(jsonObject,"item"));
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack.getItem() instanceof IAlloyTool && stack.getItem() == item) {
            return ((IAlloyTool) stack.getItem()).getAlloyHarvestLevel(stack) == level;
        }
        return false;
    }
}
