package com.xiaohunao.oxygen_not_included.common.capability.impl;

import java.util.Map;

import com.xiaohunao.oxygen_not_included.common.block.entity.AirBlockEntity;
import com.xiaohunao.oxygen_not_included.common.capability.IGasStorage;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;

import net.minecraft.nbt.CompoundTag;

public class AirBlockGasStorage implements IGasStorage {
    private final AirBlockEntity air;

    public AirBlockGasStorage(AirBlockEntity air) {
        this.air = air;
    }

    @Override
    public int getAmount(Gas gas) {
        return air.getGasAmount(gas);
    }

    @Override
    public int getCapacity(Gas gas) {
        // 空气方块不设硬上限，这里返回其标准容量，供 UI/逻辑参考
        return com.xiaohunao.oxygen_not_included.common.gas.Gas.STANDARD_GAS_CONCENTRATION;
    }

    @Override
    public int insert(Gas gas, int amount, boolean simulate) {
        if (gas == null || amount <= 0) return 0;
        int cur = air.getGasAmount(gas);
        long next = (long) cur + (long) amount;
        int newVal = (int) Math.min(Integer.MAX_VALUE, next);
        int added = newVal - cur;
        if (!simulate && added > 0) air.setGasAmount(gas, newVal);
        return added;
    }

    @Override
    public int extract(Gas gas, int amount, boolean simulate) {
        if (gas == null || amount <= 0) return 0;
        int cur = air.getGasAmount(gas);
        int took = Math.min(cur, amount);
        if (!simulate && took > 0) air.setGasAmount(gas, cur - took);
        return took;
    }

    @Override
    public Map<Gas, Integer> getAll() {
        return air.getGasMap();
    }

    public CompoundTag saveToTag() { return new CompoundTag(); }
    public void loadFromTag(CompoundTag nbt) {}
}


