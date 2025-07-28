package willow.train.kuayue.initial.panel;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.Vec2;
import willow.train.kuayue.block.panels.TrainPanelBlock;
import willow.train.kuayue.block.panels.slab.TrainLadderBlock;
import willow.train.kuayue.block.panels.slab.TrainSlabBlock;
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

    public static final SlabRegistration<TrainLadderBlock> C22_LADDER =
            new SlabRegistration<TrainLadderBlock>("22_ladder")
                    .block((properties) -> new TrainLadderBlock(properties, false))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLUE)
                    .tab(AllElements.neoKuayueCarriageTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static void invoke(){}
}
