package willow.train.kuayue.block.panels.conductor;

import com.jozufozu.flywheel.core.PartialModel;
import lombok.NonNull;
import net.minecraft.resources.ResourceLocation;
import willow.train.kuayue.block.panels.base.TrainPanelProperties;
import willow.train.kuayue.block.panels.end_face.CustomRenderedEndfaceBlock;
import willow.train.kuayue.initial.AllConductorTypes;
import willow.train.kuayue.systems.train_extension.conductor.Conductable;
import willow.train.kuayue.systems.train_extension.conductor.ConductorProvider;
import willow.train.kuayue.systems.train_extension.conductor.ConductorType;

public class DF11GEndFaceBlock extends CustomRenderedEndfaceBlock implements ConductorProvider {
    public DF11GEndFaceBlock(Properties pProperties, TrainPanelProperties.DoorType doorType, PartialModel leftModel, PartialModel rightModel, PartialModel frameModel) {
        super(pProperties, doorType, leftModel, rightModel, frameModel);
    }

    public DF11GEndFaceBlock(Properties prop, TrainPanelProperties.DoorType doorType, ResourceLocation left, ResourceLocation right, ResourceLocation frame) {
        super(prop, doorType, left, right, frame);
    }

    public DF11GEndFaceBlock(Properties properties, TrainPanelProperties.DoorType doorType, String leftModel, String rightModel, String frameModel) {
        super(properties, doorType, leftModel, rightModel, frameModel);
    }

    @Override
    public @NonNull ConductorType getType() {
        return AllConductorTypes.DUMMY;
    }

    @Override
    public @NonNull Conductable modifyConductor(@NonNull Conductable rawConductor) {
        rawConductor.setOffset(1);
        return rawConductor;
    }
}
