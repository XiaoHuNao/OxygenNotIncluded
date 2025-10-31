package com.xiaohunao.oxygen_not_included.common.capability.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.xiaohunao.oxygen_not_included.common.capability.IGasStorage;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ItemStackGasStorage implements IGasStorage {
    private static final String KEY_ID = "GasId";
    private static final String KEY_AMOUNT = "Amount";

    private final ItemStack stack;
    private final int capacity;

    public ItemStackGasStorage(ItemStack stack, int capacity) {
        this.stack = stack;
        this.capacity = Math.max(capacity, 0);
    }

    private CompoundTag tag() {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag t = data.isEmpty() ? new CompoundTag() : data.copyTag();
        return t;
    }

    private void saveTag(CompoundTag t) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    private Gas getStoredGas() {
        String id = tag().getString(KEY_ID);
        if (id.isEmpty()) return null;
        ResourceLocation rl = ResourceLocation.tryParse(id);
        return rl == null ? null : ONIRegistries.GAS.get(rl);
    }

    private void setStoredGas(Gas gas) {
        if (gas == null) {
            CompoundTag t = tag();
            t.remove(KEY_ID);
            saveTag(t);
            return;
        }
        var key = ONIRegistries.GAS.getKey(gas);
        if (key != null) {
            CompoundTag t = tag();
            t.putString(KEY_ID, key.toString());
            saveTag(t);
        }
    }

    private int getStoredAmount() {
        return tag().getInt(KEY_AMOUNT);
    }

    private void setStoredAmount(int amount) {
        CompoundTag t = tag();
        if (amount <= 0) {
            t.remove(KEY_AMOUNT);
            t.remove(KEY_ID);
        } else {
            t.putInt(KEY_AMOUNT, amount);
        }
        saveTag(t);
    }

    @Override
    public int getAmount(Gas gas) {
        Gas s = getStoredGas();
        if (s == null || gas == null || s != gas) return 0;
        return getStoredAmount();
    }

    @Override
    public int getCapacity(Gas gas) {
        Gas s = getStoredGas();
        if (s == null || s == gas) return capacity;
        return 0;
    }

    @Override
    public int insert(Gas gas, int amount, boolean simulate) {
        if (gas == null || amount <= 0) return 0;
        Gas s = getStoredGas();
        int cur = getStoredAmount();
        if (s != null && s != gas) return 0;
        int space = capacity - cur;
        if (space <= 0) return 0;
        int filled = Math.min(space, amount);
        if (!simulate) {
            if (s == null) setStoredGas(gas);
            setStoredAmount(cur + filled);
        }
        return filled;
    }

    @Override
    public int extract(Gas gas, int amount, boolean simulate) {
        if (gas == null || amount <= 0) return 0;
        Gas s = getStoredGas();
        if (s == null || s != gas) return 0;
        int cur = getStoredAmount();
        int took = Math.min(cur, amount);
        if (!simulate) setStoredAmount(cur - took);
        return took;
    }

    @Override
    public Map<Gas, Integer> getAll() {
        Gas s = getStoredGas();
        if (s == null) return Collections.emptyMap();
        Map<Gas, Integer> m = new HashMap<>();
        m.put(s, getStoredAmount());
        return m;
    }

    public CompoundTag saveToTag() {
        CompoundTag t = new CompoundTag();
        Gas s = getStoredGas();
        if (s != null) {
            var key = ONIRegistries.GAS.getKey(s);
            if (key != null) t.putString(KEY_ID, key.toString());
            t.putInt(KEY_AMOUNT, getStoredAmount());
        }
        return t;
    }

    public void loadFromTag(CompoundTag nbt) {
        if (nbt == null) return;
        String id = nbt.getString(KEY_ID);
        int amt = nbt.getInt(KEY_AMOUNT);
        if (id.isEmpty() || amt <= 0) {
            setStoredAmount(0);
            return;
        }
        ResourceLocation rl = ResourceLocation.tryParse(id);
        Gas gas = rl == null ? null : ONIRegistries.GAS.get(rl);
        if (gas == null) {
            setStoredAmount(0);
            return;
        }
        setStoredGas(gas);
        setStoredAmount(Math.min(amt, capacity));
    }
}


