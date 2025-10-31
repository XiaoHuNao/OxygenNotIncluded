package com.xiaohunao.oxygen_not_included.common.capability;

import java.util.Map;

import com.xiaohunao.oxygen_not_included.common.gas.Gas;

public interface IGasStorage {

    int getAmount(Gas gas);

    int getCapacity(Gas gas);

    int insert(Gas gas, int amount, boolean simulate);

    int extract(Gas gas, int amount, boolean simulate);

    Map<Gas, Integer> getAll();
}


