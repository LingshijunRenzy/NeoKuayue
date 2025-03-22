package willow.train.kuayue.event.both;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;

public class OnFinalizeSetup {
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        OverheadLineSupportBlockEntity.applyRegistration();
    }
}
