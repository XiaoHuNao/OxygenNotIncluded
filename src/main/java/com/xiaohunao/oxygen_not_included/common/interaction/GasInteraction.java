package com.xiaohunao.oxygen_not_included.common.interaction;

import com.mojang.serialization.Codec;
import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;
import com.xiaohunao.xhn_lib.common.codec.ICodec;

import java.util.List;
import java.util.function.Function;

/**
 * 定义一个气体交互的行为。
 * 交互会在服务器端由 {@link GasInteractionManager} 根据上下文进行调度。
 */
public interface GasInteraction extends ICodec<GasInteraction>{
    Codec<GasInteraction> CODEC = Codec.lazyInitialized(ONIRegistries.GAS_INTERACTION_CODEC::byNameCodec).dispatch(GasInteraction::codec, Function.identity());

	/**
	 * 交互所属的类别。
	 */
	List<GasInteractionCategory> category();

	/**
	 * 当前交互是否适用于给定上下文。
	 */
	boolean matches(GasInteractionContext context);

	/**
	 * 执行交互逻辑。
	 */
	void apply(GasInteractionContext context);
}

