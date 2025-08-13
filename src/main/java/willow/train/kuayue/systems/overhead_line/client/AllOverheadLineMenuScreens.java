package willow.train.kuayue.systems.overhead_line.client;

import kasuga.lib.registrations.common.MenuReg;
import willow.train.kuayue.initial.AllElements;

public class AllOverheadLineMenuScreens {

    public static final MenuReg<OverheadLineSupportAdjustMenu, OverheadLineSupportAdjustScreen> OVERHEAD_LINE_SUPPORT_ADJUST =
            new MenuReg<OverheadLineSupportAdjustMenu, OverheadLineSupportAdjustScreen>("overhead_line_support_adjust")
                    .withMenuAndScreen(OverheadLineSupportAdjustMenu::new, () -> OverheadLineSupportAdjustScreen::new)
                    .submit(AllElements.testRegistry);

    public static void invoke(){}
}
