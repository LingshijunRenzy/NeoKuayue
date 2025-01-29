package willow.train.kuayue.block.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import willow.train.kuayue.initial.ClientInit;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTree;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeGroup;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeManager;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeNode;
import willow.train.kuayue.systems.tech_tree.client.gui.TechTreeItemButton;
import willow.train.kuayue.systems.tech_tree.client.gui.TechTreeLabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class BlueprintScreen extends AbstractContainerScreen<BlueprintMenu> {
    private boolean showSub;
    private final HashMap<String, ClientTechTree> trees;
    LazyRecomputable<ImageMask> bgMask = LazyRecomputable.of(() -> new ImageMask(ClientInit.blueprintTableBg.getImageSafe().get()));
    LazyRecomputable<ImageMask> bgNoSubMask = LazyRecomputable.of(() -> new ImageMask(ClientInit.blueprintTableNoSub.getImageSafe().get()));
    LazyRecomputable<ImageMask> upArrow = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .copyWithOp(o -> o.rectangleUV(32f / 128f, 32f / 128f,
                            48f / 128f, 40f / 128f))
    );

    LazyRecomputable<ImageMask> downArrow = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .copyWithOp(o -> o.rectangleUV(32f / 128f, 40f / 128f,
                            48f / 128f, 48f / 128f))
    );

    private int windowCapacity = 0, windowTop = 0;
    private ArrayList<TechTreeItemButton> groupButtons;
    private float bgX = 0, bgY = 0, scale = 1.0f;

    public BlueprintScreen(BlueprintMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.showSub = false;
        trees = ClientTechTreeManager.MANAGER.trees();
        groupButtons = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();
        for (ClientTechTree tree : trees.values()) {
            tree.getGroups().forEach((name, group) -> groupButtons
                    .add(new TechTreeItemButton(group.getIcon(), 20, 20, (a, b, c) -> {

                    })));
        }
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick,
                            int mouseX, int mouseY) {
        renderBackground(poseStack);
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) return;
        int windowWidth = mc.screen.width;
        int windowHeight = mc.screen.height;
        ImageMask mask = showSub ? bgMask.get() : bgNoSubMask.get();
        int w = (int) (windowWidth * .9f);
        scale = ((float) w / (float) mask.getImage().width());
        int h = map(mask.getImage().height(), scale);
        bgX = (float) (windowWidth - w) / 2;
        bgY = (float) (windowHeight - h) / 2;
        mask.rectangle(new Vector3f(bgX, bgY, 0),
                ImageMask.Axis.X, ImageMask.Axis.Y, true, true, w, h);
        poseStack.pushPose();
        mask.renderToGui(poseStack.last());
        poseStack.popPose();
    }

    private int map(int xOry, float scale) {
        return (int) (xOry * scale);
    }

    // leftUp border: (7, 19); (23, 106)
    private void renderGuidelines(PoseStack poseStack, float partial,
                                  int mouseX, int mouseY, float scale) {
        int leftTopX = map(7, scale);
        int leftTopY = map(19, scale);

        int rightDownX = map(23, scale);
        int rightDownY = map(106, scale);

        int guideWidth = rightDownX - leftTopX;
        int guideHeight = rightDownY - leftTopY;
        int btnHeight = guideHeight - 20;
        windowCapacity = btnHeight / 20;

        if (windowCapacity <= groupButtons.size()) {
            ImageMask upArrowMask = upArrow.get();
            ImageMask downArrowMask = downArrow.get();
            float btnX = bgX + leftTopX + (float) (guideWidth - 16) / 2;

            if (windowTop > 0) {
                upArrowMask.rectangle(new Vector3f(btnX, bgY + leftTopY + .5f, 0),
                        ImageMask.Axis.X, ImageMask.Axis.Y,
                        true, true, 16, 8);
                upArrowMask.renderToGui();
            }
            if (windowTop + windowCapacity < groupButtons.size()) {
                downArrowMask.rectangle(new Vector3f(btnX, bgY + rightDownY - 8, 0),
                        ImageMask.Axis.X, ImageMask.Axis.Y,
                        true, true, 16, 8);
                downArrowMask.renderToGui();
            }
        }
        int grpBtnY = Math.round(bgY) + leftTopY + (guideHeight - btnHeight) / 2;
        int grpBtnX = Math.round(bgX) + leftTopX + (guideWidth - 20) / 2;
        for (int i = windowTop; i < Math.min(windowTop + windowCapacity, groupButtons.size()); i++) {
            TechTreeItemButton button = groupButtons.get(i);
            button.setPosition(grpBtnX, grpBtnY + (i - windowTop) * 20);
            button.render(poseStack, mouseX, mouseY, partial);
        }
    }

    private void testLabel(PoseStack poseStack, float partial, int mouseX, int mouseY) {
        if (trees.isEmpty()) return;
        ClientTechTree tree = null;
        for (ClientTechTree t : trees.values()) {
            tree = t;
            break;
        }
        ClientTechTreeGroup group = null;
        for (ClientTechTreeGroup g : tree.getGroups().values()) {
            group = g;
            break;
        }
        if (group == null) return;
        ClientTechTreeNode node = group.getRootNode();
        TechTreeLabel label = TechTreeLabel.smallLabel(node, 0, 0, Component.empty());
        label.render(poseStack, mouseX, mouseY, partial);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderGuidelines(poseStack, partialTick, mouseX, mouseY, scale);
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        // super.renderLabels(pPoseStack, pMouseX, pMouseY);
    }
}
