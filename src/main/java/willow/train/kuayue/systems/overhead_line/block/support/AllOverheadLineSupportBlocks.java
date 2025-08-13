package willow.train.kuayue.systems.overhead_line.block.support;

import kasuga.lib.registrations.common.BlockEntityReg;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.overhead_line.block.support.variants.*;
import willow.train.kuayue.systems.overhead_line.wire.AllWires;

public class AllOverheadLineSupportBlocks {

    public static BlockEntityReg<OverheadLineEndWeightBlockEntity> OVERHEAD_LINE_END_WEIGHT_BLOCK_ENTITY =
            new BlockEntityReg<OverheadLineEndWeightBlockEntity>("overhead_line_end_weight_block_entity")
                    .blockEntityType(OverheadLineEndWeightBlockEntity::new)
                    .blockPredicates((r, i)->i instanceof OverheadLineSupportBlock)
                    .withRenderer(()->OverheadSupportBlockRenderer::new)
                    .submit(AllElements.testRegistry);

    public static BlockEntityReg<OverheadLineSupportBBlockEntity> OVERHEAD_LINE_SUPPORT_B_BLOCK_ENTITY =
            new BlockEntityReg<OverheadLineSupportBBlockEntity>("overhead_line_support_b_block_entity")
                    .blockEntityType(OverheadLineSupportBBlockEntity::new)
                    .blockPredicates((r, i)->i instanceof OverheadLineSupportBlock)
                    .withRenderer(()->OverheadSupportBlockRenderer::new)
                    .submit(AllElements.testRegistry);

    public static BlockEntityReg<OverheadLineSupportB2BlockEntity> OVERHEAD_LINE_SUPPORT_B2_BLOCK_ENTITY =
            new BlockEntityReg<OverheadLineSupportB2BlockEntity>("overhead_line_support_b2_block_entity")
                    .blockEntityType(OverheadLineSupportB2BlockEntity::new)
                    .blockPredicates((r, i)->i instanceof OverheadLineSupportBlock)
                    .withRenderer(()->OverheadSupportBlockRenderer::new)
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock<OverheadLineSupportBlockEntity>,OverheadLineSupportBlockEntity> OVERHEAD_LINE_SUPPORT_A1 =
            new OverheadLineSupportBlockReg<>("overhead_line_support_a1")
                    .blockType(NormalOverheadLineSupportBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .withRenderer(()->OverheadLineSupportARenderer.A1Renderer::new)
                    .connectionPoints(
                            new Vec3(1.55, .125, 0)
                    )
                    .addAllowedWireType(
                            AllWires.OVERHEAD_LINE_WIRE,
                            AllWires.ELECTRONIC_WIRE
                    )
                    .maxConnections(8)
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock<OverheadLineSupportBlockEntity>,OverheadLineSupportBlockEntity> OVERHEAD_LINE_SUPPORT_A2 =
            new OverheadLineSupportBlockReg<>("overhead_line_support_a2")
                    .blockType(NormalOverheadLineSupportBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .withRenderer(()->OverheadLineSupportARenderer.A2Renderer::new)
                    .connectionPoints(
                            new Vec3(2.25, .125, 0)
                    )
                    .addAllowedWireType(
                            AllWires.OVERHEAD_LINE_WIRE,
                            AllWires.ELECTRONIC_WIRE
                    )
                    .maxConnections(8)
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineSupportBBlock,OverheadLineSupportBBlockEntity> OVERHEAD_LINE_SUPPORT_B =
            new OverheadLineSupportBlockReg<OverheadLineSupportBBlock,OverheadLineSupportBBlockEntity>("overhead_line_support_b")
                    .blockType(OverheadLineSupportBBlock::new)
                    .withBlockEntity(OVERHEAD_LINE_SUPPORT_B_BLOCK_ENTITY)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .withRenderer(()-> OverheadLineSupportBRenderer.B1Renderer::new)
                    .connectionPoints(
                            new Vec3(1.55, -0.3, 0)
                    )
                    .addAllowedWireType(
                            AllWires.OVERHEAD_LINE_WIRE,
                            AllWires.ELECTRONIC_WIRE
                    )
                    .maxConnections(8)
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineSupportB2Block,OverheadLineSupportB2BlockEntity> OVERHEAD_LINE_SUPPORT_B2 =
            new OverheadLineSupportBlockReg<OverheadLineSupportB2Block,OverheadLineSupportB2BlockEntity>("overhead_line_support_b2")
                    .blockType(OverheadLineSupportB2Block::new)
                    .withBlockEntity(OVERHEAD_LINE_SUPPORT_B2_BLOCK_ENTITY)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .withRenderer(()-> OverheadLineSupportBRenderer.B2Renderer::new)
                    .connectionPoints(
                            new Vec3(2.25, -0.6, 0)
                    )
                    .addAllowedWireType(
                            AllWires.OVERHEAD_LINE_WIRE,
                            AllWires.ELECTRONIC_WIRE
                    )
                    .maxConnections(8)
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock<OverheadLineSupportBlockEntity>,OverheadLineSupportBlockEntity> OVERHEAD_LINE_SUPPORT_C =
            new OverheadLineSupportBlockReg<>("overhead_line_support_c")
                    .blockType(NormalOverheadLineSupportBlock::new)
                    .withItem((b, p)->new CustomRendererItem(b, p).withRenderer(()->OverheadLineSupportCRenderer.C1ItemRenderer::new), AllElements.testRegistry.asResource("dynamic_renderer_item/overhead_line_support_c1"))
                    .tabTo(AllElements.neoKuayueGridTab)
                    .withRenderer(()-> OverheadLineSupportCRenderer.C1Renderer::new)
                    .connectionPoints(
                            new Vec3(1.55, .125, - .52),
                            new Vec3(2.25, .125, .52)
                    )
                    .addAllowedWireType(
                            AllWires.OVERHEAD_LINE_WIRE,
                            AllWires.ELECTRONIC_WIRE
                    )
                    .maxConnections(8)
                    .shouldCustomRenderItem(true)
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock<OverheadLineSupportBlockEntity>,OverheadLineSupportBlockEntity> OVERHEAD_LINE_SUPPORT_C2 =
            new OverheadLineSupportBlockReg<>("overhead_line_support_c2")
                    .blockType(NormalOverheadLineSupportBlock::new)
                    .withItem((b, p)->new CustomRendererItem(b, p).withRenderer(()->OverheadLineSupportCRenderer.C2ItemRenderer::new), AllElements.testRegistry.asResource("dynamic_renderer_item/overhead_line_support_c2"))
                    .tabTo(AllElements.neoKuayueGridTab)
                    .withRenderer(()-> OverheadLineSupportCRenderer.C2Renderer::new)
                    .connectionPoints(
                            new Vec3(1.55, .125, .52),
                            new Vec3(2.25, .125, -.52)
                    )
                    .addAllowedWireType(
                            AllWires.OVERHEAD_LINE_WIRE,
                            AllWires.ELECTRONIC_WIRE
                    )
                    .maxConnections(8)
                    .shouldCustomRenderItem(true)
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock<OverheadLineSupportBlockEntity>,OverheadLineSupportBlockEntity> OVERHEAD_LINE_INSULATOR_A =
            new OverheadLineSupportBlockReg<>("overhead_line_insulator_a")
                    .blockType(OverheadLineSupportInsulatorBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .withRenderer(()-> OverheadLineInsulatorRenderer.ARenderer::new)
                    .withConnectionPointBuilder(OverheadLineConnectionPoints::getInsulatorAConnectionPointIf)
                    .addAllowedWireType(
                            AllWires.ELECTRONIC_WIRE
                    )
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineSupportBlock<OverheadLineSupportBlockEntity>,OverheadLineSupportBlockEntity> OVERHEAD_LINE_INSULATOR_B =
            new OverheadLineSupportBlockReg<>("overhead_line_insulator_b")
                    .blockType(OverheadLineSupportInsulatorBlock::new)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .withRenderer(()-> OverheadLineInsulatorRenderer.BRenderer::new)
                    .withConnectionPointBuilder(OverheadLineConnectionPoints::getInsulatorBConnectionPointIf)
                    .addAllowedWireType(
                            AllWires.ELECTRONIC_WIRE
                    )
                    .submit(AllElements.testRegistry);

    public static OverheadLineSupportBlockReg<OverheadLineEndWeightBlock, OverheadLineEndWeightBlockEntity> OVERHEAD_LINE_END_WEIGHT =
            new OverheadLineSupportBlockReg<OverheadLineEndWeightBlock, OverheadLineEndWeightBlockEntity>("overhead_line_end_weight")
                    .blockType(OverheadLineEndWeightBlock::new)
                    .withBlockEntity(OVERHEAD_LINE_END_WEIGHT_BLOCK_ENTITY)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .withRenderer(()-> OverheadLineEndCounterWeightRenderer::new)
                    .addAllowedWireType(
                            AllWires.OVERHEAD_LINE_WIRE,
                            AllWires.ELECTRONIC_WIRE
                    )
                    .maxConnections(1)
                    .withConnectionPointBuilder(OverheadLineConnectionPoints::getEndCounterWeightConnectionPointIf)
                    .submit(AllElements.testRegistry);
    public static void invoke(){

    }
}
