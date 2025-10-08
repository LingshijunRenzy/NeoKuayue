package willow.train.kuayue.systems.device.driver.combustion;

import kasuga.lib.core.create.device.TrainDeviceLocation;
import kasuga.lib.core.menu.base.GuiBinding;
import kasuga.lib.core.menu.base.GuiMenu;
import kasuga.lib.core.menu.base.GuiMenuType;
import kasuga.lib.core.menu.javascript.JavascriptMenu;
import kasuga.lib.core.menu.targets.Target;
import kasuga.lib.core.menu.targets.WorldRendererTarget;
import net.minecraft.resources.ResourceLocation;
import willow.train.kuayue.initial.AllElements;
import willow.train.kuayue.systems.device.driver.devices.power.PowerSystem;
import willow.train.kuayue.initial.AllElements;

import java.util.UUID;

public class InternalCombustionDrivingMenu extends JavascriptMenu {

    public InternalCombustionDrivingMenu(GuiMenuType<?> type) {
        super(type);
    }

    @Override
    protected ResourceLocation getServerScriptLocation() {
        return AllElements.testRegistry.asResource("combustion_train_control");
    }

    @Override
    protected GuiBinding createBinding(UUID id) {
        return
                new GuiBinding(id)
                        .execute(AllElements.testRegistry.asResource("combustion_train_control"));
    }

    public void providePower(PowerSystem power, TrainDeviceLocation controller) {
        provide("power", power);
    }
}
