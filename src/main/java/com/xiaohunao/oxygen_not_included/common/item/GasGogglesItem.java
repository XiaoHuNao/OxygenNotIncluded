package com.xiaohunao.oxygen_not_included.common.item;

import com.xiaohunao.oxygen_not_included.common.component.GasGogglesRenderModeData;
import com.xiaohunao.oxygen_not_included.common.init.ONIComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 气相护目镜
 * 可穿戴到头部的装备，当装备时会在气体方块中心显示含量数值，方便调试
 */
public class GasGogglesItem extends ArmorItem {
    public GasGogglesItem(Holder<ArmorMaterial> material, Item.Properties properties) {
        super(material, Type.HELMET, properties);
    }

    public static GasGogglesRenderMode getRenderMode(ItemStack stack) {
        GasGogglesRenderModeData data = stack.getOrDefault(ONIComponents.GAS_GOGGLES_MODE.get(), GasGogglesRenderModeData.DEFAULT);
        return data.mode();
    }

    public static GasGogglesRenderModeData getRenderModeData(ItemStack stack) {
        return stack.getOrDefault(ONIComponents.GAS_GOGGLES_MODE.get(), GasGogglesRenderModeData.DEFAULT);
    }

    public static void setRenderMode(ItemStack stack, GasGogglesRenderMode mode) {
        stack.set(ONIComponents.GAS_GOGGLES_MODE.get(), new GasGogglesRenderModeData(mode));
    }

    public static void cycleRenderMode(ItemStack stack, int step) {
        GasGogglesRenderMode current = getRenderMode(stack);
        setRenderMode(stack, current.cycle(step));
    }
}

