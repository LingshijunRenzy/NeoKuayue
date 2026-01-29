package willow.train.kuayue.systems.train_extension.client.overlay;


import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import willow.train.kuayue.systems.train_extension.client.overlay.handler.CouplerOverlayHandler;
import willow.train.kuayue.systems.train_extension.client.overlay.handler.IContraptionFocusHandler;
import willow.train.kuayue.systems.train_extension.conductor.registry.ConductorCandidateRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TrainOverlayRegistry {

    private static final List<Pair<Predicate<TrainOverlayContext>, IContraptionFocusHandler>> handlers = new ArrayList<>();

    public static void register(Predicate<TrainOverlayContext> matcher, IContraptionFocusHandler handler) {
        handlers.add(Pair.of(matcher, handler));
    }

    public static void register(Block block, IContraptionFocusHandler handler) {
        register(context -> context.state().is(block), handler);
    }

    public static void register(TagKey<Block> tag, IContraptionFocusHandler handler) {
        register(context ->  context.state().is(tag), handler);
    }

    public static boolean process(TrainOverlayContext context) {
        for(Pair<Predicate<TrainOverlayContext>, IContraptionFocusHandler> pair : handlers) {
            if(!pair.getFirst().test(context)) continue;
            if(pair.getSecond().handle(context)) {
                return true;
            }
        }
        return false;
    }

    public static void invoke() {
        // Register overlay handlers here
        handlers.add(Pair.of(
           context -> ConductorCandidateRegistry.getProvider(context.state()) != null,
                new CouplerOverlayHandler()
        ));
    }
}
