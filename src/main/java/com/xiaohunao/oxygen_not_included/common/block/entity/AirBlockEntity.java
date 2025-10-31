package com.xiaohunao.oxygen_not_included.common.block.entity;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AirBlockEntity extends BlockEntity{
    private final Map<Gas, Integer> gasMap = Maps.newHashMap();


    public AirBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        initGas();
    }


    public AirBlockEntity(BlockPos pos, BlockState blockState) {
        super(ONIBlockEntityTypes.AIR_BLOCK_ENTITY.get(), pos, blockState);
        initGas();
    }

    public void initGas(){
//        gasMap.put(ONIGases.OXYGEN.get(), Gas.STANDARD_GAS_CONCENTRATION);
    }

    public Map<Gas, Integer> getGasMap() {
        return gasMap;
    }


    public int getTotalAmount() {
        int total = 0;
        for (Map.Entry<Gas, Integer> e : gasMap.entrySet()) {
            total += Objects.requireNonNullElse(e.getValue(), 0);
        }
        return total;
    }

    public void setGasAmount(Gas gas, int amount) {
        if (amount <= 0) {
            gasMap.remove(gas);
        } else {
            gasMap.put(gas, amount);
        }
        setChanged();
        sync();
    }



    public int getGasAmount(Gas gas) {
        return gasMap.getOrDefault(gas, 0);
    }

    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (Map.Entry<Gas, Integer> e : gasMap.entrySet()) {
            net.minecraft.nbt.CompoundTag g = new net.minecraft.nbt.CompoundTag();
            var id = com.xiaohunao.oxygen_not_included.common.init.ONIRegistries.GAS.getKey(e.getKey());
            if (id != null) {
                g.putString("id", id.toString());
                g.putInt("amount", e.getValue());
                list.add(g);
            }
        }
        tag.put("gases", list);
    }

    @Override
    protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        gasMap.clear();
        ListTag list = tag.getList("gases", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            net.minecraft.nbt.CompoundTag g = list.getCompound(i);
            String idStr = g.getString("id");
            int amount = g.getInt("amount");
            var rl = net.minecraft.resources.ResourceLocation.tryParse(idStr);
            if (rl != null) {
                Gas gas = com.xiaohunao.oxygen_not_included.common.init.ONIRegistries.GAS.get(rl);
                if (gas != null) {
                    gasMap.put(gas, amount);
                }
            }
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public void handleUpdateTag(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        loadAdditional(tag, provider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        if (this.level == null) return;
        if (!this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public static void tickServer(Level level, BlockPos pos, BlockState blockState, AirBlockEntity blockEntity) {

    }
}
