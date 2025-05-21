package willow.train.kuayue.systems.editable_panel.screens;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.render.SimpleColor;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.util.LazyRecomputable;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import willow.train.kuayue.block.panels.block_entity.EditablePanelEntity;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.initial.ClientInit;
import willow.train.kuayue.network.c2s.DiscardChangeC2SPacket;
import willow.train.kuayue.network.c2s.NbtC2SPacket;
import willow.train.kuayue.systems.editable_panel.EditablePanelEditMenu;
import willow.train.kuayue.systems.editable_panel.widget.*;

/**
 * 水牌的GUI呈现
 * GUI开发的第一步就是要有一个UI。MC的GUI分为Screen，即窗口；是GUI的总呈现器。一个这些Widget与玩家的交互
 *
 * @author 童话的爱
 * @since 2025-03-29
 */
// 这里AbstractContainerScreen后面的泛型要填你的menu类。
public class LaqueredScreen
        extends CustomScreen<EditablePanelEditMenu, EditablePanelEntity>
//        extends AbstractContainerScreen<LaqueredMenu>
{

    // 以下代码来自于 src\main\java\willow\train\kuayue\systems\editable_panel\screens\TypeScreen.java
    private int color;
    public boolean revert;
    public Label titleLabel;
    public ImageButton mirrorBtn, cancelBtn, confirmBtn;
    public EditBar editBar;
    public ColorScreenBundles colorEditor;
    private OffsetEditor offsetEditor;
    private float bgX = 0, bgY = 0, scale = 1.0f;
    private boolean showSub, hasJei;

    private final LazyRecomputable<ImageMask> cancelBtnImage =
            new LazyRecomputable<>(() -> GetShareTemplateScreen.cancelImage.get().copyWithOp(p -> p));

    private final LazyRecomputable<ImageMask> acceptBtnImage =
            new LazyRecomputable<>(() -> GetShareTemplateScreen.acceptImage.get().copyWithOp(p -> p));

    private final LazyRecomputable<ImageMask> mirrorBtnImage =
            new LazyRecomputable<>(() -> ColorTemplateScreen.buttons.get()
                    .copyWithOp(p -> p.rectangleUV(.125f, .375f, .25f, .5f)));

    public static final LazyRecomputable<ImageMask> laqueredBoardLogo = new LazyRecomputable<>(
            () -> ClientInit.laqueredBoardLogo.getImageSafe().get().getMask()
                    .rectangleUV(0, 0, 1, 1)
    );

    public static final LazyRecomputable<ImageMask> laqueredBoardWhiteBg = new LazyRecomputable<>(
            () -> ClientInit.laqueredBoardWhiteBg.getImageSafe().get().getMask()
                    .rectangleUV(48f / 128f, 64f / 128f, 64f / 128f, 80f / 128f)
    );

    public static final LazyRecomputable<ImageMask> leftLaqueredColorBoard = new LazyRecomputable<>(
            () -> ClientInit.laqueredBoardWhiteBg.getImageSafe().get().getMask()
                    .rectangleUV(0, 0, 1, 1)
    );
    public static final LazyRecomputable<ImageMask> rightLaqueredColorBoard = new LazyRecomputable<>(
            () -> ClientInit.laqueredBoardWhiteBg.getImageSafe().get().getMask()
                    .rectangleUV(0, 0, 1, 1)
    );

    public static final Integer
            YELLOW = 16776961,
            YELLOW2 = 16776960,
            RED = 15216648,
            BLUE = 22220,
            BLUE2 = 0x60A0B0,
            BLUE3 = 468326,
            BLUE4 = 0x2B4CA1,
            WHITE = 0xFFFFFF,
            BLACK = 789516;

    private static SimpleColor colorBarColor = SimpleColor.BLACK;

    /**
     * @param pMenu
     * @param pPlayerInventory
     * @param pTitle
     */
    public LaqueredScreen(AbstractContainerScreen<EditablePanelEditMenu> editablePanelEditMenuAbstractContainerScreen, CompoundTag compoundTag) {
        super(editablePanelEditMenuAbstractContainerScreen, compoundTag);

        setBlockEntity(editablePanelEditMenuAbstractContainerScreen.getMenu().getEditablePanelEntity());
        editBar = new EditBar(0, 0, Component.empty(), "");
        // 检测是否加了jei mod，以防止打开screen时被右侧栏占用
        this.hasJei = ModList.get().isLoaded("jei");
    }


    /**
     * 初始化screen 时添加一个label 组件
     * 该init方法会在GUI最终开始显示之前调用。
     * 有一些参数(或方法)在Screen构造的时候是不存在(或不可用)的，那么若要调用这些参数，就都要写在init()方法里
     */
    @Override
    public void init() {
        Minecraft instance = Minecraft.getInstance();
        if (instance.screen == null) return;
        int sW = instance.screen.width;
        int sH = instance.screen.height;
// 获取窗口的 GUI 缩放尺寸（更准确，考虑 UI 缩放）
        Window window = instance.getWindow();
        int guiScaledWidth = window.getGuiScaledWidth();
        int guiScaledHeight = window.getGuiScaledHeight();

        // 获取 ImageMask 背景
        ImageMask imageMask = laqueredBoardWhiteBg.get();
        ImageMask logoIm = laqueredBoardLogo.get();
        ImageMask leftColorBoard = leftLaqueredColorBoard.get();
        ImageMask rightColorBoard = rightLaqueredColorBoard.get();


        // 初始化颜色编辑器组件
        colorEditorInit();
        // 初始化按钮组件
        buttonsInit();

        Font font = instance.font;
        CompoundTag nbt = getNbt();
        // 获取可编辑面板实体对象
        EditablePanelEntity entity = getScreen().getMenu().getEditablePanelEntity();
        // 获取实体的颜色并赋值给类成员变量 color
        color = entity.getColor();
        // 存储从 NBT 数据中读取的文本值
        String[] values = new String[4];
        values[0] = nbt.getString("left_top");
        values[1] = nbt.getString("left_bottom");
        values[2] = nbt.getString("right_top");
        values[3] = nbt.getString("right_bottom");

        innerInit(values, color, font, revert);


        cancelBtn.setOnClick((w, x, y) -> {
            AllPackets.CHANNEL.sendToServer(new DiscardChangeC2SPacket(entity.getBlockPos()));
            this.close();
        });

        confirmBtn.setOnClick((w, x, y) -> {
            BlockPos pos = entity.getBlockPos();
//            SimpleColor.fromRGBA()
            nbt.putInt("color", this.color);
//            nbt.putBoolean("revert", this.revert);
            Pair<Float, Float> offset = offsetEditor.getCursorPosition();
            nbt.putFloat("offset_x", offset.getFirst());
            nbt.putFloat("offset_y", offset.getSecond());
            TransparentEditBox[] boxes = new TransparentEditBox[5];
            int counter = 0;
            for (Widget widget : getWidgets()) {
                if (widget instanceof TransparentEditBox box) {
                    boxes[counter] = box;
                    counter++;
                }
            }


            nbt.putString("left_top", boxes[0].getValue());
            nbt.putString("left_bottom", boxes[1].getValue());
            nbt.putString("right_top", boxes[2].getValue());
            nbt.putString("right_bottom", boxes[3].getValue());

            CompoundTag tag = new CompoundTag();
            tag.put("data", nbt);
            entity.load(tag);
            entity.markUpdated();
            AllPackets.CHANNEL.sendToServer(new NbtC2SPacket(pos, tag));
            this.close();
        });
    }

    private void innerInit(String[] values, int color, Font font, boolean revert) {
        // 设置文本缩放因子
        float textScaleFactor = 11f;
        float textScaleFactorForEnglish = 16f;

// 获取窗口和字体信息
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
//        Font font = minecraft.font;

// 动态获取当前GUI缩放后的尺寸
        double guiScale = window.getGuiScale();
        int guiScaledWidth = window.getGuiScaledWidth();
        int guiScaledHeight = window.getGuiScaledHeight();
        float heightMarginPixel = guiScaledHeight * 0.015f;

// 计算文本显示尺寸（考虑缩放因子）
        int width0 = font.width(values[0]);
        float size0 = width0 * textScaleFactor;
        double v0TextCount = (width0 / guiScale) / 9;
        int width1 = font.width(values[1]);
        float size1 = width1 * textScaleFactorForEnglish;
        double v1TextCount = (width1 / guiScale) / 6;
        int width2 = font.width(values[2]);
        float size2 = width2 * textScaleFactor;
        double v2TextCount = (width2 / guiScale) / 9;
        int width3 = font.width(values[3]);
        float size3 = width3 * textScaleFactorForEnglish;
        double v3TextCount = (width3 / guiScale) / 6;


        float bgImageXStarter = (float) (guiScaledWidth * 0.15); // 偏移至从左到右第25%的位置
        float bgImageYStarter = (float) (guiScaledHeight * 0.3); // 偏移至从下到上第35%的位置
        float imageBgWidth = (float) (guiScaledWidth * 0.70); // 宽度为窗口宽度的70%，
        float imageBgHeight = (float) (guiScaledHeight * 0.3); // 高度为窗口高度的40%
//        float colorBarHeightStart = (bgImageYStarter + imageBgHeight * 0.70f); // 高度为   // todo 可能会到上面去
        float colorBarHeightStart = (bgImageYStarter + imageBgHeight * 0.70f); // 高度为   // todo 可能会到上面去
        float colorBarHeightEnd = (bgImageYStarter + imageBgHeight) * 1f; // 高度为
        float leftColorBarXStarter = (float) (guiScaledWidth * 0.15); // 偏移至从左到右第25%的位置
        float colorBarWidth = (float) (imageBgWidth * 0.4); // 彩条宽度为水牌宽度的20%
        float colorBarHeight = (float) (imageBgHeight * 0.25); // 彩条高度为水牌高的23%
        float leftColorBarXEnd = (float) ((guiScaledWidth * 0.15) + (imageBgWidth * 0.4)); // 0.4+0.4 -> 0.8
        float rightColorBarXEnd = (float) ((guiScaledWidth * 0.15) + imageBgWidth); // 偏移至从左到右第25%的位置
        // 使logo的大小是水牌背景高度的20%
        double logoWidthHalf = imageBgHeight * 0.7;
        float rightColorBarXStarter = (float) ((imageBgWidth * 0.6) + (imageBgWidth * 0.2133));
//                + (imageBgWidth * 0.475) + ( imageBgHeight * 0.40)
        // 偏移至从左到右第25%的位置
        float logoXStarter = (float) ((guiScaledWidth / 2) - (logoWidthHalf / 2)); //
        float logoYStarter = (float) (bgImageYStarter + ((imageBgHeight / 2) - (logoWidthHalf / 2))); //

        // 中间基线
        float middleLineY = (float) (bgImageYStarter + (imageBgHeight / 2));
        float middleLineX = (float) (guiScaledWidth / 2);

        // 计算得出文字应在当前屏幕x y 时的文字比例（修改文字缩放参数）
        double nonEngScaleNumber = (bgImageXStarter / 100) * 0.5;
//        double nonEngScaleNumber = (imageBgWidth * 0.2) + bgImageXStarter;
        float resizedTextScaleFactor = (float) ((textScaleFactor * nonEngScaleNumber));
        double engLineScaleNumber = (bgImageXStarter / 100) * 0.15;
//        double engLineScaleNumber = (imageBgWidth * 0.3) + bgImageXStarter;
        float resizedTextScaleFactorForEnglish = (float) (textScaleFactorForEnglish * engLineScaleNumber);

        // 左边距
//        float leftMoJiMargin = (float) (nonEngScaleNumber * imageBgWidth);
//        font.width(values[0]) * 3.55
//        float leftMoJiMargin = (float) (font.width(values[0]) * 3.55);
//        float leftMoJiMargin = (float) 80f; // calculateDynamicMargin(values[0], font, imageBgWidth);

//        float leftMoJiMargin = (float) calculateDynamicMargin(values[0], font, imageBgWidth);
        // 为了解决文字变长时锚点不在固定的中间位置问题，需先将锚点x定位到水牌纸面中央，然后再根据文字长度 / 2 反向（给反方向的x值，负值）偏移锚点
        float leftMoJiMargin = 0f;
        if (v0TextCount <= 2) { // 0.5 一个字？  font.width(values[0]) 4中文 36float 一个字9f
            // 左边距中文计算公式
//            leftMoJiMargin = (float) Math.max (允许的最小值, ((最大字符数 - 当前字符数 ) / 除以2即留白的一半) * (画面缩放宽度 * 的百分之25));
            leftMoJiMargin = (float) Math.max (0, ((2 - v0TextCount ) / 2) * (guiScaledWidth * 0.15));
        }

        float leftEngMoJiMargin = 0f;
        if (v1TextCount <= 13) { // 0.5 一个字？  font.width(values[0]) 4中文 36float 一个字9f
//            leftMoJiMargin = (float) Math.max (允许的最小值, ((最大字符数 - 当前字符数 ) / 除以2即留白的一半) * (画面缩放宽度 * 的百分之25));
            // todo 2025-05-20 英文文字的边距计算 目前左边距计算扔有偏差（在多字和少字均仍有瑕疵），超出13字后不可自动缩放
            leftEngMoJiMargin = (float) Math.max (0, ((13 - v1TextCount*2 ) / 2) * (guiScaledWidth * 0.02));
        }
        float rightMoJiMargin = 0f;
        if (v2TextCount <= 2) { // 0.5 一个字？  font.width(values[0]) 4中文 36float 一个字9f
            // 左边距中文计算公式
//            leftMoJiMargin = (float) Math.max (允许的最小值, ((最大字符数 - 当前字符数 ) / 除以2即留白的一半) * (画面缩放宽度 * 的百分之25));
            rightMoJiMargin = (float) Math.max (0, ((2 - v2TextCount ) / 2) * (guiScaledWidth * 0.15));
        }
        float rightEngMoJiMargin = 0f;
        if (v3TextCount <= 13) { // 0.5 一个字？  font.width(values[0]) 4中文 36float 一个字9f
//            leftMoJiMargin = (float) Math.max (允许的最小值, ((最大字符数 - 当前字符数 ) / 除以2即留白的一半) * (画面缩放宽度 * 的百分之25));
            // todo 2025-05-20 英文文字的边距计算 目前左边距计算扔有偏差（在多字和少字均仍有瑕疵），超出13字后不可自动缩放
            rightEngMoJiMargin = (float) Math.max (0, ((13 - v3TextCount*2 ) / 2) * (guiScaledWidth * 0.02));
        }
//        if (width1 <= 13 *4) {
////            leftMoJiMargin = (float)
////                    (
////                            (bgImageXStarter * 0.75) +
////                                    ((imageBgWidth / 2) * -0.275) + (width0 * 0.5)
////                    );
// leftEngMoJiMargin = (float) ((bgImageXStarter ) + ((imageBgWidth / 2)) + (width1 * 10));
//        }
//        float rightMoJiMargin = (float) (nonEngScaleNumber);
//        float rightEngMoJiMargin = (float) (engLineScaleNumber);

//        int bgHeight = (int) (170 * scale); // 固定高度或按需调整
//        double logoWidthHalf = bgHeight * 0.55;

//// 计算布局参数
//        int lineHeight = (int)(font.lineHeight * textScaleFactor);
//        int lineHeightForEnglish = (int)(font.lineHeight * textScaleFactorForEnglish);
//        int verticalSpacing = lineHeight / 2; // 行间距
//        int chineseCharWidth = (int)(logoWidthHalf * textScaleFactor * 1.2f); // 基于实际中文字符宽度
//        int chineseCharWidthForEnglish = (int)(logoWidthHalf * textScaleFactorForEnglish * 1.2f); // 基于实际中文字符宽度
//
//// 计算行宽
//        float firstLineWidth = size0 + chineseCharWidth + size1;
//        float secondLineWidth = size2 + chineseCharWidth + size3;
//        float maxLineWidth = Math.max(firstLineWidth, secondLineWidth);
//
//// 动态计算基准位置（考虑窗口安全边距）
//        int minMargin = 20; // 最小边距
//        int baseX = (int)Math.max(minMargin, (guiScaledWidth - maxLineWidth) / 2);
//        int baseY = (int)((guiScaledHeight - (lineHeight * 2 + verticalSpacing)) / 2);

// 始发地布局（values[0]和values[1]）
//        int firstLineY = baseY;
        int lineFontMaxWidth = (int) (guiScaledWidth * 0.28);
        addWidget(new TransparentEditBox(font,
                        // 使其根据文本内容大小偏移
//                baseX +  font.width(values[0]),
//                firstLineY,
//                        v0TextCount < 4 ? (int) ((leftMoJiMargin+ bgImageXStarter) + ((4 - v0TextCount) * 20.1f ) / 2)
//                                :
                        (int) (leftMoJiMargin + bgImageXStarter),
                        (int) (bgImageYStarter + heightMarginPixel),
                        (int) width0,
                        font.lineHeight,
//                (float) lineFontMaxWidth / width0,
//                        (width0 + (width0 * 0.28) < lineFontMaxWidth) && guiScaledWidth < 1080 ? resizedTextScaleFactor
//                                :
                        v0TextCount < 2.5 ? resizedTextScaleFactor : (float) lineFontMaxWidth / width0,
                        resizedTextScaleFactor,
                        Component.empty(),
                        values[0],
                        BLACK)
        );
//        int secondLineY = baseY + lineHeight + verticalSpacing;
        addWidget(new TransparentEditBox(font,
//                (int) (leftEngMoJiMargin + bgImageXStarter),
//                v1TextCount < 12 ? (int) ((leftEngMoJiMargin+ bgImageXStarter) + ((12 - v1TextCount) * 13f ) / 2)
//                        :
                (int) (leftEngMoJiMargin + bgImageXStarter),
                (int) (colorBarHeightStart + heightMarginPixel),
                width1,
                font.lineHeight,
//                width1 < lineFontMaxWidth ? resizedTextScaleFactorForEnglish : (float) lineFontMaxWidth / width1,
                v1TextCount <= 6 ? resizedTextScaleFactorForEnglish : (float) lineFontMaxWidth / width1,
                resizedTextScaleFactorForEnglish,
                Component.empty(),
                values[1],
                WHITE));
        // 目的地布局（values[2]和values[3]）
        addWidget(new TransparentEditBox(font,
                // imageBgWidth * 0.6 用于将文字偏移到logo右边
//                (int) (rightMoJiMargin + (bgImageXStarter + (imageBgWidth * 0.6))),
                (int) (rightMoJiMargin + (bgImageXStarter + (imageBgWidth * 0.6))),
                (int) (bgImageYStarter + heightMarginPixel),  //firstLineY,
                width2,
                font.lineHeight,
//                width2 < lineFontMaxWidth ? resizedTextScaleFactor : (float) lineFontMaxWidth / width2,
                v2TextCount < 2.5 ? resizedTextScaleFactor : (float) lineFontMaxWidth / width2,
                resizedTextScaleFactor,
                Component.empty(),
                values[2],
                BLACK));


        addWidget(new TransparentEditBox(font,
                // imageBgWidth * 0.6 用于将文字偏移到logo右边
                (int) (rightEngMoJiMargin + (bgImageXStarter + (imageBgWidth * 0.6))),
                (int) (colorBarHeightStart + heightMarginPixel), // secondLineY,
                width3,
                font.lineHeight,
//                width3 < lineFontMaxWidth ? resizedTextScaleFactorForEnglish : (float) lineFontMaxWidth / width3,
                v3TextCount <= 6 ? resizedTextScaleFactorForEnglish : (float) lineFontMaxWidth / width3,
                resizedTextScaleFactorForEnglish,
                Component.empty(),
                values[3],
                WHITE));
    }

    /**
     * 根据文字长度计算动态偏移量
     *
     * @param text 待测量的文字
     * @param font
     * @return 水平偏移量（像素值）
     */
    @Deprecated
    public static float calculateDynamicMargin(String text, Font font, float bgWidth) {
//        int textWidth = font.width(text);
//        int baseMargin = 0; // 基础偏移量（可根据需求调整）
//
//        // 动态计算规则
//        if (text.length() > 10) {
//            return -baseMargin - (text.length() - 10); // 长度超10时负偏移
//        } else {
//            return baseMargin + (10 - text.length()); // 长度≤10时正偏移
//        }
        // 基础参数（可配置化） 当中文那行是 北京 时，则边距应为80f 以使其在中间
        final float BASE_MARGIN = 80f;    // 基础偏移量
        final float LENGTH_THRESHOLD = 4f; // 长度临界点
        final float PIXEL_PER_CHAR = 1f;  // 每字符补偿系数

        // 获取有效长度（考虑Unicode组合字符）
//        int codePointCount = text.codePointCount(0, text.length());
        int codePointCount = font.width(text) / 9;
        float lengthFactor = codePointCount / LENGTH_THRESHOLD;

        // 动态计算规则 当codePointCount / LENGTH_THRESHOLD 大于1则代表超出 LENGTH_THRESHOLD 中规定的文字长度限制，否则未超出限制
        if (lengthFactor > 1.0f) {
            // 超长文本负偏移公式：-baseMargin - (extraLength * compensation)
            return -BASE_MARGIN - ((codePointCount - LENGTH_THRESHOLD));
        } else {
            // 短文本正偏移公式：baseMargin + (remainingSpace * compensation)
            return BASE_MARGIN + ((LENGTH_THRESHOLD - codePointCount) * 0.6f);
        }
    }

//    public void buttonsInnerInit(Font font, float textScaleFactor, float size0,
//                                 float size1, float size2, float size3
////            , float size4
//            , int sW, int sH) {
//
//        // todo 这里是否也有用于缩放文本的特定系数?
//        int height = font.lineHeight;
//        int labelW = (int) (size1 * 1.05f + size2 + size3 * 1.4f
////                + size4
//        );
//        int labelH = (int) (height * 0.18f * textScaleFactor - 23 + textScaleFactor * height * 0.13f);
//        int basicX = (sW - labelW) / 2 + 20, basicY = (sH - labelH) / 2 - 10;
//        titleLabel.setWidth(font.width(titleLabel.getPlainText()));
//        titleLabel.setPosition((float) (sW - titleLabel.getWidth()) / 2, basicY - 20);
//
//        int btnY = basicY + labelH + 20;
//        colorEditor.getColorBtn().setPos(basicX, btnY);
//        colorEditor.getTemplateBtn().setPos(basicX + 20, btnY);
//        cancelBtn.setPos(basicX + labelW - 60, btnY);
//        confirmBtn.setPos(basicX + labelW - 80, btnY);

    /// /        mirrorBtn.setPos(basicX + 40, btnY);
//        offsetEditor.getEditorBtn().setPos(basicX + labelW - 100, btnY);
//    }
    public void buttonsInit() {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
// 动态获取当前GUI缩放后的尺寸
        int guiScaledWidth = window.getGuiScaledWidth();
        int guiScaledHeight = window.getGuiScaledHeight();

        float bgImageXStarter = (float) (guiScaledWidth * 0.15); // 偏移至从左到右第25%的位置
        float bgImageYStarter = (float) (guiScaledHeight * 0.3); // 偏移至从下到上第35%的位置
        float imageBgWidth = (float) (guiScaledWidth * 0.70); // 宽度为窗口宽度的70%，
        float imageBgHeight = (float) (guiScaledHeight * 0.3); // 高度为窗口高度的40%

        int offsetButtonX = (int) (bgImageXStarter);
        int offsetButtonY = (int) (bgImageYStarter + imageBgHeight + 16);
        titleLabel = new Label(Component.translatable("tooltip.kuayue.type_screen.title"));
        titleLabel.setPosition(bgImageXStarter, bgImageYStarter - 20);
        addWidget(titleLabel);

        mirrorBtn = new ImageButton(mirrorBtnImage, offsetButtonX + (16), offsetButtonY, 16, 16, Component.empty(), b -> {
            revert = !revert;
            refresh();
        });

        offsetEditor = new OffsetEditor(offsetButtonX + (16 * 2), offsetButtonY, Component.literal("offset"),
                -.5f, .5f, -.5f, .5f, 0f, 0f);
        offsetEditor.setPosition((Minecraft.getInstance().screen.width - offsetEditor.getWidth()) / 2,
                (Minecraft.getInstance().screen.height - offsetEditor.getHeight()) / 2);
        offsetEditor.visible = false;

        cancelBtn = new ImageButton(cancelBtnImage, offsetButtonX + (16 * 3), offsetButtonY, 16, 16, Component.empty(), b -> {
        });
        confirmBtn = new ImageButton(acceptBtnImage, offsetButtonX + (16 * 4), offsetButtonY, 16, 16, Component.empty(), b -> {
        });

        editBar.onCancelClick((w, x, y) -> editBar.visible = false);
        editBar.visible = false;
        offsetEditor.onCancelBtnClick(((widget, mouseX, mouseY) -> {
            setBoardWidgetVisible(true);
            offsetEditor.visible = false;
        }));
        offsetEditor.onEditorBtnClick((widget, mouseX, mouseY) -> {
            setBoardWidgetVisible(false);
            offsetEditor.visible = true;
            offsetEditor.setCursorPosition(getNbt().getFloat("offset_x"), getNbt().getFloat("offset_y"));
        });
        offsetEditor.onAcceptBtnClick((widget, mouseX, mouseY) -> {
            setBoardWidgetVisible(true);
            Pair<Float, Float> offset = offsetEditor.getCursorPosition();
            getNbt().putFloat("offset_x", offset.getFirst());
            getNbt().putFloat("offset_y", offset.getSecond());
            getBlockEntity().saveNbt(this.getNbt());
            offsetEditor.visible = false;
        });

        addWidget(cancelBtn);
        // todo 2025-05-05 点击提交按钮后会恢复成初始值，且设定的内容没有同步到服务器
        addWidget(confirmBtn);
        addWidget(editBar);
        ImageButton editorBtn = offsetEditor.getEditorBtn();
        editorBtn.setPos(offsetButtonX + (16 * 5), offsetButtonY);
        addWidget(editorBtn);
        addWidget(mirrorBtn);
        ImageButton colorBtn = colorEditor.getColorBtn();
        colorBtn.setPos(offsetButtonX + (16 * 6), offsetButtonY);
        // todo 2025-05-05 颜色调好后点提交按钮后颜色没有同步到服务器，且只有右键点开编辑界面才会显示改后的颜色
        addWidget(colorBtn);
        ImageButton templateBtn = colorEditor.getTemplateBtn();
        templateBtn.setPos(offsetButtonX + (16 * 7), offsetButtonY);
        addWidget(templateBtn);
        addWidget(offsetEditor);
    }

    private void refresh() {
        if (editBar.visible) return;
        String[] values = new String[4];
        int counter = 0;
        int focus = -1;
        int focusIndex = -1;
        for (Widget w : getWidgets()) {
            if (!(w instanceof TransparentEditBox box)) continue;
            values[counter] = box.getValue();
            if (box.isFocused()) {
                focus = counter;
                focusIndex = box.getCursorPosition();
            }
            counter++;
        }
        clearWidgets();

        addWidget(cancelBtn);
        addWidget(confirmBtn);
        addWidget(titleLabel);
        addWidget(editBar);
        addWidget(mirrorBtn);
        addWidget(colorEditor);
        addWidget(colorEditor.getColorBtn());
        addWidget(colorEditor.getTemplateBtn());
        addWidget(offsetEditor.getEditorBtn());
        addWidget(offsetEditor);

        clearLabels();
        Font font = Minecraft.getInstance().font;
        CompoundTag nbt = getNbt();
        int color = getScreen().getMenu().getEditablePanelEntity().getColor();
        innerInit(values, color, font, revert);
        if (focus > -1 && focusIndex > -1) {
            Widget w = getWidgets().get(focus);
            if (!(w instanceof TransparentEditBox box)) return;
            box.setFocus(true);
            box.setCursorPosition(focusIndex);
        }
    }

    public void colorEditorInit() {
        colorEditor = new ColorScreenBundles();
        colorEditor.init();
        colorEditor.setOpen((selector, template, now) -> {
            selector.setRgb(this.color);
            setBoardWidgetVisible(false);
        });
        colorEditor.setCancel((selector, template, now) -> {
            setBoardWidgetVisible(true);
        });
        colorEditor.setSuccess((selector, template, now) -> {
            if (now == template) {
                this.color = template.getChosenBox().getTemplate().getColor();
                this.setTextColor(color);
                colorBarColor = SimpleColor.fromRGBAInt(template.getChosenBox().getTemplate().getColor());
                setBoardWidgetVisible(true);
            } else {
                this.color = selector.getColor().getRGB();
                setTextColor(color);
                colorBarColor = SimpleColor.fromRGBAInt(template.getChosenBox().getTemplate().getColor());
                setBoardWidgetVisible(true);
            }
        });
        colorEditor.visible = false;
        addWidget(colorEditor);
    }

    /**
     * 渲染背景
     * (一般来说，是GUI从开始显示到最终关闭都会存在的静态渲染对象)
     *
     * @param pose
     * @param mouseX
     * @param mouseY
     * @param partialTick
     **/
    @Override
    public void renderBackGround(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        Minecraft instance = Minecraft.getInstance();
        if (instance.screen == null) return;
        int sW = instance.screen.width;
        int sH = instance.screen.height;
// 获取窗口的 GUI 缩放尺寸（更准确，考虑 UI 缩放）
        Window window = instance.getWindow();
        int guiScaledWidth = window.getGuiScaledWidth();
        int guiScaledHeight = window.getGuiScaledHeight();

        // 获取 ImageMask 背景
        ImageMask imageMask = laqueredBoardWhiteBg.get();
        ImageMask logoIm = laqueredBoardLogo.get();
        ImageMask leftColorBoard = leftLaqueredColorBoard.get();

        // starter ender 都是锚点，width是宽度，height是高度
        // 确定水牌图片背景的左上角x百分比  0.25-> 0.15   0.35 -> 0.3
        float bgImageXStarter = (float) (guiScaledWidth * 0.15); // 偏移至从左到右第25%的位置
        float bgImageYStarter = (float) (guiScaledHeight * 0.3); // 偏移至从下到上第35%的位置
        float imageBgWidth = (float) (guiScaledWidth * 0.70); // 宽度为窗口宽度的70%，
        float imageBgHeight = (float) (guiScaledHeight * 0.3); // 高度为窗口高度的40%
//        float colorBarHeightStart = (bgImageYStarter + imageBgHeight * 0.70f); // 高度为   // todo 可能会到上面去
        float colorBarHeightStart = (bgImageYStarter + imageBgHeight * 0.70f); // 高度为   // todo 可能会到上面去
        float colorBarHeightEnd = (bgImageYStarter + imageBgHeight) * 1f; // 高度为
        float leftColorBarXStarter = (float) (guiScaledWidth * 0.15); // 偏移至从左到右第25%的位置
        float colorBarWidth = (float) (imageBgWidth * 0.4); // 彩条宽度为水牌宽度的20%
        float colorBarHeight = (float) (imageBgHeight * 0.25); // 彩条高度为水牌高的23%
        float leftColorBarXEnd = (float) ((guiScaledWidth * 0.15) + (imageBgWidth * 0.4)); // 0.4+0.4 -> 0.8
        float rightColorBarXEnd = (float) ((guiScaledWidth * 0.15) + imageBgWidth); // 偏移至从左到右第25%的位置
        // 使logo的大小是水牌背景高度的20%
        double logoWidthHalf = imageBgHeight * 0.7;
        float rightColorBarXStarter = (float) ((imageBgWidth * 0.6) + (imageBgWidth * 0.2133));
//                + (imageBgWidth * 0.475) + ( imageBgHeight * 0.40)
        // 偏移至从左到右第25%的位置
        float logoXStarter = (float) ((guiScaledWidth / 2) - (logoWidthHalf / 2)); //
        float logoYStarter = (float) (bgImageYStarter + ((imageBgHeight / 2) - (logoWidthHalf / 2))); //


        // 计算背景尺寸（宽度和高度）
//        int bgWidth = (int) (windowWidth * (hasJei ? .7f : .9f));
//        int bgHeight = (int) (150 * scale);
//        int bgWidth = (int) (guiScaledWidth * (hasJei ? 0.6f : 0.8f)); // 动态宽度（70% 或 90% 窗口宽度）
//        int bgHeight = (int) (170 * scale); // 固定高度或按需调整
//        // 计算背景的起始坐标 (bgX, sH)，使其与 values[0] 对齐
//        int bgX = (guiScaledWidth  - bgWidth) / 2;  // 与 values[0] 的 basicX 计算方式一致
//        int sH = (guiScaledHeight  - bgHeight) / 2;  // 与 values[0] 的 basicY 计算方式一致
        // 确保背景的 x 和 y 原点与 values[0] 对齐
        imageMask.rectangle(
                new Vector3f(bgImageXStarter, bgImageYStarter, 0),  // 使用与 values[0] 相同的原点
                ImageMask.Axis.X,
                ImageMask.Axis.Y,
                true,
                true,
                imageBgWidth,
                imageBgHeight
        );
        logoIm.rectangle(
//                new Vector3f((guiScaledWidth / 2) - (float)(logoWidthHalf / 2),
                new Vector3f(logoXStarter,
//                        (float)( (guiScaledHeight / 2) - (logoWidthHalf / 2)),
                        logoYStarter,
                        1),
                ImageMask.Axis.X,
                ImageMask.Axis.Y,
                true,
                true,
                (float) logoWidthHalf,
                (float) logoWidthHalf
        );
        leftColorBoard.rectangle(
//                new Vector3f(bgImageXStarter,
                new Vector3f(leftColorBarXStarter,
//                        bgImageYStarter + 200,
                        colorBarHeightStart,
                        1),  // 使用与 values[0] 相同的原点
                ImageMask.Axis.X,
                ImageMask.Axis.Y,
                true,
                true,
//                (float) (imageBgWidth * 0.35),
                colorBarWidth,
//                (float) (imageBgHeigth * 0.15)
                colorBarHeight
        );
        leftColorBoard.setColor(SimpleColor.fromRGBAInt(BLUE));
        ImageMask rightColorBoard = rightLaqueredColorBoard.get();
        rightColorBoard.rectangle(
//                new Vector3f(bgImageXStarter - 400, bgImageYStarter + 200, 2),  // 使用与 values[0] 相同的原点
                new Vector3f(rightColorBarXStarter, colorBarHeightStart, 1),  // 使用与 values[0] 相同的原点
                ImageMask.Axis.X,
                ImageMask.Axis.Y,
                true,
                true,
//                (float) (imageBgWidth * 0.35),
                colorBarWidth,
//                (float) (imageBgHeigth * 0.15)
                colorBarHeight
        );
        rightColorBoard.setColor(SimpleColor.fromRGBAInt(BLUE));
        // 渲染背景
        imageMask.renderToGui();
        rightColorBoard.renderToGui();
        leftColorBoard.renderToGui();
        logoIm.renderToGui();
        // 添加半透明黑色背景覆盖整个屏幕
        GuiComponent.fill(pose, 0, 0, sW, sH, 0x80000000);
    }

    /**
     * Screen的核心渲染方法，用于控制其他所有渲染方法的工作
     *
     * @param pose
     * @param mouseX
     * @param mouseY
     * @param partial
     */
    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partial) {
        super.render(pose, mouseX, mouseY, partial);

        if (offsetEditor == null) return;
    }


//    @Override
//    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
//
//    }


    @Override
    public void mouseClicked(double mouseX, double mouseY, int btn) {
        for (Widget widget : getWidgets()) {
            if (!(widget instanceof GuiEventListener listener)) continue;
            if (!listener.isMouseOver(mouseX, mouseY)) continue;
            if (listener instanceof AbstractWidget widget1 && !widget1.visible) continue;
            if (listener instanceof ColorScreen cs && !cs.getVisible()) continue;
            if (listener instanceof GetShareTemplateScreen screen && !screen.isVisible()) continue;
            if (widget instanceof TransparentEditBox box) {
                editBar.setPosition(box.x + ((int) ((float) box.getWidth() * box.getScaleX()) - 200) / 2,
                        box.y + (int) ((float) box.getHeight() * box.getScaleY()) + 2);
                editBar.setText(box.getValue());
                editBar.onAcceptClick(
                        (w, x, y) -> {
                            box.setValue(editBar.getText());
                            editBar.visible = false;
                            refresh();
                            getBlockEntity().saveNbt(this.getNbt());
                        }
                );
                editBar.visible = true;
                editBar.setFocused(true);
                return;
            }
            listener.mouseClicked(mouseX, mouseY, btn);
            return;
        }
    }

    @Override
    public void renderTooltip(PoseStack pose, int mouseX, int mouseY) {
    }

    /**
     * 该screen 关闭时调用
     */
//    @Override
//    public void onClose() {
//        super.onClose();
//    }
    @Override
    public void charTyped(char code, int modifier) {
        super.charTyped(code, modifier);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * 这个方法是AbstractContainerScreen特有的，默认情况下会在固定位置渲染"物品栏"("Inventory")几个字
     * 如果不想要这几个字的话就把他里面的super调用注释掉
     *
     * @param pPoseStack
     * @param pMouseX
     * @param pMouseY
     */
//    @Override
//    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
////        super.renderLabels(pPoseStack, pMouseX, pMouseY);
//    }
    public void setButtonsVisible(boolean visible) {
        colorEditor.getTemplateBtn().visible = visible;
        colorEditor.getColorBtn().visible = visible;
        this.cancelBtn.visible = visible;
        this.confirmBtn.visible = visible;
        this.titleLabel.visible = visible;
        this.mirrorBtn.visible = visible;
        editBar.visible = false;
        this.offsetEditor.getEditorBtn().visible = visible;
        editBar.setFocused(false);
    }

    public void setBoardWidgetVisible(boolean visible) {
        getWidgets().forEach(w -> {
            if (w instanceof TransparentEditBox box) box.visible = visible;
        });
        setButtonsVisible(visible);
    }

    public void setTextColor(int color) {
        getWidgets().forEach(w -> {
            if (w instanceof TransparentEditBox box) box.setTextColor(color);
        });
    }
}
