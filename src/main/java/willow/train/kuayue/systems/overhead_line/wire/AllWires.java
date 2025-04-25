package willow.train.kuayue.systems.overhead_line.wire;

import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.overhead_line.block.line.OverheadLineRenderer;

public class AllWires {
    public static WireReg OVERHEAD_LINE_WIRE =
            new WireReg("overhead_line_wire")
                    .maxVoltage(1000)
                    .maxCurrent(1000)
                    .maxLength(1000)
                    .renderer(() -> OverheadLineWireRenderer::new)
                    .submit(AllElements.testRegistry);

    public static WireReg ELECTRONIC_WIRE =
            new WireReg("electronic_wire")
                    .maxVoltage(1000)
                    .maxCurrent(1000)
                    .maxLength(1000)
                    .renderer(() -> ElectronicLineRenderer::new)
                    .submit(AllElements.testRegistry);

    public static void invoke(){}
}
