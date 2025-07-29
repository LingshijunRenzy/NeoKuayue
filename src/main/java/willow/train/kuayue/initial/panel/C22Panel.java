package willow.train.kuayue.initial.panel;

import com.simibubi.create.foundation.utility.Couple;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.block.panels.TrainPanelBlock;
import willow.train.kuayue.block.panels.door.CustomRenderedDoorBlock;
import willow.train.kuayue.block.panels.slab.TrainLadderBlock;
import willow.train.kuayue.block.panels.slab.TrainSlabBlock;
import willow.train.kuayue.block.panels.window.TrainOpenableWindowBlock;
import willow.train.kuayue.block.panels.window.TrainSmallWindowBlock;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.initial.registration.PanelRegistration;
import willow.train.kuayue.initial.registration.SlabRegistration;

public class C22Panel {

    public static final SlabRegistration<TrainSlabBlock> C22_FLOOR =
            new SlabRegistration<TrainSlabBlock>("22_floor")
                    .block(p -> new TrainSlabBlock(p, false))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueCarriageTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final SlabRegistration<TrainSlabBlock> C22_FLOOR_CENTER =
            new SlabRegistration<TrainSlabBlock>("22_floor_center")
                    .block(p -> new TrainSlabBlock(p, false))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueCarriageTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<TrainPanelBlock> C22_PANEL_BOTTOM =
            new PanelRegistration<TrainPanelBlock>("22_panel_bottom")
                    .block(p -> new TrainPanelBlock(p, new Vec2(0, 0), new Vec2(1, 1)))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueCarriageTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<TrainPanelBlock> C22_PANEL_MIDDLE =
            new PanelRegistration<TrainPanelBlock>("22_panel_middle")
                    .block(p -> new TrainPanelBlock(p, new Vec2(0, 0), new Vec2(1, 1)))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueCarriageTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final SlabRegistration<TrainLadderBlock> C22_LADDER =
            new SlabRegistration<TrainLadderBlock>("22_ladder")
                    .block((properties) -> new TrainLadderBlock(properties, false))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueCarriageTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<TrainSmallWindowBlock> C22_WINDOW =
            new PanelRegistration<TrainSmallWindowBlock>("22_window")
                    .block(p -> new TrainSmallWindowBlock(p, new Vec2(0, 0), new Vec2(1, 1)))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueCarriageTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<TrainOpenableWindowBlock> C22_LARGE_WINDOW =
            new PanelRegistration<TrainOpenableWindowBlock>("22_large_window")
                    .block(p -> new TrainOpenableWindowBlock(p, 2))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueCarriageTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<TrainOpenableWindowBlock> C22_DOUBLE_SMALL_WINDOW =
            new PanelRegistration<TrainOpenableWindowBlock>("22_double_small_window")
                    .block(p -> new TrainOpenableWindowBlock(p, 2))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueCarriageTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<TrainOpenableWindowBlock> C22_SMALL_WINDOW =
            new PanelRegistration<TrainOpenableWindowBlock>("22_small_window")
                    .block(p -> new TrainOpenableWindowBlock(p, 1))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueCarriageTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final PanelRegistration<CustomRenderedDoorBlock> DOOR_22 =
            new PanelRegistration<CustomRenderedDoorBlock>("22_door")
                    .block(p -> new CustomRenderedDoorBlock(p,
                            Couple.create(
                                    AllElements.testRegistry.asResource("carriage/carriage_22/door/22_door_bottom_lh"),
                                    AllElements.testRegistry.asResource("carriage/carriage_22/door/22_door_upper_lh")
                            ), Couple.create(
                            AllElements.testRegistry.asResource("carriage/carriage_22/door/22_door_bottom"),
                            AllElements.testRegistry.asResource("carriage/carriage_22/door/22_door_upper")
                    ), new Vec3(0, 0, 0),new Vec3(0, 0, 0), RenderShape.MODEL, false
                    ))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .tab(AllElements.neoKuayueCarriageTab)
                    .submit(AllElements.testRegistry);

    public static void invoke(){}
}
