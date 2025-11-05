package com.xiaohunao.oxygen_not_included.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;

import java.util.Objects;

public class GasTankData {
    public static final Codec<GasTankData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ONIRegistries.GAS.byNameCodec().fieldOf("gas").forGetter(GasTankData::getGas),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(GasTankData::getAmount)
    ).apply(instance, GasTankData::new));

    private Gas gas;
    private int amount;

    public GasTankData() {
        this(null, 0);
    }

    public GasTankData(Gas gas, int amount) {
        this.gas = gas;
        this.amount = amount;
    }

    public Gas getGas() {
        return gas;
    }

    public void setGas(Gas gas) {
        this.gas = gas;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isEmpty() {
        return gas == null|| amount <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GasTankData that)) return false;
        return getAmount() == that.getAmount() && Objects.equals(getGas(), that.getGas());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGas(), getAmount());
    }
}


