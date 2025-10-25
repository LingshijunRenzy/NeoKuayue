package willow.train.kuayue.block.panels.pantograph.network;

import com.simibubi.create.content.contraptions.Contraption;
import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import willow.train.kuayue.KuayueConfig;
import willow.train.kuayue.block.panels.pantograph.CurrOverheadLineCache;
import willow.train.kuayue.initial.AllPackets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerSyncManager {

    @Getter
    private final HashMap<Pair<Contraption, BlockPos>, AtomicInteger> tickers;

    @Getter
    private final HashMap<Pair<Contraption, BlockPos>, AtomicInteger> needSync;

    private final HashSet<Pair<Contraption, BlockPos>> deadTickers;

    public static final ServerSyncManager INSTANCE = new ServerSyncManager();

    private ServerSyncManager() {
        tickers = new HashMap<Pair<Contraption, BlockPos>, AtomicInteger>();
        needSync = new HashMap<>();
        deadTickers = new HashSet<>();
    }

    public static ServerSyncManager getInstance() {
        return INSTANCE;
    }

    public void tick(TickEvent.ServerTickEvent event) {
        final int configValue = getConfigValue();
        tickers.forEach((c, i) -> {
            int val = i.incrementAndGet();
            if (val > configValue) {
                if (val > 10 * configValue) {
                    // 太长时间无响应，判定为这个 contraption 已经不存在了
                    deadTickers.add(c);
                    return;
                }
                if (needSync.containsKey(c)) return;
                needSync.put(c, i);
            }
        });

        // 对于不存在的 contraption 的映像要予以删除, 使得 vm 能够尽快释放资源
        deadTickers.forEach(c -> {
            needSync.remove(c);
            tickers.remove(c);
        });
        deadTickers.clear();
    }

    public boolean needToSync(Contraption contraption, BlockPos localPos) {
        Pair<Contraption, BlockPos> pair = Pair.of(contraption, localPos);
        tickers.computeIfAbsent(pair,
                c -> new AtomicInteger(getConfigValue()));
        return needSync.containsKey(pair);
    }

    public void forceSync(Contraption contraption, BlockPos localPos) {
        if (needToSync(contraption, localPos)) return;
        Pair<Contraption, BlockPos> pair = Pair.of(contraption, localPos);
        needSync.put(pair, tickers.computeIfAbsent(pair,
                c -> new AtomicInteger(getConfigValue())));
    }

    public void removeTicker(Contraption contraption, BlockPos localPos) {
        Pair<Contraption, BlockPos> pair =  Pair.of(contraption, localPos);
        tickers.remove(pair);
        needSync.remove(pair);
    }

    public void clear() {
        tickers.clear();
        needSync.clear();
        deadTickers.clear();
    }

    public void sync(ServerLevel level, BlockPos localPos, BlockPos pos, Contraption contraption, CurrOverheadLineCache cache) {
        Pair<Contraption, BlockPos> pair = Pair.of(contraption, localPos);
        if (!needSync.containsKey(pair)) return;
        PantographSyncPacket packet = new PantographSyncPacket(contraption, localPos, cache);
        AllPackets.CHANNEL.boardcastToClients(packet, level, pos);
        needSync.remove(pair).set(0);
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        ServerSyncManager.getInstance().tick(event);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        ServerSyncManager.getInstance().clear();
    }

    public int getConfigValue() {
        return Math.max(
                KuayueConfig.CONFIG.getIntValue("PANTOGRAPH_FRESH_INTERVAL_TICKS_SERVER"),
                KuayueConfig.CONFIG.getIntValue("PANTOGRAPH_SYNC_TICKS"));
    }
}
