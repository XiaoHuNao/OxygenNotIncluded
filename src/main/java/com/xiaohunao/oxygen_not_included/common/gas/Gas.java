package com.xiaohunao.oxygen_not_included.common.gas;

public class Gas {
    //一个空气方块标准气体含量为1000ml
    public static final int STANDARD_GAS_CONCENTRATION = 1000;


    public final GasProperties properties;

    public Gas(GasProperties properties) {
        this.properties = properties;
    }
}
