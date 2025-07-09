package willow.train.kuayue.systems.device.driver.seat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import kasuga.lib.core.client.frontend.common.layouting.LayoutBox;
import kasuga.lib.core.client.frontend.gui.SourceInfo;
import kasuga.lib.core.client.frontend.gui.events.mouse.*;
import kasuga.lib.core.client.frontend.rendering.RenderContext;
import kasuga.lib.core.client.interaction.GuiOperatingPerspectiveScreen;
import kasuga.lib.core.menu.targets.Target;
import kasuga.lib.core.menu.targets.WorldRendererTarget;
import kasuga.lib.core.util.data_type.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.c2s.OnSeatActionPacket;
import willow.train.kuayue.network.c2s.SeatDismountPacket;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InteractiveDriveScreen extends GuiOperatingPerspectiveScreen {
    private final Contraption contraption;
    private final AbstractContraptionEntity entity;

    private final HashMap<InteractiveBehaviour.MenuEntry, MenuProp> props = new HashMap<InteractiveBehaviour.MenuEntry, MenuProp>();

    private final HashMap<InteractiveBehaviour.MenuEntry, InteractiveScreenTarget> interactiveTargets = new HashMap<>();

    private final List<InteractiveBehaviour.MenuEntry> interactiveSet = new ArrayList<>();

    Vec2 lastClickedPos = null;

    public InteractiveDriveScreen(
            Contraption contraption,
            AbstractContraptionEntity entity
    ) {
        super();
        this.contraption = contraption;
        this.entity = entity;
        contraption.forEachActor(contraption.entity.level, (behaviour, context) -> {
            if(behaviour instanceof InteractiveBehaviour interactiveBehaviour){
                interactiveSet.addAll(interactiveBehaviour.getMenusOf(context));
            }
        });

        interactiveSet.forEach(entry->{
            InteractiveScreenTarget target = entry.menuSupplier().get().getBinding().apply(GuiTargets.INTERACTIVE);
            interactiveTargets.put(entry, target);
            target.attach();
        });
    }

    @Override
    protected void init() {
        super.init();
        interactiveTargets.forEach(((entry, target) -> {
            target.updateSizeInfo(InteractiveScreenTarget.class, new SourceInfo(LayoutBox.of(
                    0,
                    0,
                    entry.getSize().x,
                    entry.getSize().y
            )));
        }));
    }

    int lazyTickRate = 0;

    @Override
    public void tick() {
        if(lazyTickRate ++ > 20) {
            lazyTickRate = 0;

        }
        if(
                contraption.disassembled ||
                entity.isRemoved() ||
                Minecraft.getInstance().player == null ||
                !entity.hasPassenger(Minecraft.getInstance().player)
        ){
            this.onClose();
            return;
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if(pKeyCode == 256){
            AllPackets.INTERACTION.sendToServer(new SeatDismountPacket());
            this.onClose();
            return true;
        }
        if(pKeyCode == 65 || pKeyCode == 68){
            AllPackets.INTERACTION.sendToServer(new OnSeatActionPacket(DriverSeatActionType.WATCHING_DOOR));
            return true;
        }
        if(pKeyCode == 32){
            AllPackets.INTERACTION.sendToServer(new OnSeatActionPacket(DriverSeatActionType.STAND));
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if(pKeyCode == 65 || pKeyCode == 68 || pKeyCode == 32) {
            AllPackets.INTERACTION.sendToServer(new OnSeatActionPacket(DriverSeatActionType.SIT_DOWN));
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        RenderContext screenTarget = new RenderContext(RenderContext.RenderContextType.SCREEN);
        screenTarget.setPoseStack(pPoseStack);
        screenTarget.pushLight(LightTexture.FULL_BRIGHT);
        screenTarget.setSource(InteractiveScreenTarget.class);
        
        int screenWidth = this.width;
        int screenHeight = this.height;
        
        for(InteractiveBehaviour.MenuEntry entry : interactiveSet) {
            if(!entry.isAvailable().get()) continue;
            
            Vec2 position = calculatePosition(entry, screenWidth, screenHeight);
            screenTarget.pushPose();
            screenTarget.pose().translate(
                    position.x,
                    position.y,
                    0
            );
            screenTarget.pose().scale(
                    entry.getScale().x,
                    entry.getScale().y,
                    1
            );
            this.interactiveTargets.get(entry).render(screenTarget);
            screenTarget.popPose();
        }
    }

    private Vec2 calculatePosition(InteractiveBehaviour.MenuEntry entry, int screenWidth, int screenHeight) {
        Vec2 size = entry.getSize();
        Vec2 scale = entry.getScale();
        float scaledWidth = size.x * scale.x;
        float scaledHeight = size.y * scale.y;
        
        float screenAnchorX = screenWidth * entry.getScreenAnchor().getXFactor();
        float screenAnchorY = screenHeight * entry.getScreenAnchor().getYFactor();
        
        float selfAnchorX = scaledWidth * entry.getSelfAnchor().getXFactor();
        float selfAnchorY = scaledHeight * entry.getSelfAnchor().getYFactor();
        
        return new Vec2(
            screenAnchorX - selfAnchorX + entry.getPosition().x,
            screenAnchorY - selfAnchorY + entry.getPosition().y
        );
    }

    public Optional<InteractiveBehaviour.MenuEntry> getMouseAtInteractiveItem(double x, double y) {
        return interactiveSet.stream().filter(entry -> {
            return this.isMouseAtInteractiveItem(x, y, entry);
        }).findFirst();
    }

    public boolean isMouseAtInteractiveItem(double pMouseX, double pMouseY, InteractiveBehaviour.MenuEntry entry) {
        Vec2 position = calculatePosition(entry, this.width, this.height);
        Vec2 size = entry.getSize();
        Vec2 scale = entry.getScale();
        float scaledWidth = size.x * scale.x;
        float scaledHeight = size.y * scale.y;

        return pMouseX >= position.x && pMouseX <= position.x + scaledWidth &&
               pMouseY >= position.y && pMouseY <= position.y + scaledHeight;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        Optional<InteractiveBehaviour.MenuEntry> interactive = getMouseAtInteractiveItem(pMouseX, pMouseY);
        if(interactive.isPresent()){
            InteractiveBehaviour.MenuEntry menu = interactive.get();
            if(menu.isFixed()) return super.mouseScrolled(pMouseX, pMouseY, pDelta);
            
            Vec2 size = menu.getScale();
            menu.setScale(
                    new Vec2(
                            Math.max(0, size.x + (float) pDelta * 0.01f),
                            Math.max(0, size.y + (float) pDelta * 0.01f)
                    )
            );
            return true;
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        Optional<InteractiveBehaviour.MenuEntry> interactive = getMouseAtInteractiveItem(pMouseX, pMouseY);
        if(interactive.isPresent()) {
            InteractiveBehaviour.MenuEntry entry = interactive.get();
            if(pButton == 1 && !entry.isFixed()){
                entry.setDragging(true);
                this.freshDraggingState();
                return true;
            }

            dispatchInteractive(
                    entry,
                    new Vec2((float) pMouseX, (float) pMouseY),
                    (localPosition)->
                        MouseDownEvent.fromScreen(null, new Vec2i((int)localPosition.x,(int)localPosition.y), pButton)
            );

            lastClickedPos = new Vec2((float) pMouseX, (float) pMouseY);

            currentlyDragging = entry;
            lastDragMouseX = pMouseX;
            lastDragMouseY = pMouseY;

            return true;
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    HashSet<InteractiveBehaviour.MenuEntry> dragging = new HashSet<>();

    private void freshDraggingState() {
        dragging.clear();
        dragging.addAll(interactiveSet.parallelStream().filter(t->t.dragging).collect(Collectors.toUnmodifiableSet()));
    }

    public Vec2 transformInteractive(InteractiveBehaviour.MenuEntry entry, Vec2 globalPosition) {
        // Transform Global Position into local position
        Vec2 position = calculatePosition(entry, this.width, this.height);
        return new Vec2(
                (globalPosition.x - position.x) / entry.getScale().x,
                (globalPosition.y - position.y) / entry.getScale().y
        );
    }

    public void dispatchInteractive(
            InteractiveBehaviour.MenuEntry entry,
            Vec2 globalPosition,
            Function<Vec2, MouseEvent> eventFunction
    ) {
        Vec2 local = transformInteractive(entry, globalPosition);

        entry.menuSupplier().get().getBinding().apply(GuiTargets.INTERACTIVE)
                .dispatchMouseEvent(InteractiveScreenTarget.class, eventFunction.apply(local));
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        currentlyDragging = null;
        if(pButton == 1){
            boolean hasDragging = false;
            for (InteractiveBehaviour.MenuEntry menuEntry : interactiveSet) {
                if(menuEntry.isDragging())
                    hasDragging = true;
                menuEntry.setDragging(false);
            }
            this.freshDraggingState();
            if(!hasDragging && lastClickedPos == null){
                super.mouseReleased(pMouseX, pMouseY, pButton);
            }
            return true;
        }
        Optional<InteractiveBehaviour.MenuEntry> interactive = getMouseAtInteractiveItem(pMouseX, pMouseY);
        if(interactive.isPresent()) {
            dispatchInteractive(
                    interactive.get(),
                    new Vec2((float) pMouseX, (float) pMouseY),
                    (localPosition)->
                            MouseUpEvent.fromScreen(null, new Vec2i((int)localPosition.x,(int)localPosition.y), pButton)
            );
            if(lastClickedPos != null) {
                if(Math.abs(lastClickedPos.x - pMouseX) + Math.abs(lastClickedPos.y - pMouseY) < 0.1){
                    dispatchInteractive(
                            interactive.get(),
                            new Vec2((float) pMouseX, (float) pMouseY),
                            (localPosition)->
                                    MouseClickEvent.fromScreen(null, new Vec2i((int)localPosition.x,(int)localPosition.y), pButton)
                    );
                }
            }
            lastClickedPos = null;
            return true;
        }
        lastClickedPos = null;
        freshDraggingState();
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    double lastMouseX = 0.0D;
    double lastMouseY = 0.0D;

    InteractiveBehaviour.MenuEntry currentlyDragging = null;
    double lastDragMouseX = 0.0D;
    double lastDragMouseY = 0.0D;

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        boolean hasDragging = false;
        for (InteractiveBehaviour.MenuEntry menuEntry : interactiveSet) {
            if(menuEntry.isDragging()){
                Vec2 pos = menuEntry.getPosition();
                menuEntry.setPosition(
                        new Vec2(
                                pos.x + (float) (pMouseX - lastMouseX),
                                pos.y + (float) (pMouseY - lastMouseY)
                        )
                );

                hasDragging = true;
            }
        }
        lastMouseX = pMouseX;
        lastMouseY = pMouseY;
        // Optional<InteractiveBehaviour.MenuEntry> interactive = getMouseAtInteractiveItem(pMouseX, pMouseY);
        if(currentlyDragging != null) {
            if(!this.isMouseAtInteractiveItem(pMouseX, pMouseY, currentlyDragging)) {
                dispatchInteractive(
                        currentlyDragging,
                        new Vec2((float) pMouseX, (float) pMouseY),
                        (localPosition)->
                                MouseUpEvent.fromScreen(null, new Vec2i((int)localPosition.x,(int)localPosition.y), 0)
                );
                currentlyDragging = null;
                return;
            }
            double scaleX = currentlyDragging.getScale().x;
            double scaleY = currentlyDragging.getScale().y;
            double xDeltaD = (pMouseX - lastDragMouseX) / scaleX, yDeltaD = (pMouseY - lastDragMouseY) / scaleY;
            int xDeltaI = (int) xDeltaD, yDeltaI = (int) yDeltaD;
            if(xDeltaI != 0 || yDeltaI != 0) {
                lastDragMouseX += ((double) xDeltaI) * scaleX;
                lastDragMouseY += ((double) yDeltaI) * scaleY;
                Vec2i delta = new Vec2i(xDeltaI, yDeltaI);

                Vec2 local = transformInteractive(currentlyDragging, new Vec2((float) pMouseX, (float) pMouseY));

                currentlyDragging.menuSupplier().get().getBinding().apply(GuiTargets.INTERACTIVE)
                        .dispatchEventToActivate(
                                MouseDragEvent.fromScreen(null, new Vec2i((int)local.x,(int)local.y), 0, delta)
                        );

            }
            return;
        }
        if(!hasDragging)
            super.mouseMoved(pMouseX, pMouseY);
    }
    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if(!this.dragging.isEmpty() || lastClickedPos != null || currentlyDragging != null)
            return true;
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public void onClose() {
        if(Minecraft.getInstance().screen != this)
            return;
        super.onClose();
        this.interactiveTargets.forEach((i,s)->s.detach());
    }

    private static class MenuProp {
        public Vec2 position;
        public double scale;
    }
}
