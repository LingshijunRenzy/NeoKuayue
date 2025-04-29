package willow.train.kuayue.systems.device.driver.seat;

import kasuga.lib.core.menu.base.GuiBindingTarget;

public class GuiTargets<T> implements GuiBindingTarget<T> {
    public static final GuiTargets<InteractiveScreenTarget> INTERACTIVE = new GuiTargets<>();
    public static final GuiTargets<WorldTrainSoundManager> SOUND = new GuiTargets<>();
}
