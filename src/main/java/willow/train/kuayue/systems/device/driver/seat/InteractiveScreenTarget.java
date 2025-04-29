package willow.train.kuayue.systems.device.driver.seat;

import kasuga.lib.core.client.frontend.gui.GuiContext;
import kasuga.lib.core.client.frontend.gui.GuiInstance;
import kasuga.lib.core.client.frontend.gui.SourceInfo;
import kasuga.lib.core.client.frontend.gui.events.mouse.MouseEvent;
import kasuga.lib.core.client.frontend.rendering.RenderContext;
import kasuga.lib.core.menu.base.BindingClient;
import kasuga.lib.core.menu.base.GuiMenu;
import kasuga.lib.core.menu.targets.Target;
import kasuga.lib.core.menu.targets.WorldRendererTarget;

import java.util.Optional;

public class InteractiveScreenTarget {
    private final GuiInstance instance;

    public static void init(){
        BindingClient.registerBinding(GuiTargets.INTERACTIVE, InteractiveScreenTarget::create);
    }

    private static InteractiveScreenTarget create(GuiInstance instance) {
        return new InteractiveScreenTarget(instance);
    }

    public InteractiveScreenTarget(GuiInstance instance) {
        this.instance = instance;
    }

    public void dispatchMouseEvent(Object source, MouseEvent event) {
        instance.getContext().ifPresent((context)->{
            context.appendTask(()->{
                context.getRootNode().onMouseEvent(source, event);
            });
        });
    }

    public void render(RenderContext context) {
        this.instance.beforeRender();
        this.instance.getContext().ifPresent((guiContext) -> {
            guiContext.render(context.source, context);
        });
        this.instance.afterRender();
    }

    public void attach() {
        this.instance.open(InteractiveScreenTarget.class);
    }

    public void detach() {
        this.instance.close(InteractiveScreenTarget.class);
    }

    public static void attach(GuiMenu menu) {
        (menu.getBinding().apply(Target.WORLD_RENDERER)).attach();
    }

    public static void detach(GuiMenu menu) {
        (menu.getBinding().apply(Target.WORLD_RENDERER)).detach();
    }

    public void updateSizeInfo(Object source, SourceInfo sourceInfo) {
        instance.updateSourceInfo(source, sourceInfo);
    }
}
