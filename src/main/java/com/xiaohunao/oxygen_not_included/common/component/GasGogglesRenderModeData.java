package com.xiaohunao.oxygen_not_included.common.component;

import com.xiaohunao.oxygen_not_included.common.item.GasGogglesRenderMode;

import static java.util.Objects.requireNonNull;

public record GasGogglesRenderModeData(GasGogglesRenderMode mode) {
    public static final com.mojang.serialization.Codec<GasGogglesRenderModeData> CODEC = GasGogglesRenderMode.CODEC.xmap(GasGogglesRenderModeData::new, GasGogglesRenderModeData::mode);
    public static final GasGogglesRenderModeData DEFAULT = new GasGogglesRenderModeData(GasGogglesRenderMode.NAME);

    public GasGogglesRenderModeData(GasGogglesRenderMode mode) {
        this.mode = requireNonNull(mode, "mode");
    }

    public GasGogglesRenderModeData withMode(GasGogglesRenderMode newMode) {
        return new GasGogglesRenderModeData(newMode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GasGogglesRenderModeData(GasGogglesRenderMode mode1))) return false;
        return mode == mode1;
    }

    @Override
    public String toString() {
        return "GasGogglesRenderModeData{" + "mode=" + mode + '}';
    }
}
