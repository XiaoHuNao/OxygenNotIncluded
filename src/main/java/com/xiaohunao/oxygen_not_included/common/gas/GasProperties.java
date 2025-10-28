package com.xiaohunao.oxygen_not_included.common.gas;

/**
 * 气体渲染与物性参数
 */
public class GasProperties {
    /** ARGB 颜色（含透明度） */
    private final int argbColor;
    /** 相对密度（空气=1.0） */
    private final float relativeDensity;
    /** 可见性（0-1，影响渲染透明度基准） */
    private final float visibility;
    /** 流动速度系数（0-1，用于扩散/插值） */
    private final float flowSpeed;

    public GasProperties(int argbColor, float relativeDensity, float visibility, float flowSpeed) {
        this.argbColor = argbColor;
        this.relativeDensity = relativeDensity;
        this.visibility = visibility;
        this.flowSpeed = flowSpeed;
    }

    public int getArgbColor() {
        return argbColor;
    }

    public float getRelativeDensity() {
        return relativeDensity;
    }

    public float getVisibility() {
        return visibility;
    }

    public float getFlowSpeed() {
        return flowSpeed;
    }

    public static GasProperties oxygen() {
        // 天蓝，较轻，可见性中等，流动适中
        return new GasProperties(0xFF7EC8E3, 0.95f, 0.6f, 0.6f);
    }

    public static GasProperties carbonDioxide() {
        // 灰色，较重，可见性较高，流动略慢
        return new GasProperties(0xFF8E8E8E, 1.50f, 0.75f, 0.45f);
    }

    public static GasProperties hydrogen() {
        // 淡黄，极轻，可见性较低，流动较快
        return new GasProperties(0xFFEFE6A7, 0.07f, 0.4f, 0.8f);
    }
}
