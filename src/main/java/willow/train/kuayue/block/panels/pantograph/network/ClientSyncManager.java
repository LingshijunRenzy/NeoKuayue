package willow.train.kuayue.block.panels.pantograph.network;

import com.simibubi.create.content.contraptions.Contraption;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.block.panels.pantograph.CurrOverheadLineCache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientSyncManager {

    private final HashMap<Integer, Pair<CurrOverheadLineCache, AtomicInteger>> caches;

    private final HashSet<Integer> deadTickers;

    public static final ClientSyncManager INSTANCE = new ClientSyncManager();

    private ClientSyncManager() {
        caches = new HashMap<>();
        deadTickers = new HashSet<>();
    }

    public static ClientSyncManager getInstance() {
        return INSTANCE;
    }

    public void push(PantographSyncPacket packet) {
        if (caches.containsKey(packet.getEntityId())) {
            Kuayue.LOGGER.warn("{} has overtime sync packets, Override!", packet.getEntityId());
        }
        caches.put(packet.getEntityId(), Pair.of(packet.getCache(), new AtomicInteger(0)));
    }

    public boolean needToSync(Contraption contraption) {
        return caches.containsKey(contraption.entity.getId());
    }

    public CurrOverheadLineCache pop(Contraption contraption) {
        int id = contraption.entity.getId();
        @Nullable Pair<CurrOverheadLineCache, AtomicInteger> cache =
                caches.remove(id);
        if (cache == null) return null;
        return cache.getFirst();
    }

    public void tick() {
        caches.forEach((k, c) -> {
            if (c.getSecond().incrementAndGet() > 600) {
                deadTickers.add(k);
            }
        });
        deadTickers.forEach(caches::remove);
        deadTickers.clear();
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        getInstance().tick();
    }
}
