package willow.train.kuayue.systems.overhead_line.wire;

import willow.train.kuayue.systems.overhead_line.block.line.OverheadLineRenderer;
import willow.train.kuayue.systems.overhead_line.block.line.OverheadLineRendererSystem;
import willow.train.kuayue.systems.overhead_line.types.OverheadLineType;

import java.util.function.Supplier;

public class OverheadLineRendererUtils {
    public static void registerRenderer(OverheadLineType wireType, Supplier<Supplier<OverheadLineRenderer>> renderer) {
        OverheadLineRendererSystem.registerRenderer(wireType, renderer);
    }
}
