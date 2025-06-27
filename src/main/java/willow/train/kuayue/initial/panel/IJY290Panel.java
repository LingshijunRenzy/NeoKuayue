package willow.train.kuayue.initial.panel;

import kasuga.lib.registrations.common.BlockReg;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import willow.train.kuayue.block.panels.FullShapeDirectionalBlock;
import willow.train.kuayue.block.panels.slab.TrainSlabBlock;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.initial.registration.SlabRegistration;

public class IJY290Panel {

    public static final BlockReg<FullShapeDirectionalBlock> JY290_HEAD_2 =
            new BlockReg<FullShapeDirectionalBlock>("jy290_head_2")
                    .blockType(FullShapeDirectionalBlock::new)
                    .material(Material.METAL).materialColor(MaterialColor.COLOR_BLACK)
                    .addProperty(BlockBehaviour.Properties::noOcclusion)
                    .addProperty(properties -> properties.strength(1.5f, 3f))
                    .addProperty(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueLocoTab)
                    .submit(AllElements.testRegistry);

    public static final BlockReg<FullShapeDirectionalBlock> JY290_HEAD_1 =
            new BlockReg<FullShapeDirectionalBlock>("jy290_head_1")
                    .blockType(FullShapeDirectionalBlock::new)
                    .material(Material.METAL).materialColor(MaterialColor.COLOR_BLACK)
                    .addProperty(BlockBehaviour.Properties::noOcclusion)
                    .addProperty(properties -> properties.strength(1.5f, 3f))
                    .addProperty(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .defaultBlockItem()
                    .tabTo(AllElements.neoKuayueLocoTab)
                    .submit(AllElements.testRegistry);

    public static final BlockReg<FullShapeDirectionalBlock> JY290_COWCATCHER =
            new BlockReg<FullShapeDirectionalBlock>("jy290_cowcatcher")
                    .blockType(FullShapeDirectionalBlock::new)
                    .material(Material.METAL).materialColor(MaterialColor.COLOR_BLACK)
                    .addProperty(properties -> properties.strength(1.5f, 3f))
                    .addProperty(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .defaultBlockItem()
                    .addProperty(BlockBehaviour.Properties::noOcclusion)
                    .tabTo(AllElements.neoKuayueLocoTab)
                    .submit(AllElements.testRegistry);

    public static final SlabRegistration<TrainSlabBlock> JY290_FLOOR =
            new SlabRegistration<TrainSlabBlock>("jy290_floor")
                    .block(p -> new TrainSlabBlock(p, false))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLACK)
                    .tab(AllElements.neoKuayueLocoTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static final SlabRegistration<TrainSlabBlock> JY290_FLOOR_LADDER =
            new SlabRegistration<TrainSlabBlock>("jy290_floor_ladder")
                    .block(p -> new TrainSlabBlock(p, false))
                    .materialAndColor(Material.METAL, MaterialColor.COLOR_BLACK)
                    .tab(AllElements.neoKuayueLocoTab)
                    .noOcclusion().strengthAndTool(1.5f, 3f)
                    .submit(AllElements.testRegistry);

    public static void invoke(){}
}
