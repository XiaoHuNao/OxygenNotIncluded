package com.xiaohunao.oxygen_not_included.common.gas;

public class GasProperties {
    public static final GasProperties EMPTY = GasProperties.builder().build();

    //摩尔质量
    public final double moles;
    //气体颜色
    public final int color;
    //气体密度
    public final float density;
    //气体粘度
    public final float viscosity;
    //气体扩散速度
    public final float diffusion;
    //是否允许呼吸
    public final boolean canBreathe;
    //比热容
    public final double heatCapacity;
    //导热率
    public final double thermalConductivity;
    //是否提供光照
    public final int light;

    private GasProperties(double moles,int color, float density, float viscosity, float diffusion,double heatCapacity,double thermalConductivity,int light,boolean canBreathe) {
        this.moles = moles;
        this.color = color;
        this.density = density;
        this.viscosity = viscosity;
        this.diffusion = diffusion;
        this.heatCapacity = heatCapacity;
        this.thermalConductivity = thermalConductivity;
        this.light = light;
        this.canBreathe = canBreathe;
    }

    public GasProperties copy() {
        return new GasProperties(moles ,color, density, viscosity, diffusion, heatCapacity, thermalConductivity, light, canBreathe);
    }

    public GasProperties withColor(int color) {
        return new GasProperties(moles, color, density, viscosity, diffusion, heatCapacity, thermalConductivity, light, canBreathe);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static GasProperties builder(int color) {
        return new Builder().color(color).build();
    }

    public static class Builder {
        private double moles = 1.0;
        private int color = 0xFFFFFFFF;
        private float density = 1.0f;
        private float viscosity = 1.0f;
        private float diffusion = 1.0f;
        private double heatCapacity = 1.0;
        private double thermalConductivity = 1.0;
        private int light = 0;
        private boolean canBreathe = true;

        public Builder moles(double moles) {
            this.moles = moles;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder density(float density) {
            this.density = density;
            return this;
        }

        public Builder viscosity(float viscosity) {
            this.viscosity = viscosity;
            return this;
        }

        public Builder diffusion(float diffusion) {
            this.diffusion = diffusion;
            return this;
        }

        public Builder heatCapacity(double heatCapacity) {
            this.heatCapacity = heatCapacity;
            return this;
        }

        public Builder thermalConductivity(double thermalConductivity) {
            this.thermalConductivity = thermalConductivity;
            return this;
        }

        public Builder light(int light) {
            this.light = light;
            return this;
        }

        public Builder canBreathe(boolean canBreathe) {
            this.canBreathe = canBreathe;
            return this;
        }

        public GasProperties build() {
            return new GasProperties(moles, color, density, viscosity, diffusion, heatCapacity, thermalConductivity, light, canBreathe);
        }

    }
}
