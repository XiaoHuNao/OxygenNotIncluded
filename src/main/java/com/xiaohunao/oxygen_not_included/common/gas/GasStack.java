package com.xiaohunao.oxygen_not_included.common.gas;

/**
 * 气体堆栈
 * 类似于 FluidStack，表示一定数量的气体
 */
public class GasStack {
    /**
     * 定义一个标准气压一格方块可以容纳的气体含量ml
     */
    public static final int STANDARD_PRESSURE = 1000;

    /**
     * 空的气体堆栈
     */
    public static final GasStack EMPTY = new GasStack(null, 0);

    private final Gas gas;
    private int amount;

    public GasStack(Gas gas, int amount) {
        this.gas = gas;
        this.amount = amount;
    }

    /**
     * 获取气体类型
     * @return 气体类型，如果为空则返回 null
     */
    public Gas getGas() {
        return gas;
    }

    /**
     * 获取气体数量（毫升）
     * @return 气体数量
     */
    public int getAmount() {
        return amount;
    }

    /**
     * 设置气体数量
     * @param amount 新的数量
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * 增加气体数量
     * @param amount 要增加的数量
     */
    public void grow(int amount) {
        this.amount += amount;
    }

    /**
     * 减少气体数量
     * @param amount 要减少的数量
     */
    public void shrink(int amount) {
        this.amount = Math.max(0, this.amount - amount);
    }

    /**
     * 检查是否为空
     * @return 如果气体为空或数量为0返回 true
     */
    public boolean isEmpty() {
        return gas == null || amount <= 0;
    }

    /**
     * 复制气体堆栈
     * @return 新的气体堆栈副本
     */
    public GasStack copy() {
        return new GasStack(gas, amount);
    }

    /**
     * 创建指定数量的气体堆栈副本
     * @param amount 新的数量
     * @return 新的气体堆栈
     */
    public GasStack copyWithAmount(int amount) {
        return new GasStack(gas, amount);
    }
}
