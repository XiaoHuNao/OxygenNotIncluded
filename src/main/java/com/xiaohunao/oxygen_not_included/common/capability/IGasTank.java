package com.xiaohunao.oxygen_not_included.common.capability;

import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasStack;

/**
 * 气体存储单元接口
 * 类似于 IFluidTank，用于表示单个气体存储单元
 */
public interface IGasTank {
    /**
     * 获取当前存储的气体堆栈
     * @return 当前的气体堆栈，如果为空则返回空堆栈
     */
    GasStack getGas();

    /**
     * 获取气体容量（毫升）
     * @return 最大容量
     */
    int getCapacity();

    /**
     * 获取当前存储量（毫升）
     * @return 当前存储量
     */
    int getGasAmount();

    /**
     * 检查是否为空
     * @return 如果为空返回 true
     */
    boolean isEmpty();

    /**
     * 填充气体
     * @param resource 要填充的气体堆栈
     * @param action 执行模式（SIMULATE 模拟，EXECUTE 执行）
     * @return 实际填充的气体量（毫升）
     */
    int fill(GasStack resource, GasAction action);

    /**
     * 抽取气体
     * @param maxDrain 最大抽取量（毫升）
     * @param action 执行模式（SIMULATE 模拟，EXECUTE 执行）
     * @return 抽取的气体堆栈
     */
    GasStack drain(int maxDrain, GasAction action);

    /**
     * 抽取指定类型的气体
     * @param resource 要抽取的气体堆栈
     * @param action 执行模式（SIMULATE 模拟，EXECUTE 执行）
     * @return 抽取的气体堆栈
     */
    GasStack drain(GasStack resource, GasAction action);

    /**
     * 检查是否可以填充指定的气体
     * @param gas 要检查的气体类型
     * @return 如果可以填充返回 true
     */
    boolean isGasValid(Gas gas);

    /**
     * 检查是否可以填充指定的气体堆栈
     * @param stack 要检查的气体堆栈
     * @return 如果可以填充返回 true
     */
    boolean isGasValid(GasStack stack);

    /**
     * 执行模式枚举
     */
    enum GasAction {
        /**
         * 模拟操作，不会实际改变存储
         */
        SIMULATE,
        /**
         * 执行操作，会实际改变存储
         */
        EXECUTE
    }
}

