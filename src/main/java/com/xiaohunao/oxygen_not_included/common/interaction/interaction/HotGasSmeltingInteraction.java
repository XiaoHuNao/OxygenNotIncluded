package com.xiaohunao.oxygen_not_included.common.interaction.interaction;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.oxygen_not_included.common.block.entity.GasBlockEntity;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteraction;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteractionCategory;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteractionContext;
import com.xiaohunao.oxygen_not_included.common.network.ItemEntityONIDataPayload;
import com.xiaohunao.oxygen_not_included.common.network.ONINetwork;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HotGasSmeltingInteraction implements GasInteraction {
    public static final MapCodec<HotGasSmeltingInteraction> CODEC = MapCodec.unit(new HotGasSmeltingInteraction());

    private static final String ONI_DATA_KEY = "ONIData";
    private static final String SMELT_TIME_KEY = "SmeltTime";
    private static final String SMELT_STACK_SIZE_KEY = "SmeltStackSize";
    @Override
    public List<GasInteractionCategory> category() {
        return List.of(GasInteractionCategory.ENTITY);
    }

    @Override
    public boolean matches(GasInteractionContext context) {
        return context.entities().map(entities -> entities.stream().anyMatch(entity -> entity instanceof ItemEntity)).orElse(false);
    }

    @Override
    public void apply(GasInteractionContext context) {
        context.entities().ifPresent(entities -> {
            for (Entity entity : entities) {
                if (entity instanceof ItemEntity itemEntity){
                    Level level = context.level();
                    Optional<RecipeHolder<SmeltingRecipe>> smeltingRecipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(itemEntity.getItem()), level);
                    smeltingRecipe.ifPresent(recipe -> {
                        RegistryAccess registryAccess = level.registryAccess();
                        if (recipe.value().assemble(new SingleRecipeInput(itemEntity.getItem()), registryAccess).isEmpty()) {
                            return;
                        }

                        if (countDown(context.source(), itemEntity) <= 0 && canSmelt(context.source(), itemEntity)) {
                            List<ItemStack> stacks = getOutputItems(context, recipe, itemEntity);
                            if (stacks == null)
                                return;
                            if (stacks.isEmpty()) {
                                clearSmeltData(itemEntity);
                                itemEntity.discard();
                                return;
                            }
                            ItemStack primaryOutput = stacks.removeFirst();
                            itemEntity.setItem(primaryOutput);
                            clearSmeltData(itemEntity);
                            for (ItemStack additional : stacks) {
                                ItemEntity entityIn = new ItemEntity(itemEntity.level(), entity.getX(), entity.getY(), entity.getZ(), additional);
                                entityIn.setDeltaMovement(itemEntity.getDeltaMovement());
                                itemEntity.level().addFreshEntity(entityIn);
                            }
                        }
                    });
                }
            }
        });
    }

    //输出配方
    public List<ItemStack> getOutputItems(GasInteractionContext context, RecipeHolder<SmeltingRecipe> recipeHolder, ItemEntity itemEntity) {
        List<ItemStack> outputItems = new ArrayList<>();
        ItemStack resultItem = recipeHolder.value().getResultItem(context.level().registryAccess()).copy();
        resultItem.setCount(itemEntity.getItem().getCount() * resultItem.getCount());

        while (resultItem.getCount() > resultItem.getMaxStackSize()) {
            outputItems.add(resultItem.split(resultItem.getMaxStackSize()));
        }

        outputItems.add(resultItem);
        return outputItems;

    }

    //计算需要冶炼的时间
    private static double calculateSmeltTime(GasBlockEntity gasBlockEntity, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        int stackSize = itemStack.getCount();
        double kelvin = gasBlockEntity.getKelvin();
        return ((double) (stackSize - 1) / 16 + 1) * Math.max(50, 1000 - Math.log(kelvin) * 100);
    }

    //是否可以被冶炼
    private static boolean canSmelt(GasBlockEntity gasBlockEntity, ItemEntity itemEntity) {
        if (itemEntity.getPersistentData().contains(ONI_DATA_KEY)){
            CompoundTag oniData = itemEntity.getPersistentData().getCompound(ONI_DATA_KEY);
            if (oniData.contains(SMELT_TIME_KEY)){
                return oniData.getInt(SMELT_TIME_KEY) <= 0;
            }
        }
        return calculateSmeltTime(gasBlockEntity, itemEntity) <= 0;
    }

    //倒计时
    public static int countDown(GasBlockEntity gasBlockEntity, ItemEntity itemEntity) {
        CompoundTag persistentData = itemEntity.getPersistentData();
        CompoundTag oniData;
        if (persistentData.contains(ONI_DATA_KEY, Tag.TAG_COMPOUND)) {
            oniData = persistentData.getCompound(ONI_DATA_KEY);
        } else {
            oniData = new CompoundTag();
            persistentData.put(ONI_DATA_KEY, oniData);
        }

        int targetSmeltTime = Math.max(1, (int) Math.round(calculateSmeltTime(gasBlockEntity, itemEntity)));
        int currentStackSize = itemEntity.getItem().getCount();
        int recordedStackSize = oniData.contains(SMELT_STACK_SIZE_KEY, Tag.TAG_INT) ? oniData.getInt(SMELT_STACK_SIZE_KEY) : -1;

        if (!oniData.contains(SMELT_TIME_KEY, Tag.TAG_INT) || recordedStackSize != currentStackSize) {
            oniData.putInt(SMELT_TIME_KEY, targetSmeltTime);
            oniData.putInt(SMELT_STACK_SIZE_KEY, currentStackSize);
        }

        int smeltTime = oniData.getInt(SMELT_TIME_KEY);

        if (targetSmeltTime < smeltTime) {
            smeltTime = targetSmeltTime;
        }

        smeltTime = Math.max(0, smeltTime - 1);
        oniData.putInt(SMELT_TIME_KEY, smeltTime);
        persistentData.put(ONI_DATA_KEY, oniData);

        syncONIData(itemEntity, oniData);
        return smeltTime;
    }

    private static void clearSmeltData(ItemEntity itemEntity) {
        CompoundTag persistentData = itemEntity.getPersistentData();
        if (persistentData.contains(ONI_DATA_KEY)) {
            persistentData.remove(ONI_DATA_KEY);
            syncONIData(itemEntity, null);
        }
    }

    private static void syncONIData(ItemEntity itemEntity, CompoundTag oniData) {
        if (!itemEntity.level().isClientSide()) {
            ONINetwork.sendToTracking(itemEntity, new ItemEntityONIDataPayload(itemEntity.getId(), oniData == null ? null : oniData.copy()));
        }
    }

    @Override
    public MapCodec<? extends GasInteraction> codec() {
        return CODEC;
    }
}
