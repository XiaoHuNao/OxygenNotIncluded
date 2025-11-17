package com.xiaohunao.oxygen_not_included.common.item;

public enum GasGogglesRenderMode implements net.minecraft.util.StringRepresentable {
    NAME("gas_goggles.mode.name"),
    AMOUNT("gas_goggles.mode.amount"),
    TEMPERATURE("gas_goggles.mode.temperature");

    private final String translationKeySuffix;

    GasGogglesRenderMode(String translationKeySuffix) {
        this.translationKeySuffix = translationKeySuffix;
    }

    public static final com.mojang.serialization.Codec<GasGogglesRenderMode> CODEC = net.minecraft.util.StringRepresentable.fromEnum(GasGogglesRenderMode::values);

    public GasGogglesRenderMode cycle(int step) {
        GasGogglesRenderMode[] modes = values();
        int index = (this.ordinal() + step) % modes.length;
        if (index < 0) {
            index += modes.length;
        }
        return modes[index];
    }

    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("item.oxygen_not_included." + translationKeySuffix);
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
