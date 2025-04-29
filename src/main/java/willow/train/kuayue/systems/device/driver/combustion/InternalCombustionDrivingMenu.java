package willow.train.kuayue.systems.device.driver.combustion;

import kasuga.lib.core.menu.base.GuiBinding;
import kasuga.lib.core.menu.base.GuiMenu;
import kasuga.lib.core.menu.base.GuiMenuType;
import kasuga.lib.core.menu.targets.Target;
import kasuga.lib.core.menu.targets.WorldRendererTarget;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class InternalCombustionDrivingMenu extends GuiMenu {

    public InternalCombustionDrivingMenu(GuiMenuType<?> type) {
        super(type);
    }

    @Override
    protected GuiBinding createBinding(UUID id) {
        return
                new GuiBinding(id)
                        .execute(ResourceLocation.tryParse("kuayue:train_control"));
    }
}
