package willow.train.kuayue.systems.graph;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class GraphScreen extends Screen {

    Vec3 position = new Vec3(0,0,0);

    protected GraphScreen(Component pTitle) {
        super(pTitle);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        Create.RAILWAYS.sided(null)
                .trackNetworks
                .forEach((key, value) -> renderGraph(value, guiGraphics));
    }

    public void renderGraph(TrackGraph value, GuiGraphics guiGraphics){
        for (TrackNodeLocation node : value.getNodes()) {

            Vec3 pos = node.getLocation().add(position.reverse());
            guiGraphics.drawString(font, ".",
                    (int) Math.round(pos.x),
                    (int) Math.round(pos.z),
                    0xffffff);
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        position = position.add(pDragX, 0, pDragY);
        return true;
    }
}
