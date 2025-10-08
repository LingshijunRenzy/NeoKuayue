package willow.train.kuayue.systems.overhead_line.block.decorating;

import kasuga.lib.registrations.common.BlockReg;
import net.minecraft.world.level.material.MapColor;
import willow.train.kuayue.initial.AllElements;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;

import willow.train.kuayue.initial.registration.PanelRegistration;

public class AllOverheadLineDecoratingBlocks {
 public static final BlockReg<SimpleOverheadLinePillarBlock> OVERHEAD_PILLAR_CONCRETE=
            new BlockReg<SimpleOverheadLinePillarBlock>("overhead_pillar_concrete")
                    .blockType(SimpleOverheadLinePillarBlock.Builder.create(
                            SimpleOverheadLinePillarBlock.defaultProperties()
                    ))
                    .materialColor(MapColor.METAL)
                    .materialColor(MapColor.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);
    public static final BlockReg<SimpleOverheadLinePillarBlock> OVERHEAD_PILLAR_STEEL=
            new BlockReg<SimpleOverheadLinePillarBlock>("overhead_pillar_steel")
                    .blockType(SimpleOverheadLinePillarBlock.Builder.create(
                            SimpleOverheadLinePillarBlock.defaultProperties()
                    ))
                    .materialColor(MapColor.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);
    public static final BlockReg<SimpleOverheadLinePillarBlock> OVERHEAD_SQUARE_PILLAR_CONCRETE_PUNCHING=
            new BlockReg<SimpleOverheadLinePillarBlock>("overhead_square_pillar_concrete_punching")
                    .blockType(SimpleOverheadLinePillarBlock.Builder.create(
                            SimpleOverheadLinePillarBlock.defaultProperties()
                                    .directional()
                    ))
                    .materialColor(MapColor.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static final BlockReg<SimpleOverheadLinePillarBlock> OVERHEAD_SQUARE_PILLAR_STEEL=
            new BlockReg<SimpleOverheadLinePillarBlock>("overhead_square_pillar_steel")
                    .blockType(SimpleOverheadLinePillarBlock.Builder.create(
                            SimpleOverheadLinePillarBlock.defaultProperties()
                                    .directional()
                    ))
                    .materialColor(MapColor.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<OverheadTruss> OVERHEAD_TRUSS_DOUBLE =
            new PanelRegistration<OverheadTruss>("overhead_truss_double")
                    .block(p -> new OverheadTruss(p, new Vec3(0,0,1), new Vec3(1,1,1)))
                    .materialAndColor(MapColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueGridTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<OverheadTruss> OVERHEAD_TRUSS_DOUBLE_AD =
            new PanelRegistration<OverheadTruss>("overhead_truss_double_ad")
                    .block(p -> new OverheadTruss(p, new Vec3(0,0,-1), new Vec3(1,1,1)))
                    .materialAndColor(MapColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueGridTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<OverheadTrussSmall> OVERHEAD_TRUSS_SINGLE_AD =
            new PanelRegistration<OverheadTrussSmall>("overhead_truss_single_ad")
                    .block(p -> new OverheadTrussSmall(p, new Vec3(0,0,1), new Vec3(1,1,1)))
                    .materialAndColor(MapColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueGridTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<OverheadTrussSmall> OVERHEAD_TRUSS_SINGLE_A =
            new PanelRegistration<OverheadTrussSmall>("overhead_truss_single_a")
                    .block(p -> new OverheadTrussSmall(p, new Vec3(0,0,1), new Vec3(1,1,1)))
                    .materialAndColor(MapColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueGridTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<OverheadTrussSmall> OVERHEAD_TRUSS_SINGLE_B =
            new PanelRegistration<OverheadTrussSmall>("overhead_truss_single_b")
                    .block(p -> new OverheadTrussSmall(p, new Vec3(0,0,1), new Vec3(1,1,1)))
                    .materialAndColor(MapColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueGridTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<OverheadTrussSmall> OVERHEAD_TRUSS_SINGLE_C =
            new PanelRegistration<OverheadTrussSmall>("overhead_truss_single_c")
                    .block(p -> new OverheadTrussSmall(p, new Vec3(0,0,1), new Vec3(1,1,1)))
                    .materialAndColor(MapColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueGridTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);



    public static final PanelRegistration<OverheadTrussPillar> OVERHEAD_TRUSS_CONCRETE =
            new PanelRegistration<OverheadTrussPillar>("overhead_truss_concrete")
                    .block(p -> new OverheadTrussPillar(p, new Vec3(0,0,1), new Vec3(1,1,1)))
                    .materialAndColor(MapColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueGridTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);


    public static final PanelRegistration<OverheadTrussPillar> OVERHEAD_PILLAR_CONCRETE_TRUSS_A =
            new PanelRegistration<OverheadTrussPillar>("overhead_pillar_concrete_truss_a")
                    .block(p -> new OverheadTrussPillar(p, new Vec3(0,0,-2), new Vec3(1,1,1)))
                    .materialAndColor(MapColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueGridTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);


    public static final PanelRegistration<OverheadTrussPillar> OVERHEAD_PILLAR_CONCRETE_TRUSS_B =
            new PanelRegistration<OverheadTrussPillar>("overhead_pillar_concrete_truss_b")
                    .block(p -> new OverheadTrussPillar(p, new Vec3(0,0,-2), new Vec3(1,1,1)))
                    .materialAndColor(MapColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueGridTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<OverheadTrussPillar> OVERHEAD_TRIANGLE_TRUSS_STEEL =
            new PanelRegistration<OverheadTrussPillar>("overhead_triangle_truss_steel")
                    .block(p -> new OverheadTrussPillar(p, new Vec3(0,0,0), new Vec3(1,1,1)))
                    .materialAndColor(MapColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueGridTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);
    public static final PanelRegistration<OverheadTrussPillar> OVERHEAD_TRIANGLE_TRUSS_STEEL_AD =
            new PanelRegistration<OverheadTrussPillar>("overhead_triangle_truss_steel_ad")
                    .block(p -> new OverheadTrussPillar(p, new Vec3(0,0,0), new Vec3(1,1,1)))
                    .materialAndColor(MapColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueGridTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);


    public static final BlockReg<LinkedTrussBlock> TRUSS_HANGER_SINGLE =
            new BlockReg<LinkedTrussBlock>("truss_hanger_single")
                    .blockType(LinkedTrussBlock.Builder.create(
                            new LinkedTrussBlock.SpecialBlockConfig()
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_truss_single_a"),
                                            LinkedTrussBlock.ConnectedType.SINGLE
                                    )
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_truss_single_b"),
                                            LinkedTrussBlock.ConnectedType.SINGLE
                                    )
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_truss_single_c"),
                                            LinkedTrussBlock.ConnectedType.SINGLE
                                    )
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_truss_double"),
                                            LinkedTrussBlock.ConnectedType.DOUBLE
                                    )
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_truss_concrete"),
                                            LinkedTrussBlock.ConnectedType.CONCRETE
                                    )
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_triangle_truss_steel"),
                                            LinkedTrussBlock.ConnectedType.TRIANGLE
                                    )
                    ))
                    .materialColor(MapColor.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);

    public static final BlockReg<LinkedTrussBlock> TRUSS_HANGER_DOUBLE =
            new BlockReg<LinkedTrussBlock>("truss_hanger_double")
                    .blockType(LinkedTrussBlock.Builder.create(
                            new LinkedTrussBlock.SpecialBlockConfig()
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_truss_single_a"),
                                            LinkedTrussBlock.ConnectedType.SINGLE
                                    )
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_truss_single_b"),
                                            LinkedTrussBlock.ConnectedType.SINGLE
                                    )
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_truss_single_c"),
                                            LinkedTrussBlock.ConnectedType.SINGLE
                                    )
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_truss_double"),
                                            LinkedTrussBlock.ConnectedType.DOUBLE
                                    )
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_truss_concrete"),
                                            LinkedTrussBlock.ConnectedType.CONCRETE
                                    )
                                    .addSpecialBlock(
                                            new ResourceLocation("kuayue", "overhead_triangle_truss_steel"),
                                            LinkedTrussBlock.ConnectedType.TRIANGLE
                                    )
                    ))
                    .materialColor(MapColor.METAL)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueGridTab)
                    .submit(AllElements.testRegistry);


    public static void invoke(){}
}
