package com.xiaohunao.oxygen_not_included.common.capability;

import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasStack;
import com.xiaohunao.oxygen_not_included.common.item.GasTankItem;
import net.minecraft.world.item.ItemStack;

public  class SingleTank implements IGasTank {
    private final ItemStack stack;
    private final int capacity;

    public SingleTank(ItemStack stack, int capacity) {
        this.stack = stack;
        this.capacity = capacity;
    }

    @Override
    public GasStack getGas() {
        return GasTankItem.getGas(stack).copy();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int getGasAmount() {
        return GasTankItem.getStoredAmount(stack);
    }

    @Override
    public boolean isEmpty() {
        return GasTankItem.getGas(stack).isEmpty();
    }

    @Override
    public int fill(GasStack resource, GasAction action) {
        if (resource == null || resource.isEmpty()) return 0;
        GasStack current = GasTankItem.getGas(stack);
        // 只能储存一种气体：若已有气体，则必须同种；若为空，接受任意气体
        if (!current.isEmpty() && current.getGas() != resource.getGas()) {
            return 0;
        }
        int space = capacity - current.getAmount();
        if (space <= 0) return 0;
        int fill = Math.min(space, resource.getAmount());
        if (action == GasAction.EXECUTE && fill > 0) {
            int newAmount = current.getAmount() + fill;
            GasTankItem.setStored(stack, resource.getGas(), newAmount);
        }
        return fill;
    }

    @Override
    public GasStack drain(int maxDrain, GasAction action) {
        if (maxDrain <= 0) return GasStack.EMPTY;
        GasStack current = GasTankItem.getGas(stack);
        if (current.isEmpty()) return GasStack.EMPTY;
        int drained = Math.min(maxDrain, current.getAmount());
        GasStack result = current.copyWithAmount(drained);
        if (action == GasAction.EXECUTE) {
            int remain = current.getAmount() - drained;
            GasTankItem.setStored(stack, remain > 0 ? current.getGas() : null, remain);
        }
        return result;
    }

    @Override
    public GasStack drain(GasStack resource, GasAction action) {
        if (resource == null || resource.isEmpty()) return GasStack.EMPTY;
        GasStack current = GasTankItem.getGas(stack);
        if (current.isEmpty() || current.getGas() != resource.getGas()) return GasStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Override
    public boolean isGasValid(Gas gas) {
        GasStack current = GasTankItem.getGas(stack);
        return current.isEmpty() || current.getGas() == gas;
    }

    @Override
    public boolean isGasValid(GasStack s) {
        return s != null && !s.isEmpty() && isGasValid(s.getGas());
    }
}
