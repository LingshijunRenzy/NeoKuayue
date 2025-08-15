package willow.train.kuayue.initial.panel;

import com.simibubi.create.foundation.utility.Couple;
import kasuga.lib.registrations.common.BlockReg;
import kasuga.lib.registrations.create.MovementReg;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.block.panels.FullShapeDirectionalBlock;
import willow.train.kuayue.block.panels.TrainHingePanelBlock;
import willow.train.kuayue.block.panels.TrainPanelBlock;
import willow.train.kuayue.block.panels.door.CustomRenderedDoorBlock;
import willow.train.kuayue.block.panels.pantograph.PantographMovementBehaviour;
import willow.train.kuayue.block.panels.pantograph.PantographProps;
import willow.train.kuayue.block.panels.pantograph.SingleArmPantographBlock;
import willow.train.kuayue.block.panels.slab.HeightSlabBlock;
import willow.train.kuayue.block.panels.slab.TrainSlabBlock;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.initial.registration.PanelRegistration;
import willow.train.kuayue.initial.registration.SlabRegistration;

public class I3DPanel {

    public static final PanelRegistration<CustomRenderedDoorBlock> DOOR_CABIN_HXD3D =
            new PanelRegistration<CustomRenderedDoorBlock>("door_cabin_hxd3d")
                    .block(p -> new CustomRenderedDoorBlock(p,
                            Couple.create(
                                    AllElements.testRegistry.asResource("hxd3d/door/door_bottom_hxd3d_left"),
                                    AllElements.testRegistry.asResource("hxd3d/door/door_upper_hxd3d_left")
                            ), Couple.create(
                            AllElements.testRegistry.asResource("hxd3d/door/door_bottom_hxd3d_right"),
                            AllElements.testRegistry.asResource("hxd3d/door/door_upper_hxd3d_right")
                    ), new Vec3(-.3125, .25, 0), new Vec3(0, 0, 0),RenderShape.MODEL, false
                    ))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_GREEN)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .tab(AllElements.neoKuayueLocoTab)
                    .submit(AllElements.testRegistry);

//    public static final SlabRegistration<TrainSlabBlock> CARPORT_GENERAL_HXD3D =
//            new SlabRegistration<TrainSlabBlock>("carport_general_hxd3d")
//                    .block(p -> new TrainSlabBlock(p, true))
//                    .materialAndColor(Material.METAL, MaterialColor.COLOR_RED)
//                    .tab(AllElements.neoKuayueLocoTab)
//                    .noOcclusion().strengthAndTool(1.5f, 3f)
//                    .submit(AllElements.testRegistry);

    public static final BlockReg<HeightSlabBlock> FLOOR_HXD3D =
            new BlockReg<HeightSlabBlock>("floor_hxd3d")
                    .blockType(HeightSlabBlock::new)
                    .material(Material.METAL)
                    .materialColor(MaterialColor.COLOR_RED)
                    .addProperty(BlockBehaviour.Properties::noOcclusion)
                    .addProperty(properties -> properties.strength(1.5f, 3f))
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueLocoTab)
                    .submit(AllElements.testRegistry);

//    public static final BlockReg<SlabBlock> SLAB_HXD3D =
//            new BlockReg<SlabBlock>("floor_hxd3d")
//                    .blockType(SlabBlock::new)
//                    .material(Material.METAL)
//                    .materialColor(MaterialColor.COLOR_RED)
//                    .addProperty(BlockBehaviour.Properties::noOcclusion)
//                    .addProperty(properties -> properties.strength(1.5f, 3f))
//                    .defaultBlockItem()
//                    .tabTo(AllElements.neoKuayueLocoTab)
//                    .submit(AllElements.testRegistry);
    public static final PanelRegistration<TrainPanelBlock> PANEL_HEXIE_HXD3D =
            new PanelRegistration<TrainPanelBlock>("panel_hexie_hxd3d")
                    .block(p -> new TrainPanelBlock(p, new Vec2(-1, 0), new Vec2(2, 2)))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_RED)
                    .tab(AllElements.neoKuayueLocoTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

//    public static final PanelRegistration<TrainPanelBlock> PANEL_RED_HXD3D =
//            new PanelRegistration<TrainPanelBlock>("panel_red_hxd3d")
//                    .block(p -> new TrainPanelBlock(p, new Vec2(0, 0), new Vec2(1, 1)))
//                    .materialAndColor(Material.METAL, MaterialColor.COLOR_RED)
//                    .tab(AllElements.neoKuayueLocoTab)
//                    .noOcclusion().strengthAndTool(1.5f, 3f)
//                    .submit(AllElements.testRegistry);

    public static final BlockReg<FullShapeDirectionalBlock> HEAD_HXD3D =
            new BlockReg<FullShapeDirectionalBlock>("head_hxd3d")
                    .blockType(FullShapeDirectionalBlock::new)
                    .material(Material.METAL).materialColor(MaterialColor.COLOR_BLACK)
                    .addProperty(BlockBehaviour.Properties::noOcclusion)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueLocoTab)
                    .submit(AllElements.testRegistry);

    public static final BlockReg<FullShapeDirectionalBlock> HXD3D_COWCATCHER =
            new BlockReg<FullShapeDirectionalBlock>("hxd3d_cowcatcher")
                    .blockType(FullShapeDirectionalBlock::new)
                    .material(Material.METAL).materialColor(MaterialColor.COLOR_BLACK)
                    .addProperty(properties -> properties.strength(1.5f, 3f))
                    .addProperty(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .defaultBlockItem()
                    .addProperty(BlockBehaviour.Properties::noOcclusion)
                    .tabTo(AllElements.neoKuayueLocoTab)
                    .submit(AllElements.testRegistry);

    public static final SlabRegistration<TrainSlabBlock> HXD3D_CARPORT =
            new SlabRegistration<TrainSlabBlock>("hxd3d_carport")
                    .block(p -> new TrainSlabBlock(p, true))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_RED)
                    .tab(AllElements.neoKuayueLocoTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final SlabRegistration<TrainSlabBlock> HXD3D_CARPORT_CENTRE =
            new SlabRegistration<TrainSlabBlock>("hxd3d_carport_center")
                    .block(p -> new TrainSlabBlock(p, true))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_RED)
                    .tab(AllElements.neoKuayueLocoTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<TrainPanelBlock> HXD3D_PANEL_BOTTOM =
            new PanelRegistration<TrainPanelBlock>("hxd3d_panel_bottom")
                    .block(p -> new TrainPanelBlock(p, new Vec2(0, 0), new Vec2(1, 1)))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_RED)
                    .tab(AllElements.neoKuayueLocoTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<TrainPanelBlock> PANEL_LOGO_NUM_HXD3D =
            new PanelRegistration<TrainPanelBlock>("panel_logo_num_hxd3d")
                    .block(p -> new TrainPanelBlock(p, new Vec2(-1, 0), new Vec2(2, 2)))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_RED)
                    .tab(AllElements.neoKuayueLocoTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<TrainHingePanelBlock> HXD3D_PANEL_TRANSIT =
            new PanelRegistration<TrainHingePanelBlock>("hxd3d_panel_transit")
                    .block(p -> new TrainHingePanelBlock(p, new Vec2(0, 0), new Vec2(1, 2)))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_RED)
                    .tab(AllElements.neoKuayueLocoTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<TrainHingePanelBlock> HXD3D_PANEL_TRANSIT_2 =
            new PanelRegistration<TrainHingePanelBlock>("hxd3d_panel_transit_2")
                    .block(p -> new TrainHingePanelBlock(p, new Vec2(0, 0), new Vec2(1, 1)))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_RED)
                    .tab(AllElements.neoKuayueLocoTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<TrainPanelBlock> HXD3D_PANEL_MIDDLE =
            new PanelRegistration<TrainPanelBlock>("hxd3d_panel_middle")
                    .block(p -> new TrainPanelBlock(p, new Vec2(0, 0), new Vec2(1, 1)))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_RED)
                    .tab(AllElements.neoKuayueLocoTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final SlabRegistration<TrainSlabBlock> HXD3D_UNDERBODY_EQUIP =
            new SlabRegistration<TrainSlabBlock>("hxd3d_underbody_equip")
                    .block(p -> new TrainSlabBlock(p, false))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueLocoTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final BlockReg<SingleArmPantographBlock> HXD3D_PANTOGRAPH =
            new BlockReg<SingleArmPantographBlock>("hxd3d_pantograph")
                    .blockType(props -> new SingleArmPantographBlock(props,
                            new PantographProps(
                                    8.8, 25.44,
                                    19.52, 3.130,
                                    161.0, 25.28),
                            "hxd3d/pantograph/hxd3d_panto_base",
                            "hxd3d/pantograph/hxd3d_panto_large_arm",
                            "hxd3d/pantograph/hxd3d_panto_pull_rod",
                            "hxd3d/pantograph/hxd3d_panto_small_arm",
                            "hxd3d/pantograph/hxd3d_panto_bow_head",
                            1.5f, 170, 130
                            ))
                    .material(Material.METAL).materialColor(MaterialColor.COLOR_BLACK)
                    .addProperty(properties -> properties.strength(1.5f, 3f))
                    .addProperty(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .defaultBlockItem()
                    .addProperty(BlockBehaviour.Properties::noOcclusion)
                    .tabTo(AllElements.neoKuayueLocoTab)
                    .submit(AllElements.testRegistry);

    public static MovementReg<PantographMovementBehaviour> PANTOGRAPH_MOVEMENT =
            new MovementReg<PantographMovementBehaviour>("pantograph_movement")
                    .behaviour(new PantographMovementBehaviour())
                    .sortByBlocks(HXD3D_PANTOGRAPH)
                    .submit(AllElements.testRegistry);

    public static void invoke(){}
}
