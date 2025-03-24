package willow.train.kuayue.systems.overhead_line.block.support;

import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.overhead_line.OverheadLineSystem;
import willow.train.kuayue.systems.overhead_line.block.support.variants.OverheadLineInsulatorRenderer;
import willow.train.kuayue.systems.overhead_line.block.support.variants.OverheadLineSupportARenderer;
import willow.train.kuayue.systems.overhead_line.block.support.variants.OverheadLineSupportBRenderer;
import willow.train.kuayue.systems.overhead_line.block.support.variants.OverheadLineSupportCRenderer;

public class AllOverheadLineSupportBlocks {
    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock> OVERHEAD_LINE_SUPPORT_A1 =
            new OverheadLineSupportBlockReg<>("overhead_line_support_a1")
                    .blockType(OverheadLineSupportBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueOverheadLineTab)
                    .withRenderer(()->OverheadLineSupportARenderer.A1Renderer::new)
                    .connectionPoints(
                            new Vec3(1.55, .125, 0)
                    )
                    .submit(AllElements.testRegistry);
    
    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock> OVERHEAD_LINE_SUPPORT_A2 =
            new OverheadLineSupportBlockReg<>("overhead_line_support_a2")
                    .blockType(OverheadLineSupportBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueOverheadLineTab)
                    .withRenderer(()->OverheadLineSupportARenderer.A2Renderer::new)
                    .connectionPoints(
                            new Vec3(2.25, .125, 0)
                    )
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock> OVERHEAD_LINE_SUPPORT_B =
            new OverheadLineSupportBlockReg<>("overhead_line_support_b")
                    .blockType(OverheadLineSupportBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueOverheadLineTab)
                    .withRenderer(()-> OverheadLineSupportBRenderer.B1Renderer::new)
                    .connectionPoints(
                            new Vec3(1.55, .125, 0)
                    )
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock> OVERHEAD_LINE_SUPPORT_B2 =
            new OverheadLineSupportBlockReg<>("overhead_line_support_b2")
                    .blockType(OverheadLineSupportBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueOverheadLineTab)
                    .withRenderer(()-> OverheadLineSupportBRenderer.B2Renderer::new)
                    .connectionPoints(
                            new Vec3(2.25, .125, 0)
                    )
                    .submit(AllElements.testRegistry);
                    
    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock> OVERHEAD_LINE_SUPPORT_C =
            new OverheadLineSupportBlockReg<>("overhead_line_support_c")
                    .blockType(OverheadLineSupportBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueOverheadLineTab)
                    .withRenderer(()-> OverheadLineSupportCRenderer.C1Renderer::new)
                    .connectionPoints(
                            new Vec3(1.55, .125, - .52),
                            new Vec3(2.25, .125, .52)
                    )
                    .submit(AllElements.testRegistry);
                    
    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock> OVERHEAD_LINE_SUPPORT_C2 =
            new OverheadLineSupportBlockReg<>("overhead_line_support_c2")
                    .blockType(OverheadLineSupportBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueOverheadLineTab)
                    .withRenderer(()-> OverheadLineSupportCRenderer.C2Renderer::new)
                    .connectionPoints(
                            new Vec3(1.55, .125, .52),
                            new Vec3(2.25, .125, -.52)
                    )
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock> OVERHEAD_LINE_INSULATOR_A =
            new OverheadLineSupportBlockReg<>("overhead_line_insulator_a")
                    .blockType(OverheadLineSupportInsulatorBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueOverheadLineTab)
                    .withRenderer(()-> OverheadLineInsulatorRenderer.ARenderer::new)
                    .submit(AllElements.testRegistry);
                    
    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock> OVERHEAD_LINE_INSULATOR_B =
            new OverheadLineSupportBlockReg<>("overhead_line_insulator_b")
                    .blockType(OverheadLineSupportInsulatorBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueOverheadLineTab)
                    .withRenderer(()-> OverheadLineInsulatorRenderer.BRenderer::new)
                    .submit(AllElements.testRegistry);

    public static void invoke(){}
}
