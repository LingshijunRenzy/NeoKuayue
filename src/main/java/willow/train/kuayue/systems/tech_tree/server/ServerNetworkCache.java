package willow.train.kuayue.systems.tech_tree.server;

import kasuga.lib.core.network.S2CPacket;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.KuayueConfig;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.s2c.tech_tree.*;
import willow.train.kuayue.systems.tech_tree.NetworkState;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;

public class ServerNetworkCache implements Runnable {
    private UUID batch;

    @Getter
    private final Player player;
    private final Queue<TechTree> waitingForSend;
    private final Queue<S2CPacket> packets;
    private TransmitStage transmitStage;
    private boolean threadStarted;
    private final Thread myThread;

    public ServerNetworkCache(Player player) {
        packets = new LinkedList<>();
        waitingForSend = new LinkedList<>();
        transmitStage = TransmitStage.STANDING_BY;
        this.player = player;
        myThread = new Thread(this);
        threadStarted = false;
    }

    public void enqueueTree(TechTree tree) {
        this.waitingForSend.add(tree);
        if (!threadStarted) {
            myThread.start();
            threadStarted = true;
            System.out.println("thread started");
        }
    }

    private void startBatch(UUID batchId) {
        this.batch = batchId;
        transmitStage = TransmitStage.HANDSHAKE;
    }

    private void compileTree(TechTree tree) {
        if (batch == null) return;
        packets.offer(new TechTreePacket(batch, tree));
        tree.getGroups().forEach((grpName, grp) -> {
            packets.offer(new TechTreeGroupPacket(batch, grp));
        });
        tree.getNodes().forEach((loc, node) -> {
            packets.offer(new TechTreeNodePacket(batch, node));
        });
    }

    public void collectClientNetworkState(NetworkState state) {
        transmitStage = TransmitStage.TRANSMITTING;
    }

    @Override
    public void run() {
        final int waitingMillis = KuayueConfig.CONFIG.
                getIntValue("TECH_TREE_TRANSMISSION_TIMEOUT");
        final int retryTimes = KuayueConfig.CONFIG.
                getIntValue("TECH_TREE_TRANSMISSION_RETRY_TIMES");
        while (true) {
            switch (transmitStage) {
                case STANDING_BY -> {
                    if (waitingForSend.isEmpty()) {
                        sendOverPacket();
                        threadStarted = false;
                        return;
                    }
                    startBatch(UUID.randomUUID());
                    compileTree(waitingForSend.poll());
                }
                case HANDSHAKE -> {
                    sendAndRetry(o -> sendHandShakePacket(),
                                 o -> forceStop(true, retryTimes),
                             waitingMillis, retryTimes,
                                        TransmitStage.TRANSMITTING);
                }
                case TRANSMITTING -> {
                    sendAndRetry(o -> sendPayloads(),
                            o -> forceStop(true, retryTimes),
                            waitingMillis, retryTimes,
                            TransmitStage.EOF);
                }
                case EOF -> {
                    sendAndRetry(o -> sendEOFPacket(),
                            o -> forceStop(true, retryTimes),
                            waitingMillis, retryTimes,
                            TransmitStage.STANDING_BY);
                }
            }
        }
    }

    public void sendAndRetry(Consumer<Object> consumer,
                             Consumer<Object> runOnFailed,
                             int waitingMillis,
                             int retryTimes,
                             TransmitStage nextStage) {
        int retry = 0;
        do {
            consumer.accept(null);
            delay(waitingMillis);
            retry++;
        } while (transmitStage != nextStage && retry < retryTimes);
        if (retry >= retryTimes) runOnFailed.accept(null);
    }

    private void clear() {
        packets.clear();
        transmitStage = TransmitStage.STANDING_BY;
        batch = null;
    }

    public void nextTree() {
        clear();
    }

    public void forceStop(boolean shouldPlayMessage, int times) {
        if (shouldPlayMessage) {
            Kuayue.LOGGER.error("Failed to send tech tree data to player {} on phase {}, " +
                            "already retried {} times.",
                    player.getDisplayName().getString(), transmitStage, times);
        }
        clear();
        waitingForSend.clear();
        transmitStage = TransmitStage.STANDING_BY;
    }

    private void delay(long waitingMillis) {
        try {
            Thread.sleep(waitingMillis);
        } catch (InterruptedException ignored) {}
    }

    private void sendHandShakePacket() {
        AllPackets.TECH_TREE_CHANNEL.sendToClient(new TechTreeHandShakeS2CPacket(batch), (ServerPlayer) player);
    }

    private void sendEOFPacket() {
        if (batch == null) return;
        AllPackets.TECH_TREE_CHANNEL.sendToClient(new TechTreeEOFS2CPacket(batch), (ServerPlayer) player);
    }

    private void sendOverPacket() {
        AllPackets.TECH_TREE_CHANNEL.sendToClient(new TechTreeSendOverPacket(), (ServerPlayer) player);
    }

    private void sendPayloads() {
        while (!packets.isEmpty()) {
            S2CPacket payload = packets.poll();
            AllPackets.TECH_TREE_CHANNEL.sendToClient(payload, (ServerPlayer) player);
        }
        this.transmitStage = TransmitStage.EOF;
    }

    public enum TransmitStage implements StringRepresentable {
        STANDING_BY,
        HANDSHAKE,
        TRANSMITTING,
        EOF;

        @Override
        public String getSerializedName() {
            return switch (this) {
                case STANDING_BY -> "standing_by";
                case HANDSHAKE -> "handshake";
                case TRANSMITTING -> "transmitting";
                case EOF -> "end_of_file";
            };
        }
    }
}
