package com.xiaohunao.oxygen_not_included;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.xiaohunao.oxygen_not_included.common.init.ONIComponents;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlockEntityTypes;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlocks;
import com.xiaohunao.oxygen_not_included.common.init.ONICreativeTabs;
import com.xiaohunao.oxygen_not_included.common.init.ONIGases;
import com.xiaohunao.oxygen_not_included.common.init.ONIItems;
import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;


@Mod(OxygenNotIncluded.MODID)
public class OxygenNotIncluded {
    public static final String MODID = "oxygen_not_included";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OxygenNotIncluded(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ONIRegistries::registerRegistries);

        ONIComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        ONIBlocks.BLOK.register(modEventBus);
        ONIBlockEntityTypes.BLOCK_ENTITY_TYPE.register(modEventBus);
        ONIGases.GAS.register(modEventBus);
        ONICreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ONIItems.ITEM.register(modEventBus);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static String asDescriptionId(String path) {
        return MODID + "." + path;
    }

    public static <T> ResourceKey<T> asResourceKey(ResourceKey<? extends Registry<T>> registryKey, String path) {
        return ResourceKey.create(registryKey, asResource(path));
    }

    public static <T> ResourceKey<Registry<T>> asResourceKey(String path) {
        return ResourceKey.createRegistryKey(asResource(path));
    }
}
