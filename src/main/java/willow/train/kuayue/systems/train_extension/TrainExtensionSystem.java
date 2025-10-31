package willow.train.kuayue.systems.train_extension;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import kasuga.lib.core.base.Saved;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.s2c.TrainExtensionSyncPacket;
import willow.train.kuayue.systems.train_extension.bogey_weight.BogeyExtensionSystem;
import willow.train.kuayue.systems.train_extension.conductor.ConductorType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class TrainExtensionSystem extends SavedData {

    public final String resourceKey = "train_extension_store";

    public final Saved<TrainExtensionSystem> distSaving =
            new Saved<>(resourceKey, TrainExtensionSystem::getInstance, TrainExtensionSystem::load);

    private final HashMap<ResourceLocation, ConductorType> types;

    @Getter
    private final HashMap<UUID, TrainAdditionalData> data;

    public final BogeyExtensionSystem BOGEY_EXTENSION;

    public final HashSet<UUID> trainsToRemove;

    public TrainExtensionSystem() {
        this.data = new HashMap<>();
        this.types = new HashMap<>();
        types.put(ConductorType.DUMMY.id(), ConductorType.DUMMY);
        BOGEY_EXTENSION = new BogeyExtensionSystem();
        trainsToRemove = new HashSet<>();
    }

    public void broadcastToClients(ServerLevel level, BlockPos pos) {
        AllPackets.CHANNEL.boardcastToClients(new TrainExtensionSyncPacket(this), level, pos);
    }

    public void serverSync(ServerPlayer player) {
        AllPackets.CHANNEL.sendToClient(new TrainExtensionSyncPacket(this), player);
    }

    public void clientSync(HashMap<UUID, TrainAdditionalData> data) {
        this.data.putAll(data);
    }

    public void removeTrain(UUID trainId) {
        trainsToRemove.add(trainId);
    }

    public void cancelRemoveTrain(UUID trainId) {
        trainsToRemove.remove(trainId);
    }

    public static TrainExtensionSystem getInstance() {
        return Kuayue.TRAIN_EXTENSION;
    }

    public void register(ConductorType type) {
        this.types.put(type.id(), type);
    }

    public @Nullable ConductorType getType(ResourceLocation id) {
        return this.types.get(id);
    }

    public boolean hasType(ResourceLocation id) {
        return this.types.containsKey(id);
    }

    public @Nullable TrainAdditionalData get(UUID id) {
        return this.data.get(id);
    }

    public @NotNull TrainAdditionalData getOrCreate(Train train) {
        return this.data.computeIfAbsent(train.id, id -> {
            return new TrainAdditionalData(train);
        });
    }

    public void add(TrainAdditionalData data) {
        this.data.put(data.getTrain(), data);
    }

    public void remove(UUID trainId) {
        this.data.remove(trainId);
    }

    public boolean contains(UUID trainId) {
        return this.data.containsKey(trainId);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        int i = 0;
        for (Map.Entry<UUID, TrainAdditionalData> entry : this.data.entrySet()) {
            CompoundTag nbt = new CompoundTag();
            TrainAdditionalData data = entry.getValue();
            data.write(nbt);
            tag.put("data" + i, nbt);
            i++;
        }
        return tag;
    }

    public void clearData() {
        data.clear();
    }

    public static TrainExtensionSystem load(@NotNull CompoundTag nbt) {
        TrainExtensionSystem sys = Kuayue.TRAIN_EXTENSION;
        sys.clearData();
        int i = 0;
        while (nbt.contains("data" + i)) {
            CompoundTag tag = nbt.getCompound("data" + i);
            i++;
            TrainAdditionalData data = new TrainAdditionalData(tag);
            sys.data.put(data.getTrain(), data);
        }
        return sys;
    }
}
