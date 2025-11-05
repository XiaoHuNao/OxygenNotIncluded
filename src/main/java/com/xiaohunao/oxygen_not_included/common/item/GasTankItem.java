package com.xiaohunao.oxygen_not_included.common.item;

import com.xiaohunao.oxygen_not_included.common.component.GasTankData;
import com.xiaohunao.oxygen_not_included.common.init.ONIComponents;
import com.xiaohunao.oxygen_not_included.common.capability.IGasHandlerItem;
import com.xiaohunao.oxygen_not_included.common.capability.IGasTank;
import com.xiaohunao.oxygen_not_included.common.capability.SingleTank;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasStack;
import com.xiaohunao.oxygen_not_included.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * 只能存储一种气体的物品气罐。
 * - 使用 NBT 存储：GasId（字符串，命名空间:id）与 Amount（int，毫升）
 * - 提供 IGasHandlerItem 能力，包含一个单槽 IGasTank
 * - 右键方块尝试将一格标准量气体放置为气体方块
 */
public class GasTankItem extends Item {
    private final int capacity;

    public GasTankItem(Properties properties, int capacity) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        // 暂无空手使用逻辑，沿用默认
        return super.use(level, player, hand);
    }

    @Override
    public @NotNull net.minecraft.world.InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) {
            return net.minecraft.world.InteractionResult.PASS;
        }

        GasStack contained = getGas(stack);
        if (contained.isEmpty() || contained.getAmount() < GasStack.STANDARD_PRESSURE) {
            return net.minecraft.world.InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(pos);
        BlockPos placeAt = state.canBeReplaced() ? pos : pos.relative(context.getClickedFace());

        boolean placed = WorldUtils.tryPlaceContainedGas(level, placeAt, contained.getGas(), player);
        if (placed) {
            drain(stack, GasStack.STANDARD_PRESSURE, true);
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    public static Gas getStoredGas(ItemStack stack) {
        GasTankData data = stack.get(ONIComponents.GAS_TANK.get());
        if (data == null || data.isEmpty()) return null;
        return data.getGas();
    }

    public static int getStoredAmount(ItemStack stack) {
        GasTankData data = stack.get(ONIComponents.GAS_TANK.get());
        return data == null ? 0 : data.getAmount();
    }

    public static void setStored(ItemStack stack, Gas gas, int amount) {
        if (gas == null || amount <= 0) {
            stack.remove(ONIComponents.GAS_TANK.get());
        } else {
            GasTankData data = stack.get(ONIComponents.GAS_TANK.get());
            if (data == null) data = new GasTankData();
            data.setGas(gas);
            data.setAmount(amount);
            stack.set(ONIComponents.GAS_TANK.get(), data);
        }
    }

    public static GasStack getGas(ItemStack stack) {
        Gas gas = getStoredGas(stack);
        int amt = getStoredAmount(stack);
        return gas == null || amt <= 0 ? GasStack.EMPTY : new GasStack(gas, amt);
    }

    // --- 能力实现 ---
    public static class GasHandler implements IGasHandlerItem {
        private final ItemStack container;

        public GasHandler(ItemStack container) {
            this.container = container;
        }

        @Override
        public ItemStack getContainer() {
            return container;
        }

        private GasTankItem asItem() {
            return (GasTankItem) container.getItem();
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public IGasTank getTank(int tank) {
            return new SingleTank(container, asItem().getCapacity());
        }

        @Override
        public GasStack getGasInTank(int tank) {
            return GasTankItem.getGas(container).copy();
        }

        @Override
        public int getTankCapacity(int tank) {
            return asItem().getCapacity();
        }

        @Override
        public boolean isGasValid(int tank, GasStack stack) {
            return new SingleTank(container, asItem().getCapacity()).isGasValid(stack);
        }

        @Override
        public int fill(GasStack resource, IGasTank.GasAction action) {
            return new SingleTank(container, asItem().getCapacity()).fill(resource, action);
        }

        @Override
        public GasStack drain(GasStack resource, IGasTank.GasAction action) {
            if (resource == null || resource.isEmpty()) return GasStack.EMPTY;
            GasStack current = GasTankItem.getGas(container);
            if (current.isEmpty() || current.getGas() != resource.getGas()) return GasStack.EMPTY;
            int toDrain = Math.min(resource.getAmount(), current.getAmount());
            return new SingleTank(container, asItem().getCapacity()).drain(toDrain, action);
        }

        @Override
        public GasStack drain(int maxDrain, IGasTank.GasAction action) {
            return new SingleTank(container, asItem().getCapacity()).drain(maxDrain, action);
        }
    }



    private static void drain(ItemStack stack, int amount, boolean execute) {
        GasStack current = getGas(stack);
        if (current.isEmpty() || amount <= 0) return;
        int toDrain = Math.min(amount, current.getAmount());
        if (execute && toDrain > 0) {
            int remain = current.getAmount() - toDrain;
            setStored(stack, remain > 0 ? current.getGas() : null, remain);
        }
    }
}


