package com.xiaohunao.oxygen_not_included.common.util;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum TemperatureType implements StringRepresentable {
    Kelvin,
    Celsius,
    Fahrenheit,
    Mc;

    public static final Codec<TemperatureType> CODEC = StringRepresentable.fromEnum(TemperatureType::values);

    @Override
    @NotNull
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Component getDisplayName() {
        return switch (this) {
            case Kelvin -> Component.translatable("temperature_type.kelvin");
            case Celsius -> Component.translatable("temperature_type.celsius");
            case Fahrenheit -> Component.translatable("temperature_type.fahrenheit");
            case Mc -> Component.translatable("temperature_type.mc");
        };
    }


    public String getSymbol() {
        return switch (this) {
            case Kelvin -> "K";
            case Celsius -> "°C";
            case Fahrenheit -> "°F";
            case Mc -> "MC";
        };
    }

    /**
     * 转换温度值
     * @param value 要转换的温度值
     * @param from 源温度单位
     * @param to 目标温度单位
     * @param absolute 是否为绝对温度（影响华氏度和摄氏度的转换）
     * @return 转换后的温度值
     */
    public static double convert(double value, TemperatureType from, TemperatureType to, boolean absolute) {
        if (from == to) {
            return value;
        }

        return switch (from) {
            case Kelvin -> switch (to) {
                case Celsius -> value - 273.15;
                case Fahrenheit -> (value - 273.15) * 1.8 + (absolute ? 32d : 0d);
                case Mc -> (value - 273.15) / 25d;
                default -> value;
            };
            case Celsius -> switch (to) {
                case Kelvin -> value + 273.15;
                case Fahrenheit -> value * 1.8 + (absolute ? 32d : 0d);
                case Mc -> value / 25d;
                default -> value;
            };
            case Fahrenheit -> switch (to) {
                case Kelvin -> (value - (absolute ? 32d : 0d)) / 1.8 + 273.15;
                case Celsius -> (value - (absolute ? 32d : 0d)) / 1.8;
                case Mc -> (value - (absolute ? 32d : 0d)) / 45d;
                default -> value;
            };
            case Mc -> switch (to) {
                case Kelvin -> value * 25d + 273.15;
                case Celsius -> value * 25d;
                case Fahrenheit -> value * 45d + (absolute ? 32d : 0d);
                default -> value;
            };
        };
    }

    public static double convert(double value, TemperatureType from, TemperatureType to) {
        return convert(value, from, to, true);
    }


    public double convertTo(double value, TemperatureType to, boolean absolute) {
        return convert(value, this, to, absolute);
    }

    public double convertTo(double value, TemperatureType to) {
        return convert(value, this, to, true);
    }
}
