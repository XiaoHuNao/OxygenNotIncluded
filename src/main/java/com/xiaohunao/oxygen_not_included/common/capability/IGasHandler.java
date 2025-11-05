package com.xiaohunao.oxygen_not_included.common.capability;

import com.xiaohunao.oxygen_not_included.common.gas.GasStack;

/**
 * 气体处理器接口
 * 类似于 IFluidHandler，用于处理多个气体存储单元
 */
public interface IGasHandler {
    /**
     * 获取气体存储单元的数量
     * @return 存储单元数量
     */
    int getTanks();

    /**
     * 获取指定索引的存储单元
     * @param tank 存储单元索引
     * @return 气体存储单元
     */
    IGasTank getTank(int tank);

    /**
     * 获取指定索引存储单元中的气体堆栈
     * @param tank 存储单元索引
     * @return 气体堆栈
     */
    GasStack getGasInTank(int tank);

    /**
     * 获取指定索引存储单元的容量
     * @param tank 存储单元索引
     * @return 容量（毫升）
     */
    int getTankCapacity(int tank);

    /**
     * 检查指定索引的存储单元是否可以填充指定的气体
     * @param tank 存储单元索引
     * @param stack 要检查的气体堆栈
     * @return 如果可以填充返回 true
     */
    boolean isGasValid(int tank, GasStack stack);

    /**
     * 填充气体到指定的存储单元
     * @param tank 目标存储单元索引
     * @param resource 要填充的气体堆栈
     * @param action 执行模式（SIMULATE 模拟，EXECUTE 执行）
     * @return 实际填充的气体量（毫升）
     */
    int fill(GasStack resource, IGasTank.GasAction action);

    /**
     * 从指定的存储单元抽取气体
     * @param resource 要抽取的气体堆栈（如果为 null，则抽取任意类型）
     * @param action 执行模式（SIMULATE 模拟，EXECUTE 执行）
     * @return 抽取的气体堆栈
     */
    GasStack drain(GasStack resource, IGasTank.GasAction action);

    /**
     * 从指定的存储单元抽取指定量的气体
     * @param maxDrain 最大抽取量（毫升）
     * @param action 执行模式（SIMULATE 模拟，EXECUTE 执行）
     * @return 抽取的气体堆栈
     */
    GasStack drain(int maxDrain, IGasTank.GasAction action);
}

