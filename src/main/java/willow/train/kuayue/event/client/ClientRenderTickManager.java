package willow.train.kuayue.event.client;

import net.minecraftforge.event.TickEvent;

import java.util.HashSet;
import java.util.List;

public class ClientRenderTickManager {

    public interface TickReceiver {
        public void onRenderTick();
    }
    public static final HashSet<TickReceiver> receivers = new HashSet<>();

    public static void renderClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.END){
            return;
        }
        List<TickReceiver> tickReceiversList;
        synchronized (receivers) {
            tickReceiversList = List.copyOf(receivers);
        }
        for (TickReceiver receiver : tickReceiversList) {
            receiver.onRenderTick();
        }
    }

    public static void register(TickReceiver receiver) {
        synchronized (receivers) {
            receivers.add(receiver);
        }
    }

    public static void unregister(TickReceiver receiver) {
        synchronized (receivers){
            receivers.remove(receiver);
        }
    }
}
