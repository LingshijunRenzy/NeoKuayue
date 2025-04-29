package willow.train.kuayue.event.client;

import net.minecraftforge.event.TickEvent;

import java.util.HashSet;

public class ClientRenderTickManager {

    public interface TickReceiver {
        public void onRenderTick();
    }
    public static HashSet<TickReceiver> receivers = new HashSet<>();

    public static void renderClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.END){
            return;
        }
        for (TickReceiver receiver : receivers) {
            receiver.onRenderTick();
        }
    }

    public static void register(TickReceiver receiver) {
        receivers.add(receiver);
    }

    public static void unregister(TickReceiver receiver) {
        receivers.remove(receiver);
    }
}
