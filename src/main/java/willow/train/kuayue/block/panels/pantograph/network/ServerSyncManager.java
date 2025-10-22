package willow.train.kuayue.block.panels.pantograph.network;

import com.simibubi.create.content.contraptions.Contraption;
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
    private final HashMap<Contraption, AtomicInteger> tickers;

    @Getter
    private final HashMap<Contraption, AtomicInteger> needSync;

    private final HashSet<Contraption> deadTickers;

    public static final ServerSyncManager INSTANCE = new ServerSyncManager();

    private ServerSyncManager() {
        tickers = new HashMap<Contraption, AtomicInteger>();
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

    public boolean needToSync(Contraption contraption) {
        tickers.computeIfAbsent(contraption,
                c -> new AtomicInteger(getConfigValue()));
        return needSync.containsKey(contraption);
    }

    public void forceSync(Contraption contraption) {
        if (needToSync(contraption)) return;
        needSync.put(contraption, tickers.computeIfAbsent(contraption,
                c -> new AtomicInteger(getConfigValue())));
    }

    public void removeTicker(Contraption contraption) {
        tickers.remove(contraption);
        needSync.remove(contraption);
    }

    public void clear() {
        tickers.clear();
        needSync.clear();
        deadTickers.clear();
    }

    public void sync(ServerLevel level, BlockPos pos, Contraption contraption, CurrOverheadLineCache cache) {
        if (!needSync.containsKey(contraption)) return;
        PantographSyncPacket packet = new PantographSyncPacket(contraption, cache);
        AllPackets.CHANNEL.boardcastToClients(packet, level, pos);
        needSync.remove(contraption).set(0);
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
