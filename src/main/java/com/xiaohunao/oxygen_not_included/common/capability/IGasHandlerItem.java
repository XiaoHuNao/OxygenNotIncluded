package com.xiaohunao.oxygen_not_included.common.capability;

import net.minecraft.world.item.ItemStack;

/**
 * 物品气体处理器接口
 * 类似于 IFluidHandlerItem，用于处理包含气体的物品
 */
public interface IGasHandlerItem extends IGasHandler {
    /**
     * 获取包含此气体处理器的物品堆栈
     * @return 物品堆栈
     */
    ItemStack getContainer();
}

