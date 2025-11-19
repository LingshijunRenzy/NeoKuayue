package willow.train.kuayue.network.s2c;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.trains.entity.Train;
import kasuga.lib.core.network.S2CPacket;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class TrainMigrationSyncPacket extends S2CPacket {

    @Getter
    private final UUID trainA, trainB;

    public TrainMigrationSyncPacket(UUID trainA, UUID trainB) {
        this.trainA = trainA;
        this.trainB = trainB;
    }

    public TrainMigrationSyncPacket(FriendlyByteBuf buf) {
        trainA = buf.readUUID();
        trainB = buf.readUUID();
    }

    @Override
    public void handle(Minecraft minecraft) {
        Train tA = CreateClient.RAILWAYS.trains.get(trainA);
        Train tB = CreateClient.RAILWAYS.trains.get(trainB);

        if (tA == null || tB == null) return;
        if (Minecraft.getInstance().level == null) return;
        // Test.tryMigrateTrains(tA, tB, Minecraft.getInstance().level, true);
    }

    @Override
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(trainA);
        friendlyByteBuf.writeUUID(trainB);
    }
}
