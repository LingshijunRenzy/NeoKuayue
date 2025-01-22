package willow.train.kuayue.systems.device.driver.seat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import kasuga.lib.core.client.frontend.gui.GuiScreen;
import kasuga.lib.core.client.frontend.rendering.RenderContext;
import kasuga.lib.core.client.interaction.GuiOperatingPerspectiveScreen;
import kasuga.lib.core.menu.base.GuiBindingTarget;
import kasuga.lib.core.menu.base.GuiMenu;
import kasuga.lib.core.menu.targets.ClientScreenTarget;
import kasuga.lib.core.menu.targets.Target;
import kasuga.lib.core.menu.targets.WorldRendererTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.c2s.OnSeatActionPacket;
import willow.train.kuayue.network.c2s.SeatDismountPacket;
import willow.train.kuayue.systems.editable_panel.widget.ItemIconButton;

import java.util.*;

public class InteractiveDriveScreen extends GuiOperatingPerspectiveScreen {
    private final Contraption contraption;
    private final AbstractContraptionEntity entity;

    private final HashMap<InteractiveBehaviour.MenuEntry, MenuProp> props = new HashMap<InteractiveBehaviour.MenuEntry, MenuProp>();

    private final List<InteractiveBehaviour.MenuEntry> interactiveSet = new ArrayList<>();

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
    }

    @Override
    public void tick() {
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
        if(pKeyCode == 65 || pKeyCode == 66){
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
        if(pKeyCode == 30 || pKeyCode == 32) {
            AllPackets.INTERACTION.sendToServer(new OnSeatActionPacket(DriverSeatActionType.SIT_DOWN));
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        RenderContext worldContext = new RenderContext(RenderContext.RenderContextType.SCREEN);
        worldContext.setPoseStack(pPoseStack);
        worldContext.pushLight(LightTexture.FULL_BRIGHT);
        worldContext.setSource(WorldRendererTarget.class);
        for(InteractiveBehaviour.MenuEntry entry : interactiveSet) {
            worldContext.pushPose();
            worldContext.pose().translate(
                    entry.getPosition().x,
                    entry.getPosition().y,
                    0
            );
            worldContext.pose().scale(
                    entry.getScale().x,
                    entry.getScale().y,
                    1
            );
            if(entry.isAvailable().get()){
                entry
                        .menuSupplier()
                        .get()
                        .getBinding()
                        .apply(Target.WORLD_RENDERER)
                        .render(worldContext);
            }
            worldContext.popPose();
        }
    }

    public Optional<InteractiveBehaviour.MenuEntry> getMouseAtInteractiveItem(double x, double y) {
        return interactiveSet.stream().filter(v->{
            return x >= v.getPosition().x && x <= v.getPosition().x + v.getSize().x * v.getScale().x &&
                   y>= v.getPosition().y && y <= v.getPosition().y + v.getSize().y * v.getScale().y;
        }).findFirst();
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        Optional<InteractiveBehaviour.MenuEntry> interactive = getMouseAtInteractiveItem(pMouseX, pMouseY);
        if(interactive.isPresent()){
            InteractiveBehaviour.MenuEntry menu = interactive.get();
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
            if(pButton == 1){
                interactive.get().setDragging(true);
                return true;
            }
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if(pButton == 1){
            for (InteractiveBehaviour.MenuEntry menuEntry : interactiveSet) {
                menuEntry.setDragging(false);
            }
            return true;
        }
        Optional<InteractiveBehaviour.MenuEntry> interactive = getMouseAtInteractiveItem(pMouseX, pMouseY);
        if(interactive.isPresent()) {
            return true;
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    double lastMouseX = 0.0D;
    double lastMouseY = 0.0D;
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
        Optional<InteractiveBehaviour.MenuEntry> interactive = getMouseAtInteractiveItem(pMouseX, pMouseY);
        if(interactive.isPresent()) {
            return;
        }
        if(!hasDragging)
            super.mouseMoved(pMouseX, pMouseY);
    }

    @Override
    public void onClose() {
        if(Minecraft.getInstance().screen != this)
            return;
        super.onClose();
    }

    private static class MenuProp {
        public Vec2 position;
        public double scale;
    }
}
