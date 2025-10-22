package willow.train.kuayue.block.panels.pantograph.network;

import com.simibubi.create.content.contraptions.Contraption;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.block.panels.pantograph.CurrOverheadLineCache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientSyncManager {

    private final HashMap<Pair<Integer, BlockPos>, Pair<CurrOverheadLineCache, AtomicInteger>> caches;

    private final HashSet<Pair<Integer, BlockPos>> deadTickers;

    public static final ClientSyncManager INSTANCE = new ClientSyncManager();

    private ClientSyncManager() {
        caches = new HashMap<>();
        deadTickers = new HashSet<>();
    }

    public static ClientSyncManager getInstance() {
        return INSTANCE;
    }

    public void push(PantographSyncPacket packet) {
        Pair<Integer, BlockPos> pair = Pair.of(packet.getEntityId(), packet.getLocalPos());
        if (caches.containsKey(pair)) {
            Kuayue.LOGGER.warn("contraption entity id <{}>, local pos <{}> has overtime sync packets, Override!", packet.getEntityId(), packet.getLocalPos());
        }
        caches.put(pair, Pair.of(packet.getCache(), new AtomicInteger(0)));
    }

    public boolean needToSync(Contraption contraption, BlockPos blockPos) {
        return caches.containsKey(Pair.of(contraption.entity.getId(), blockPos));
    }

    public CurrOverheadLineCache pop(Contraption contraption, BlockPos localPos) {
        int id = contraption.entity.getId();
        Pair<Integer, BlockPos> pair = Pair.of(id, localPos);
        @Nullable Pair<CurrOverheadLineCache, AtomicInteger> cache =
                caches.remove(pair);
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
