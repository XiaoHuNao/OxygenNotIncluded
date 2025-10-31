package com.xiaohunao.oxygen_not_included.common.item;

import java.util.List;
import java.util.Map;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.block.entity.AirBlockEntity;
import com.xiaohunao.oxygen_not_included.common.capability.IGasStorage;
import com.xiaohunao.oxygen_not_included.common.capability.impl.ItemStackGasStorage;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class GasTankItem extends Item {
    public static final int DEFAULT_CAPACITY = 1000;

    public GasTankItem(Properties properties) {
        super(properties);
    }

    // 简化：不使用 NeoForge 能力，直接用 NBT 存储访问

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        IGasStorage storage = new ItemStackGasStorage(stack, DEFAULT_CAPACITY);
        Map<Gas, Integer> all = storage.getAll();
            if (all.isEmpty()) {
                tooltip.add(Component.translatable(OxygenNotIncluded.asDescriptionId("gas_tank.empty")).withStyle(ChatFormatting.GRAY));
            } else {
                for (Map.Entry<Gas, Integer> e : all.entrySet()) {
                    ResourceLocation id = ONIRegistries.GAS.getKey(e.getKey());
                    String name = id == null ? "unknown" : id.getPath();
                    tooltip.add(Component.literal(name + ": " + e.getValue() + "ml").withStyle(ChatFormatting.AQUA));
                }
            }
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        
        ItemStack stack = ctx.getItemInHand();
        IGasStorage tank = new ItemStackGasStorage(stack, DEFAULT_CAPACITY);

        BlockPos clickedPos = ctx.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        BlockPos targetPos = clickedState.isAir() ? clickedPos : clickedPos.relative(ctx.getClickedFace());

        BlockEntity be = level.getBlockEntity(targetPos);
        if (!(be instanceof AirBlockEntity air)) return InteractionResult.PASS;

        // 1) 如果罐子有气体，优先向空气方块释放
        Map<Gas, Integer> all = tank.getAll();
        if (!all.isEmpty()) {
            Gas gas = all.keySet().iterator().next();
            int amt = all.get(gas);
            int moved = new com.xiaohunao.oxygen_not_included.common.capability.impl.AirBlockGasStorage(air).insert(gas, Math.min(amt, 250), false);
            if (moved > 0) {
                tank.extract(gas, moved, false);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        // 2) 罐子空：从空气方块抽取主导气体
        Gas dominant = null;
        int max = 0;
        for (Map.Entry<Gas, Integer> e : air.getGasMap().entrySet()) {
            if (e.getValue() > max) { max = e.getValue(); dominant = e.getKey(); }
        }
        if (dominant == null || max <= 0) return InteractionResult.PASS;
        int moved = tank.insert(dominant, Math.min(max, 250), false);
        if (moved > 0) {
            air.setGasAmount(dominant, max - moved);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // 右键空气时：对准脚下/眼前空气方块进行释放/吸取
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}


