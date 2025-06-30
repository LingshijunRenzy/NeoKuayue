package willow.train.kuayue.systems.overhead_line.block.decorating;

import kasuga.lib.registrations.common.BlockReg;
import net.minecraft.world.level.material.Material;
import willow.train.kuayue.initial.AllElements;

public class AllOverheadLineDecoratingBlocks {
    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_CONCRETE_PUNCHED_POST =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_concrete_punched_post")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_FRAME =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_frame")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_HORIZONTAL_ROUNDED_PILLAR =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_horizontal_rounded_pillar")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_HORIZONTAL_ROUNDED_PILLAR_CONNECTOR =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_horizontal_rounded_pillar_connector")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_HORIZONTAL_ROUNDED_PILLAR_HANGER_DOUBLE_TOP =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_horizontal_rounded_pillar_hanger_double_top")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_HORIZONTAL_SLANT_ROUNDED_PILLAR =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_horizontal_slant_rounded_pillar")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_HORIZONTAL_SLANT_ROUNDED_PILLAR_NOSUPPORT =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_horizontal_slant_rounded_pillar_nosupport")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_ROUNDED_PILLAR =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_rounded_pillar")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_SINGLE_LAYER_TRUSS =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_single_layer_truss")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_SINGLE_LAYER_TRUSS_CONNECTOR =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_single_layer_truss_connector")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_SINGLE_LAYER_TRUSS_REVERSAL =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_single_layer_truss_reversal")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_SINGLE_LAYER_TRUSS_Z =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_single_layer_truss_z")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_STEEL_BEAM =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_steel_beam")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_STEEL_FRAME_PILLAR =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_steel_frame_pillar")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_TRIANGLE_BEAM_HANGER =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_triangle_beam_hanger")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_TRIANGLE_BEAM_HANGER_DOUBLE =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_triangle_beam_hanger_double")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_TRIANGLE_BEAM_HANGER_DOUBLE_TOP =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_triangle_beam_hanger_double_top")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_TRIANGLE_BEAM_HANGER_TOP =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_triangle_beam_hanger_top")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_TRIANGLE_CROSSBEAM =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_triangle_crossbeam")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_TRIANGLE_CROSSBEAM_CONNECTOR =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_triangle_crossbeam_connector")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_TRUSS_COLUMN =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_truss_column")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_TRUSS_CONNECTOR_LARGE =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_truss_connector_large")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_TRUSS_HANGER =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_truss_hanger")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_TRUSS_HANGER_TOP =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_truss_hanger_top")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static BlockReg<SimpleOverheadLineDecoratingBlock> OVERHEAD_TRUSS_LARGE =
            new BlockReg<SimpleOverheadLineDecoratingBlock>("overhead_truss_large")
                    .blockType(SimpleOverheadLineDecoratingBlock.Builder.create(
                            SimpleOverheadLineDecoratingBlock.defaultProperties().directional()
                    ))
                    .material(Material.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);


    public static void invoke(){}
}
