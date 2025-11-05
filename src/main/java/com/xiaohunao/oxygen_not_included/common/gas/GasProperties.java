package com.xiaohunao.oxygen_not_included.common.gas;

public class GasProperties {
    //气体颜色
    public final int color;
    //气体密度
    public final float density;
    //气体粘度
    public final float viscosity;
    //气体扩散速度
    public final float diffusion;
    //是否窒息
    public final boolean suffocation;
    //是否提供光照
    public final int light;

    private GasProperties(int color, float density, float viscosity, float diffusion, boolean suffocation, int light) {
        this.color = color;
        this.density = density;
        this.viscosity = viscosity;
        this.diffusion = diffusion;
        this.suffocation = suffocation;
        this.light = light;
    }

    public GasProperties copy() {
        return new GasProperties(color, density, viscosity, diffusion, suffocation, light);
    }

    public GasProperties withColor(int color) {
        return new GasProperties(color, density, viscosity, diffusion, suffocation, light);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static GasProperties builder(int color) {
        return new Builder().color(color).build();
    }

    public static class Builder {
        private int color = 0xFFFFFFFF;
        private float density = 1.0f;
        private float viscosity = 1.0f;
        private float diffusion = 1.0f;
        private boolean suffocation = false;
        private int light = 0;

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

        public Builder suffocation(boolean suffocation) {
            this.suffocation = suffocation;
            return this;
        }

        public Builder light(int light) {
            this.light = light;
            return this;
        }

        public GasProperties build() {
            return new GasProperties(color, density, viscosity, diffusion, suffocation, light);
        }

    }
}
