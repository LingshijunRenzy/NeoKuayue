package willow.train.kuayue.block.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.initial.ClientInit;
import willow.train.kuayue.network.c2s.tech_tree.CanUnlockNodePacket;
import willow.train.kuayue.network.c2s.tech_tree.UnlockNodePacket;
import willow.train.kuayue.systems.editable_panel.widget.ImageButton;
import willow.train.kuayue.systems.editable_panel.widget.OnClick;
import willow.train.kuayue.systems.tech_tree.NodeLocation;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTree;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeGroup;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeManager;
import willow.train.kuayue.systems.tech_tree.client.ClientTechTreeNode;
import willow.train.kuayue.systems.tech_tree.client.gui.*;
import willow.train.kuayue.systems.tech_tree.player.ClientPlayerData;
import willow.train.kuayue.systems.tech_tree.player.PlayerData;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class BlueprintScreen extends AbstractContainerScreen<BlueprintMenu> {

    public static BlueprintScreen INSTANCE = null;

    private boolean showSub, hasJei;
    private final HashMap<String, ClientTechTree> trees;
    LazyRecomputable<ImageMask> bgMask = LazyRecomputable.of(() -> new ImageMask(ClientInit.blueprintTableBg.getImageSafe().get()));

    LazyRecomputable<ImageMask> bgNoSubMask = LazyRecomputable.of(() -> new ImageMask(ClientInit.blueprintTableNoSub.getImageSafe().get()));

    LazyRecomputable<ImageMask> bgFinishedMask = LazyRecomputable.of(() -> new ImageMask(ClientInit.blueprintTableCompleted.getImageSafe().get()));

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

    LazyRecomputable<ImageMask> subRightArrow = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .copyWithOp(o -> o.rectangleUV(64f / 128f, 0, 96f / 128f, 18f / 128f))
    );

    LazyRecomputable<ImageMask> subRightArrow2 = LazyRecomputable.of(
            () -> subRightArrow.get().copyWithOp(o -> o)
    );

    LazyRecomputable<ImageMask> groupChosenFrame = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .copyWithOp(o -> o.rectangleUV(64f / 128f, 18f / 128f,
                            80f / 128f, 36f / 128f))
    );

    LazyRecomputable<ImageMask> mainArrow = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .copyWithOp(o -> o.rectangleUV(48f / 128f, 48f / 128f, 101f / 128f, 64f / 128f))
    );

    LazyRecomputable<ImageMask> confirmBtnLight = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .copyWithOp(o -> o.rectangleUV(48f / 128f, 64f / 128f, 64f / 128f, 80f / 128f))
    );

    LazyRecomputable<ImageMask> confirmBtnDark = LazyRecomputable.of(
            () -> ClientInit.blueprintButtons.getImageSafe().get().getMask()
                    .copyWithOp(o -> o.rectangleUV(64f / 128f, 64f / 128f, 80f / 128f, 80f / 128f))
    );

    int windowWidth = 0, windowHeight = 0;

    private int windowCapacity = 0, windowTop = 0;
    private final ArrayList<TechTreeItemButton> groupButtons;
    private ImageButton guideUpBtn, guideDownBtn;

    private float bgX = 0, bgY = 0, scale = 1.0f;
    private ClientTechTreeGroup chosenGroup;
    private final HashMap<ClientTechTreeGroup, TechTreePanel> panels;
    private final TechTreeTitleLabel titleLabel;
    private Tooltip tooltip = null;

    // for node chosen
    private TechTreeLabel chosenLabel = null;
    private final HashSet<TechTreeLabel> prevLabels, nextLabels;
    private final ArrayList<LabelGrid> prevGrids, nextGrids;
    private int prevGridIndex = -1, nextGridIndex = -1;
    private final ItemSlot[] consumptionSlots, resultSlots;
    private MutableComponent nodeTitleComponent = null;
    private final List<ClientTechTreeGroup> groups;
    private final ImageButton confirmButton;

    private ImageButton gridBtnUpLeft, gridBtnDownLeft,
            gridBtnUpRight, gridBtnDownRight;

    private float mainPercentage = 0;
    private boolean nodeFinished = false;
    private FinishedTooltip finishedTooltip = null;
    private final ExpComponentBar expBar;


    public BlueprintScreen(BlueprintMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        INSTANCE = this;
        this.showSub = false;
        groups = new ArrayList<>();
        trees = ClientTechTreeManager.MANAGER.trees();
        updateGroups(null);
        groupButtons = new ArrayList<>();
        this.hasJei = ModList.get().isLoaded("jei");
        panels = new HashMap<>();
        titleLabel = new TechTreeTitleLabel(chosenGroup == null ? Component.empty() :
                Component.translatable(chosenGroup.getTitleKey()));
        this.prevLabels = new HashSet<>();
        this.nextLabels = new HashSet<>();
        this.prevGrids = new ArrayList<>();
        this.nextGrids = new ArrayList<>();
        expBar = new ExpComponentBar(0, 0, 50, 11);
        expBar.visible = false;
        consumptionSlots = new ItemSlot[8];
        resultSlots = new ItemSlot[4];
        for (int i = 0; i < consumptionSlots.length; i++)
            consumptionSlots[i] = new ItemSlot(0, 0);
        for (int i = 0; i < resultSlots.length; i++)
            resultSlots[i] = new ItemSlot(0, 0);
        confirmButton = new ImageButton(confirmBtnDark, confirmBtnLight, 0, 0, 16, 16, Component.empty(), b -> {
            sendUnlockPacket();
        });
    }

    public boolean isFocusingLabel(NodeLocation node) {
        return this.chosenLabel != null && this.chosenLabel.getNode().getLocation().equals(node);
    }

    public void sendCheckPacket() {
        if (this.chosenLabel == null) return;
        AllPackets.TECH_TREE_CHANNEL.sendToServer(new CanUnlockNodePacket(this.chosenLabel.getNode().getLocation()));
    }

    public void sendUnlockPacket() {
        if (this.chosenLabel == null) return;
        AllPackets.TECH_TREE_CHANNEL.sendToServer(new UnlockNodePacket(chosenLabel.getNode().location));
    }

    private void updateGroups(ResourceLocation prevChosen) {
        groups.clear();
        Player player = Minecraft.getInstance().player;
        if (ClientPlayerData.getData().isPresent()) {
            PlayerData playerData = ClientPlayerData.getData().get();
            for (Map.Entry<String, ClientTechTree> tree : trees.entrySet()) {
                if (player != null && player.isCreative()) {
                    groups.addAll(tree.getValue().getGroups().values());
                } else {
                    groups.addAll(tree.getValue().getVisiblePart(playerData));
                }
            }
        }
        if (chosenGroup == null || prevChosen == null) {
            if (!groups.isEmpty()) chosenGroup = groups.get(0);
        } else {
            for (ClientTechTreeGroup group : groups) {
                if (group.getId().equals(prevChosen)) {
                    chosenGroup = group;
                    return;
                }
            }
            if (!groups.isEmpty()) chosenGroup = groups.get(0);
        }
    }

    public void handleUpdateResult(NodeLocation node, PlayerData.UnlockResult result) {
        panels.clear();
        updateGroups(chosenGroup == null ? null : chosenGroup.getId());
        refreshPanels(true);
        refreshGroupButtons();
        updateGuidelines(scale);
        setPanelsPosition();
        panels.forEach((g, p) -> {
            p.setSize(map(247, scale), map(117 ,scale));
            // p.adjustSize(map(247, scale), map(117, scale));
            p.moveToWindowCentral(scale);
        });
        setPanelsSize();
        if (chosenLabel != null && showSub) {
            updateSub(chosenGroup.getNodes().get(chosenLabel.getNode().location));
            updateGrids();
        }
        if (result.flag()) {
            clearUnlock();
            renderAllSlots(false);
            nodeFinished = true;
            confirmButton.visible = false;
            updateFinishTooltip(this.chosenLabel);
        }
    }

    private void setPercentage(int x, int y, int width, int height, float percentage) {
        ImageMask mask = mainArrow.get();
        mask.rectangleUV(48f / 128f, 48f / 128f,
                48f / 128f + (101f - 48f) * percentage / 128f, 64f / 128f);
        mask.rectangle(new Vector3f(x, y, 0), ImageMask.Axis.X, ImageMask.Axis.Y,
                true, true, Math.round((float) width * percentage), height);
    }

    private void updatePercentage(float percentage) {
        this.mainPercentage = percentage;
        setPercentage(getBgX() + map(150, scale), getBgY() + map(114, scale),
                map(53, scale), map(15, scale), mainPercentage);
    }

    public ImageButton genArrowButton(int x, int y, Button.OnPress action, boolean upArrow) {
        LazyRecomputable<ImageMask> mask = upArrow ?
                LazyRecomputable.of(() -> this.upArrow.get().copyWithOp(m -> m)) :
                LazyRecomputable.of(() -> this.downArrow.get().copyWithOp(m -> m));
        return new ImageButton(mask, x, y, 16, 8, Component.empty(), action);
    }

    public void updateGrids() {
        clearAllGrids();
        ArrayList<TechTreeLabel> prevLabels = new ArrayList<>(this.prevLabels);
        ArrayList<TechTreeLabel> nextLabels = new ArrayList<>(this.nextLabels);
        OnClick<TechTreeLabel> click = (label, mx, my) -> {
            setChosenLabel(label);
        };
        for (int i = 0; ;i += 9) {
            if (i >= prevLabels.size() && i >= nextLabels.size())
                break;
            genGrid(prevLabels, click, i, prevGrids);
            genGrid(nextLabels, click, i, nextGrids);
        }
        if (!prevGrids.isEmpty()) {
            prevGridIndex = 0;
            LabelGrid grid = prevGrids.get(prevGridIndex);
            grid.visible = true;
        }
        if (!nextGrids.isEmpty()) {
            nextGridIndex = 0;
            LabelGrid grid = nextGrids.get(nextGridIndex);
            grid.visible = true;
        }
        updateGridsPosition(scale);
    }

    private void clearGridBtn() {
        gridBtnUpLeft.visible = false;
        gridBtnDownLeft.visible = false;
        gridBtnUpRight.visible = false;
        gridBtnDownRight.visible = false;
    }

    private void updateSlotPos(float scale) {
        consumptionSlots[0].setPosition(getBgX() + map( 55, scale) + getSlotSidePos(scale),
                getBgY() + map( 113, scale) + getSlotSidePos(scale));
        for (int i = 1; i < 7; i++) {
            consumptionSlots[i].setPosition(getBgX() + map(71, scale) +
                            ((i - 1) / 2) * getSlotSide(scale) + getSlotSidePos(scale),
                    getBgY() + map(105, scale) + ((i - 1) % 2) * getSlotSide(scale) + getSlotSidePos(scale));
        }
        consumptionSlots[7].setPosition(getBgX() + map(119, scale) + getSlotSidePos(scale),
                getBgY() + map(113, scale) + getSlotSidePos(scale));
        resultSlots[0].setPosition(getBgX() + map(216, scale) + getSlotSidePos(scale),
                getBgY() + map(113, scale) + getSlotSidePos(scale));
        resultSlots[1].setPosition(getBgX() + map(231, scale) + getSlotSidePos(scale),
                getBgY() + map(105, scale) + getSlotSidePos(scale));
        resultSlots[2].setPosition(getBgX() + map(231, scale) + getSlotSidePos(scale),
                getBgY() + map(105, scale) + getSlotSide(scale) + getSlotSidePos(scale));
        resultSlots[3].setPosition(getBgX() + map(247, scale) + getSlotSidePos(scale),
                getBgY() + map(113, scale) + getSlotSidePos(scale));
    }

    public void updateSlotItems(ClientTechTreeNode node) {
        clearSlotItems();
        int counter = 0;
        for (ItemStack item : node.getItemConsume()) {
            ItemSlot slot = consumptionSlots[counter];
            slot.setItemStack(item);
            counter++;
            if (counter >= consumptionSlots.length) break;
        }
    }

    public void unableToUnlock(PlayerData.CheckReason reason,
                               Collection<NodeLocation> nodeRequired,
                               Collection<ItemStack> itemRequired) {
        clearUnlock();
        prevGrids.forEach(grid -> {
            grid.getLabels().forEach(label -> {
                if (nodeRequired.contains(label.getNode().location))
                    label.setRequired(true);
            });
        });
        for (ItemSlot slot : consumptionSlots) {
            ItemStack stack = slot.getItemStack();
            if (stack == null || stack.equals(ItemStack.EMPTY))
                continue;
            for (ItemStack item : itemRequired) {
                if (item.sameItem(stack)) {
                    slot.setPermanentRedMask(true);
                    break;
                } else {
                    slot.setPermanentGreenMask(true);
                }
            }
        }
        this.confirmButton.visible = false;
        this.expBar.updateSize();
        this.expBar.setPos(getBgX() + map(174, scale) - expBar.getWidth() / 2,
                getBgY() + map(113, scale) - expBar.getHeight() / 2);
        this.expBar.setUnlock(reason.enoughLevel());
        this.expBar.visible = true;
    }

    public void clearUnlock() {
        for (ItemSlot slot : consumptionSlots) {
            slot.setPermanentRedMask(false);
            slot.setRenderRedMask(false);
            slot.setPermanentGreenMask(false);
            slot.setRenderGreenMask(false);
        }
        this.confirmButton.visible = showSub;
        this.prevGrids.forEach(grid -> {
            grid.getLabels().forEach(label -> {
                label.setRequired(false);
            });
        });
        this.nextGrids.forEach(grid -> {
            grid.getLabels().forEach(label -> {
                label.setRequired(false);
            });
        });
        for (ItemSlot slot : resultSlots) {
            slot.setItemStack(ItemStack.EMPTY);
        }
        this.expBar.visible = false;
    }

    public void ableToUnlock(PlayerData.CheckReason reason) {
        clearUnlock();
        for (ItemSlot slot : consumptionSlots) {
            if (slot.isEmpty()) continue;
            slot.setPermanentGreenMask(true);
        }
        this.expBar.updateSize();
        this.expBar.setPos(getBgX() + map(174, scale) - expBar.getWidth() / 2,
                getBgY() + map(113, scale) - expBar.getHeight() / 2);
        this.expBar.setUnlock(true);
        this.expBar.visible = true;
        if (reason.itemGot().isEmpty()) return;
        List<ItemStack> gotList = new ArrayList<>(reason.itemGot());
        int count = 0;
        for (ItemSlot slot : resultSlots) {
            ItemStack stack = gotList.get(count);
            slot.setItemStack(stack);
            count++;
        }
    }

    public void clearSlotItems() {
        for (ItemSlot slot : consumptionSlots) {
            slot.setItemStack(ItemStack.EMPTY);
        }
        for (ItemSlot slot : resultSlots) {
            slot.setItemStack(ItemStack.EMPTY);
        }
    }

    private void renderAllSlots(boolean flag) {
        for (ItemSlot slot : consumptionSlots)
            slot.visible = flag;
        for (ItemSlot slot : resultSlots)
            slot.visible = flag;
    }

    private int getSlotSide(float scale) {
        return Math.round(scale * 16f);
    }

    private int getSlotSidePos(float scale) {
        return getSlotSide(scale) / 2 - 8;
    }

    private void genGrid(ArrayList<TechTreeLabel> nextLabels, OnClick<TechTreeLabel> click, int i, ArrayList<LabelGrid> nextGrids) {
        if (i < nextLabels.size()) {
            LabelGrid grid = new LabelGrid(0, 0, nextLabels.subList(i, nextLabels.size()));
            grid.visible = false;
            grid.setOnClick(click);
            nextGrids.add(grid);
            addRenderableWidget(grid);
        }
    }

    private void updateGridsPosition(float scale) {
        prevGrids.forEach(grid -> {
            grid.setPos(map(getBgX() + Math.round(314f / 4f) - Math.round(grid.getWidth() / 2f), scale),
                    map(getBgY() + 55 - grid.getHeight() / 2, scale));
        });
        nextGrids.forEach(grid -> {
            grid.setPos(map(getBgX() + Math.round(942f / 4f) - Math.round(grid.getWidth() / 2f), scale),
                    map(getBgY() + 55 - grid.getHeight() / 2, scale));
        });
        if (!prevGrids.isEmpty()) {
            LabelGrid grid = prevGrids.get(0);
            gridBtnUpLeft.setPos(grid.x + grid.getWidth() / 2 - 8, grid.y + grid.getHeight() / 2 - 35);
            gridBtnDownLeft.setPos(grid.x + grid.getWidth() / 2 - 8, grid.y + grid.getHeight() / 2 + 35);
        }
        if (!nextGrids.isEmpty()) {
            LabelGrid grid = nextGrids.get(0);
            gridBtnUpRight.setPos(grid.x + grid.getWidth() / 2 - 8, grid.y + grid.getHeight() / 2 - 35);
            gridBtnDownRight.setPos(grid.x + grid.getWidth() / 2 - 8, grid.y + grid.getHeight() / 2 + 35);
        }
        autoGridBtnVisible();
    }

    private void renderSubArrows(float scale) {
        if (!prevGrids.isEmpty()) {
            ImageMask arrow1 = subRightArrow.get();
            arrow1.rectangle(new Vector3f(map(getBgX() + Math.round(314f * 3 / 8) - 16, scale), map(getBgY() + 55 - 9, scale), 0),
                    ImageMask.Axis.X, ImageMask.Axis.Y, true, true, 32, 18);
            arrow1.renderToGui();
        }
        if (!nextGrids.isEmpty()) {
            ImageMask arrow2 = subRightArrow2.get();
            arrow2.rectangle(new Vector3f(map(getBgX() + Math.round(314f * 5 / 8) - 17, scale), map(getBgY() + 55 - 9, scale), 0),
                    ImageMask.Axis.X, ImageMask.Axis.Y, true, true, 32, 18);
            arrow2.renderToGui();
        }
    }

    private void clearAllGrids() {
        prevGrids.forEach(this::removeWidget);
        nextGrids.forEach(this::removeWidget);
        prevGrids.clear();
        nextGrids.clear();
        clearGridIndex();
    }

    private void clearGridIndex() {
        prevGridIndex = -1;
        nextGridIndex = -1;
    }

    private void refreshPanels(boolean notVisible) {
        panels.forEach((p, w) -> removeWidget(w));
        panels.clear();
        for (ClientTechTreeGroup group : groups) {
            TechTreePanel panel =
                    new TechTreePanel(0, 0, 300, 200, 100, 100);
            panel.compileGroup(group);
            panels.put(group, panel);
            panel.setOnClick((p, mX, mY) -> {
                TechTreeLabel label = p.getChosenLabel(mX, mY);
                if (label == null) return;
                setChosenLabel(label);
            });
            addRenderableWidget(panel);
            if (notVisible) panel.visible = false;
            else panel.visible = group == chosenGroup;
        }
    }



    private void refreshGroupButtons() {
        groupButtons.forEach(this::removeWidget);
        groupButtons.clear();
        groups.forEach(group -> groupButtons
                .add(new TechTreeItemButton(group.getIcon(), 20, 20, group,
                        (a, b, c) -> {
                            showSub = false;
                            chosenGroup = group;
                            panels.forEach((g, p) -> p.visible = g == chosenGroup);
                            titleLabel.setTitle(Component.translatable(chosenGroup.getTitleKey()));
                            clearSub();
                            clearAllGrids();
                            renderAllSlots(false);
                            titleLabel.visible = true;
                            updateFramePosition(chosenGroup);
                            clearGridBtn();
                            updateFinishTooltip(null);
                            confirmButton.visible = false;
                            expBar.visible = false;
                        }))
        );
        groupButtons.forEach(btn -> {
            addRenderableWidget(btn);
            btn.setVisible(false);
        });
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(confirmButton);
        confirmButton.visible = false;
        addRenderableWidget(titleLabel);
        onRefresh();
        refreshPanels(false);
        updateSlotPos(scale);
        for (ItemSlot slot : consumptionSlots) {
            addRenderableWidget(slot);
        }
        for (ItemSlot slot : resultSlots) {
            addRenderableWidget(slot);
        }
        initGridButtons();
        showSub = false;
        addRenderableWidget(expBar);
    }

    protected void initGridButtons() {
        gridBtnUpLeft = genArrowButton(0, 0, b -> {
            int index = prevGridIndex - 1;
            if (prevGridIndex == -1 || index < 0) {
                b.visible = false;
                return;
            }
            b.visible = showSub && (index - 1) >= 0;
            setPrevGridIndex(index);
        }, true);
        gridBtnDownLeft = genArrowButton(0, 0, b -> {
            int index = prevGridIndex + 1;
            if (prevGridIndex == -1 || index >= prevGrids.size()) {
                b.visible = false;
                return;
            }
            b.visible = showSub && (index + 1) < prevGrids.size();
            setPrevGridIndex(index);
        }, false);
        gridBtnUpRight = genArrowButton(0, 0, b -> {
            int index = nextGridIndex - 1;
            if (nextGridIndex == -1 || index < 0) {
                b.visible = false;
                return;
            }
            b.visible = showSub && (index - 1) >= 0;
            setNextGridIndex(index);
        }, true);
        gridBtnDownRight = genArrowButton(0, 0, b -> {
            int index = nextGridIndex + 1;
            if (nextGridIndex == -1 || index >= nextGrids.size()) {
                b.visible = false;
                return;
            }
            b.visible = showSub && (index + 1) < nextGrids.size();
            setNextGridIndex(index);
        }, false);
        gridBtnUpLeft.visible = false;
        gridBtnDownLeft.visible = false;
        gridBtnUpRight.visible = false;
        gridBtnDownRight.visible = false;
        addRenderableWidget(gridBtnUpLeft);
        addRenderableWidget(gridBtnDownLeft);
        addRenderableWidget(gridBtnUpRight);
        addRenderableWidget(gridBtnDownRight);
    }

    private void autoGridBtnVisible() {
        gridBtnUpLeft.visible = showSub &&
                prevGrids.size() > 1&&
                prevGridIndex > 0;
        gridBtnDownLeft.visible = showSub &&
                prevGrids.size() > 1 &&
                prevGridIndex < prevGrids.size() - 1;
        gridBtnUpRight.visible = showSub &&
                nextGrids.size() > 1 &&
                nextGridIndex > 0;
        gridBtnDownRight.visible = showSub &&
                nextGrids.size() > 1 &&
                nextGridIndex < nextGrids.size() - 1;
    }

    private void setPrevGridIndex(int index) {
        this.prevGridIndex = index;
    }

    private void setNextGridIndex(int index) {
        this.nextGridIndex = index;
    }

    public void setGroupsUpAndDownArrowButton() {
        if (this.guideDownBtn != null)
            removeWidget(this.guideDownBtn);
        if (this.guideUpBtn != null)
            addWidget(this.guideUpBtn);
        ImageButton upArrowBtn = genArrowButton(0, 0,
                button -> moveGuideWindow(true), true);
        ImageButton downArrowBtn = genArrowButton(0, 0,
                button -> moveGuideWindow(false), false);
        addRenderableWidget(upArrowBtn);
        addRenderableWidget(downArrowBtn);
        guideUpBtn = upArrowBtn;
        guideDownBtn = downArrowBtn;
    }

    public void moveGuideWindow(boolean up) {
        if (up && windowTop > 0) {
            windowTop --;
            return;
        }
        if (!up && windowTop < groupButtons.size() - windowCapacity)
            windowTop ++;
    }

    public void onRefresh() {
        refreshGroupButtons();
        updateGuidelines(this.scale);
        updatePercentage(mainPercentage);
        updateConfirmBtnPos();
    }

    private void setChosenLabel(TechTreeLabel label) {
        updateSub(label.getNode());
        updateGrids();
        updateSlotPos(scale);
        renderAllSlots(true);
        nodeTitleComponent = Component.translatable(label.getNode().getName())
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.BOLD)
                        .withUnderlined(true));
        updateSlotItems(label.getNode());
        if (ClientPlayerData.getData().isPresent()) {
            PlayerData playerData = ClientPlayerData.getData().get();
            if (playerData.unlocked.contains(label.getNode().location)) {
                clearUnlock();
                renderAllSlots(false);
                nodeFinished = true;
                confirmButton.visible = false;
                updateFinishTooltip(label);
                return;
            }
        }
        nodeFinished = false;
        sendCheckPacket();
        updateFinishTooltip(null);
    }

    private void updateFinishTooltip(@Nullable TechTreeLabel label) {
        if (finishedTooltip != null) removeWidget(finishedTooltip);
        if (label == null) return;
        this.finishedTooltip = new FinishedTooltip(label.getNode().getLocation().toString(), label.getNode().getName(),
                label.getNode().getDescription(), getBgX() + map(53, scale), getBgY() + map(107, scale),
                map(212, scale), map(33, scale));
        addRenderableWidget(this.finishedTooltip);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick,
                            int mouseX, int mouseY) {
        renderBackground(poseStack);
        Minecraft mc = Minecraft.getInstance();
        ImageMask mask = setParams(mc);
        if (mask == null) return;
        poseStack.pushPose();
        mask.renderToGui(poseStack.last());
        poseStack.popPose();
    }

    private void onPositionChanged(float neoBgx, float neoBgy) {
        if (neoBgx == bgX && neoBgy == bgY) return;
        bgX = neoBgx;
        bgY = neoBgy;
        setPanelsPosition();
        panels.forEach((g, p) -> {
            p.setSize(map(247, scale), map(117 ,scale));
            // p.adjustSize(map(247, scale), map(117, scale));
            p.moveToWindowCentral(scale);
        });
        updateGuidelines(scale);
        clearAllGrids();
        renderAllSlots(false);
        updateSlotPos(scale);
        titleLabel.visible = true;
        updateConfirmBtnPos();
        clearGridBtn();
        expBar.visible = false;
    }

    private void updateConfirmBtnPos() {
        confirmButton.setPos(getBgX() + map(168, scale), getBgY() + map(124, scale));
    }

    private void onScaleChanged(float neoScale) {
        if (neoScale == scale) return;
        scale = neoScale;
        setPanelsSize();
        updateGuidelines(scale);
        clearAllGrids();
        renderAllSlots(false);
        updateSlotPos(scale);
        titleLabel.visible = true;
        updatePercentage(mainPercentage);
        updateConfirmBtnPos();
        clearGridBtn();
        expBar.visible = false;
    }

    private void setPanelsPosition() {
        panels.forEach((g, p) -> p.setPosition(
                Math.round(bgX + map(33, scale)),
                Math.round(bgY + map(15, scale))
        ));
        updatePercentage(mainPercentage);
    }

    private void setPanelsSize() {
        panels.forEach((g, p) -> p.setSize(map(252, scale), map(122, scale)));
    }

    private ImageMask setParams(Minecraft mc) {
        if (mc.screen == null) return null;
        windowWidth = mc.screen.width;
        windowHeight = mc.screen.height;
        ImageMask mask = showSub ? (nodeFinished ? bgFinishedMask.get() : bgMask.get()) : bgNoSubMask.get();
        int w = (int) (windowWidth * (hasJei ? .7f : .9f));
        onScaleChanged(((float) w / (float) mask.getImage().width()));
        int h = map(mask.getImage().height(), scale);
        onPositionChanged((windowWidth * (hasJei ? .725f : 1f) - w) / 2, (float) (windowHeight - h) / 2);
        mask.rectangle(new Vector3f(bgX, bgY, 0),
                ImageMask.Axis.X, ImageMask.Axis.Y,
                true, true, w, h);
        return mask;
    }

    private int map(int xOry, float scale) {
        return (int) (xOry * scale);
    }

    // leftUp border: (7, 19); (23, 106)
    private void updateGuidelines(float scale) {
        int leftTopX = map(7, scale);
        int leftTopY = map(19, scale);

        int rightDownX = map(23, scale);
        int rightDownY = map(106, scale);

        int guideWidth = rightDownX - leftTopX;
        int guideHeight = rightDownY - leftTopY;
        int btnHeight = guideHeight - 20;
        windowCapacity = btnHeight / 20;

        titleLabel.setPosition(Math.round(bgX) + map(35, scale), Math.round(bgY) + map(12, scale));

        int grpBtnY = Math.round(bgY) + leftTopY + (guideHeight - btnHeight) / 2;
        int grpBtnX = Math.round(bgX) + leftTopX + (guideWidth - 20) / 2;
        for (int i = 0; i < groupButtons.size(); i++) {
            boolean flag = i >= windowTop &&
                    i < Math.min(windowTop + windowCapacity, groupButtons.size());
            TechTreeItemButton button = groupButtons.get(i);
            if (flag) button.setPosition(grpBtnX, grpBtnY + (i - windowTop) * 20);
            button.setVisible(flag);
        }
        if (this.windowCapacity < this.groupButtons.size() &&
                this.guideDownBtn == null && this.guideUpBtn == null) {
            setGroupsUpAndDownArrowButton();
        }
        int btnX = Math.round(bgX + leftTopX + (float) (guideWidth - 16) / 2);
        if (guideUpBtn != null)
            guideUpBtn.setPos(btnX, Math.round(bgY) + leftTopY + 1);
        if (guideDownBtn != null)
            guideDownBtn.setPos(btnX, Math.round(bgY) + rightDownY - 8);
        updateFramePosition(chosenGroup);
    }

    private void updateFramePosition(ClientTechTreeGroup group) {
        if (group == null) return;
        int index = -1;
        TechTreeItemButton chosenButton = null;
        for (int i = 0; i < groupButtons.size(); i++) {
            TechTreeItemButton button = groupButtons.get(i);
            if (chosenGroup == button.getGroup()) {
                index = i;
                chosenButton = button;
                break;
            }
        }
        if (index < 0) return;
        index -= windowTop;
        if (index >= windowCapacity) return;
        ImageMask frame = groupChosenFrame.get();
        frame.rectangle(new Vector3f(chosenButton.x + 2, chosenButton.y + 2, 0),
                ImageMask.Axis.X, ImageMask.Axis.Y, true, true,
                16, 18);
    }

    private void updateSub(ClientTechTreeNode chosenNode) {
        if (chosenLabel != null) clearSub();
        // 150 - 12, 55 - 12
        chosenLabel = TechTreeLabel.largeLabel(chosenNode, map(getBgX() + 145, scale),
                map(getBgY() + 43, scale), Component.empty());
        if (ClientPlayerData.getData().isPresent()) {
            PlayerData playerData = ClientPlayerData.getData().get();
            chosenLabel.setFinished(playerData.unlocked.contains(chosenNode.location));
        }
        addRenderableWidget(chosenLabel);
        updatePrevAndNextLabels(chosenNode);
        this.showSub = true;
        titleLabel.visible = false;
    }

    private void updatePrevAndNextLabels(ClientTechTreeNode chosenNode) {
        for (ClientTechTreeNode node : chosenNode.getPrevNode()) {
            TechTreeLabel label = TechTreeLabel.smallLabel(node, 0, 0, Component.empty());
            if (ClientPlayerData.getData().isPresent()) {
                PlayerData data = ClientPlayerData.getData().get();
                label.setFinished(data.unlocked.contains(label.getNode().location));
            }
            prevLabels.add(label);
        }
        for (ClientTechTreeNode node : chosenNode.getNextNode()) {
            TechTreeLabel label = TechTreeLabel.smallLabel(node, 0, 0, Component.empty());
            if (ClientPlayerData.getData().isPresent()) {
                PlayerData data = ClientPlayerData.getData().get();
                label.setFinished(data.unlocked.contains(label.getNode().location));
            }
            nextLabels.add(label);
        }
    }

    public void clearSub() {
        removeWidget(chosenLabel);
        chosenLabel = null;
        prevLabels.forEach(this::removeWidget);
        nextLabels.forEach(this::removeWidget);
        prevLabels.clear();
        nextLabels.clear();
        showSub = false;
        titleLabel.visible = true;
    }

    private void renderTooltip(PoseStack poseStack, TechTreeLabel label,
                               TechTreeItemButton grpBtn,
                               int mouseX, int mouseY, float partial) {
        if (label == null && grpBtn == null) return;
        int tooltipX, tooltipY, smallerX;
        if (grpBtn != null) {
            ClientTechTreeGroup group = grpBtn.getGroup();
            if (tooltip == null) {
                tooltip = Tooltip.fromGroup(group);
            } else if (!tooltip.is(group)) {
                tooltip = Tooltip.fromGroup(group);
            }
            tooltipX = grpBtn.x + grpBtn.getWidth();
            tooltipY = grpBtn.y;
            smallerX = grpBtn.x;
        } else {
            ClientTechTreeNode node = label.getNode();
            if (tooltip == null) {
                tooltip = Tooltip.fromNode(node);
            } else if (!tooltip.is(node)) {
                tooltip = Tooltip.fromNode(node);
            }
            tooltipX = label.x + label.getWidth();
            tooltipY = label.y;
            smallerX = label.x;
        }
        if (tooltipX + 1 + tooltip.getWidth() <= windowWidth)
            tooltip.setPosition(tooltipX + 1, tooltipY);
        else
            tooltip.setPosition(smallerX - tooltip.getWidth() - 1, tooltipY);
        tooltip.render(poseStack, mouseX, mouseY, partial);
    }

    private int getBgX() {
        return Math.round(bgX);
    }

    private int getBgY() {
        return Math.round(bgY);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.confirmButton.setRenderMask(this.confirmButton.isMouseOver(mouseX, mouseY));
        if (chosenGroup != null)
            groupChosenFrame.get().renderToGui();
        if (showSub) {
            panels.forEach((g, p) -> p.visible = false);
            renderSubArrows(scale);
            if (nodeTitleComponent != null) {
                font.draw(poseStack, nodeTitleComponent,
                        bgX + map(39, scale),
                        bgY + map(16, scale), 0xffffff);
            }
            if (mainPercentage > 0) {
                this.mainArrow.get().renderToGui();
            }
            renderSlotItemTooltip(poseStack, mouseX, mouseY);
        }
        TechTreeItemButton button = null;
        for (TechTreeItemButton btn : groupButtons) {
            if (btn.isMouseOver(mouseX, mouseY) && btn.visible) {
                button = btn;
                break;
            }
        }
        TechTreeLabel label = getChosenLabel(mouseX, mouseY);
        if (label == null) return;
        renderTooltip(poseStack, label, button, mouseX, mouseY, partialTick);
    }

    public void renderSlotItemTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        for (ItemSlot slot : consumptionSlots) {
            if (slot.getItemStack() != null &&
                    !slot.getItemStack().equals(ItemStack.EMPTY) &&
                    slot.isMouseOver(mouseX, mouseY)) {
                renderItemTooltip(poseStack, slot.getItemStack(), mouseX, mouseY);
            }
        }
        for (ItemSlot slot : resultSlots) {
            if (slot.getItemStack() != null &&
                    !slot.getItemStack().equals(ItemStack.EMPTY) &&
                    slot.isMouseOver(mouseX, mouseY)) {
                renderItemTooltip(poseStack, slot.getItemStack(), mouseX, mouseY);
            }
        }
    }

    public void renderItemTooltip(PoseStack poseStack, ItemStack item, int mouseX, int mouseY) {
        this.renderTooltip(poseStack, this.getTooltipFromItem(item), item.getTooltipImage(), mouseX, mouseY);
    }

    public @Nullable TechTreeLabel getChosenLabel(double mouseX, double mouseY) {
        if (!showSub) {
            if (chosenGroup == null || !panels.containsKey(chosenGroup)) return null;
            if (!panels.get(chosenGroup).visible) return null;
            return panels.get(chosenGroup).getChosenLabel(mouseX, mouseY);
        } else {
            if (chosenLabel != null && chosenLabel.isMouseOver(mouseX, mouseY))
                return chosenLabel;
            if (prevGridIndex != -1) {
                TechTreeLabel label = prevGrids.get(prevGridIndex)
                        .getChosenLabel(mouseX, mouseY);
                if (label != null) return label;
            }
            if (nextGridIndex != -1) {
                return nextGrids.get(nextGridIndex)
                        .getChosenLabel(mouseX, mouseY);
            }
        }
        return null;
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        // super.renderLabels(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (showSub) {
            if (nodeFinished) {
                return finishedTooltip.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
            }
            return false;
        }
        boolean flag = super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        for (Map.Entry<ClientTechTreeGroup, TechTreePanel> entry : panels.entrySet()) {
            flag |= entry.getValue().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        return flag;
    }

}
