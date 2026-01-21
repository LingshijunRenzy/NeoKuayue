package willow.train.kuayue.utils.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.ArrayList;
import java.util.List;

public class StatusOverlayRenderer {
    public static Boolean canDivide = null;
    public static Boolean lastCanDivide = null;
    public static int hoverTicks = 0;

    public static IGuiOverlay OVERLAY = StatusOverlayRenderer::render;

    public static void render(ForgeGui gui, PoseStack poseStack, float partialTicks, int width, int height) {
        if(canDivide == null) {
            lastCanDivide = null;
            hoverTicks = 0;
            return;
        }


        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;

        boolean stateChanged = !canDivide.equals(lastCanDivide);
        if(stateChanged) {
            hoverTicks = 1;
            lastCanDivide = canDivide;
        } else {
            hoverTicks++;
        }

        Component component = canDivide ?
                Component.literal("    ").append(Component.translatable("gui.kuayue.coupler.can_divide")) :
                Component.literal("    ").append(Component.translatable("gui.kuayue.coupler.cannot_divide"));

        ItemStack item = AllItems.WRENCH.asStack();

        poseStack.pushPose();

        CClient cfg = AllConfigs.client();
        int posX = width / 2 + cfg.overlayOffsetX.get();
        int posY = height / 2 + cfg.overlayOffsetY.get();

        posX = Math.min(posX, width - mc.font.width(component) - 20);
        posY = Math.min(posY, height - 28);

        float fade = Mth.clamp((hoverTicks + partialTicks) / 24f, 0, 1);
        Boolean useCustom = cfg.overlayCustomColor.get();
        Color colorBackground = useCustom ? new Color(cfg.overlayBackgroundColor.get())
                : Theme.c(Theme.Key.VANILLA_TOOLTIP_BACKGROUND)
                .scaleAlpha(.75f);
        Color colorBorderTop = useCustom ? new Color(cfg.overlayBorderColorTop.get())
                : Theme.c(Theme.Key.VANILLA_TOOLTIP_BORDER, true)
                .copy();
        Color colorBorderBot = useCustom ? new Color(cfg.overlayBorderColorBot.get())
                : Theme.c(Theme.Key.VANILLA_TOOLTIP_BORDER, false)
                .copy();

        if (fade < 1) {
            poseStack.translate(Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + .5f) * 8, 0, 0);
            colorBackground.scaleAlpha(fade);
            colorBorderTop.scaleAlpha(fade);
            colorBorderBot.scaleAlpha(fade);
        }

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(component);

        RemovedGuiUtils.drawHoveringText(poseStack, tooltip, posX, posY, width, height, -1, colorBackground.getRGB(),
                colorBorderTop.getRGB(), colorBorderBot.getRGB(), mc.font);

        GuiGameElement.of(item)
                .at(posX + 10, posY - 16, 450)
                .render(poseStack);

        poseStack.popPose();
    }
}
