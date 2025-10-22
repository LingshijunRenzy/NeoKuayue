package willow.train.kuayue.block.panels.block_entity.renderer;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import willow.train.kuayue.block.panels.block_entity.SingleArmPantographBlockEntity;
import willow.train.kuayue.block.panels.pantograph.IPantographModel;
import willow.train.kuayue.block.panels.pantograph.PantographProps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SingleArmPantographRenderer implements
        BlockEntityRenderer<SingleArmPantographBlockEntity>, IPantographModel {

    public static float STEP_FAST = 0.20f;
    public static float STEP_SLOW = 0.05f;

    public SingleArmPantographRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SingleArmPantographBlockEntity pBlockEntity,
                       float pPartialTick, @NotNull PoseStack pose,
                       @NotNull MultiBufferSource buffer, int light, int overlay) {

        BlockState blockState = pBlockEntity.getBlockState();
        boolean risen = pBlockEntity.isRisen();
        float risenSpeed = pBlockEntity.getRisenSpeed();
        float downPullRodAngle = pBlockEntity.getDownPullRodAngle();
        float risePullRodAngle = pBlockEntity.getRisePullRodAngle();
        double transPosY = pBlockEntity.getTransPosY();
        // 获取玩家朝向
        Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        // 各部件固定参数
        PantographProps pantographProps = pBlockEntity.getPantographType();

        if (!pantographProps.isMapping()) {
            pantographProps.heightListInit(pantographProps, risePullRodAngle, downPullRodAngle);
//            System.out.println(pantographProps.getHeightList().toString());
        }

//        List<PantographProps.PantographMapping> heightList = pantographProps.getHeightList();
//
//        Random random = new Random();
//        double randomNumber =
//                random.nextDouble(heightList.get(0).getBowHeight() -
//                        heightList.get(heightList.size() - 1).getBowHeight()) +
//                        heightList.get(heightList.size() - 1).getBowHeight();
//        double pantographAngleByList =
//                pantographProps.getPantographAngleByList(heightList, randomNumber);
//        System.out.println("randomNumber: " + randomNumber + ".angleByList: " + pantographAngleByList);

        // 建模转缓存
        Map<String, PartialModel> pantographModel = pBlockEntity.getPantographModel();
        if (pantographModel == null)
            return;
        PartialModel baseModel = pantographModel.get(BASE_MODEL);
        PartialModel largeArmModel = pantographModel.get(LARGE_ARM_MODEL);
        PartialModel pullRodModel = pantographModel.get(PULL_ROD_MODEL);
        PartialModel smallArmModel = pantographModel.get(SMALL_ARM_MODEL);
        PartialModel bowHeadModel = pantographModel.get(BOW_HEAD_MODEL);

        SuperByteBuffer baseBuffer =
                baseModel == null ? null : CachedBufferer.partial(baseModel, blockState).light(light);
        SuperByteBuffer largeArmBuffer =
                largeArmModel == null ? null : CachedBufferer.partial(largeArmModel, blockState).light(light);
        SuperByteBuffer pullRodBuffer =
                pullRodModel == null ? null : CachedBufferer.partial(pullRodModel, blockState).light(light);
        SuperByteBuffer smallArmBuffer =
                smallArmModel == null ? null : CachedBufferer.partial(smallArmModel, blockState).light(light);
        SuperByteBuffer bowHeadBuffer =
                bowHeadModel == null ? null : CachedBufferer.partial(bowHeadModel, blockState).light(light);

        pose.pushPose();
        pose.translate(0, transPosY, 0);

        if (facing == Direction.NORTH)
            pose.translate(1f, 0, 0);
        if (facing == Direction.SOUTH)
            pose.translate(0, 0, 1f);
        if (facing == Direction.WEST)
            pose.translate(1f, 0, 1f);
        pose.mulPose(Vector3f.YP.rotationDegrees(facing.toYRot() + 90f));

        // 升降弓拉杆角度变化
//        if (risen && pBlockEntity.pullRodAngle > risePullRodAngle) {
//            pBlockEntity.pullRodAngle -= STEP_SLOW * risenSpeed;
//        } else if (!risen && pBlockEntity.pullRodAngle < downPullRodAngle) {
//            pBlockEntity.pullRodAngle += STEP_SLOW * risenSpeed;
//        } else if (risen) {
//            pBlockEntity.pullRodAngle = risePullRodAngle;
//        } else {
//            pBlockEntity.pullRodAngle = downPullRodAngle;
//        }
        float speedThreshold = 155;

        Pair<Double, Double> angle = pBlockEntity.getAngle();
        // 这地方的算法改成向给定的值靠拢
        if (pBlockEntity.getCache() != null) {
            pBlockEntity.pullRodAngle = angle.getFirst() + (angle.getSecond() - angle.getFirst()) * pPartialTick;
        } else {
            if (risen && pBlockEntity.pullRodAngle > speedThreshold) {
                pBlockEntity.pullRodAngle -= STEP_FAST * risenSpeed;
            } else if (risen && pBlockEntity.pullRodAngle <= speedThreshold &&
                    pBlockEntity.pullRodAngle > risePullRodAngle) {
                pBlockEntity.pullRodAngle -= STEP_SLOW * risenSpeed;
            } else if (!risen && pBlockEntity.pullRodAngle < speedThreshold) {
                pBlockEntity.pullRodAngle += STEP_SLOW * risenSpeed;
            } else if (!risen && pBlockEntity.pullRodAngle >= speedThreshold &&
                    pBlockEntity.pullRodAngle < downPullRodAngle) {
                pBlockEntity.pullRodAngle += STEP_FAST * risenSpeed;
            } else if (risen) {
                pBlockEntity.pullRodAngle = risePullRodAngle;
            } else {
                pBlockEntity.pullRodAngle = downPullRodAngle;
            }
        }

        // 随动角度与坐标
        HashMap<String, Double> pantoModelMap = IPantographModel.getPantoModelMapByType(pantographProps, pBlockEntity.pullRodAngle);
        Double largeArmAngle = pantoModelMap.get(LARGE_ARM_ANGLE);
        Double smallArmPosX = pantoModelMap.get(SMALL_ARM_POS_X);
        Double smallArmPosY = pantoModelMap.get(SMALL_ARM_POS_Y);
        Double smallArmAngle = pantoModelMap.get(SMALL_ARM_ANGLE);
        Double bowHeadPosX = pantoModelMap.get(BOW_HEAD_POS_X);
        Double bowHeadPosY = pantoModelMap.get(BOW_HEAD_POS_Y);

        // 渲染
        if (baseBuffer != null)
            baseBuffer.renderInto(pose, buffer.getBuffer(RenderType.cutout()));

        pose.translate(0, 0.25f, 0.28125f);

        if (largeArmBuffer != null)
            largeArmBuffer.translateZ(8.8 / 16.0).rotateX(-largeArmAngle)
                    .renderInto(pose, buffer.getBuffer(RenderType.cutout()));
        if (pullRodBuffer != null)
            pullRodBuffer.rotateX(-pBlockEntity.pullRodAngle)
                    .renderInto(pose, buffer.getBuffer(RenderType.cutout()));
        if (smallArmBuffer != null)
            smallArmBuffer.translateY(smallArmPosY / 16.0).translateZ(smallArmPosX / 16.0).rotateX(-smallArmAngle)
                    .renderInto(pose, buffer.getBuffer(RenderType.cutout()));
        if (bowHeadBuffer != null)
            bowHeadBuffer.translateY(bowHeadPosY / 16.0).translateZ(bowHeadPosX / 16.0)
                    .renderInto(pose, buffer.getBuffer(RenderType.cutout()));

        pose.popPose();

        if(pBlockEntity.getCache() != null){
            pose.pushPose();
            pose.translate(0f,1f,0f);
            pose.scale(-0.02f,-0.02f,-0.02f);
            pose.translate(0, 10, 0);
            Minecraft.getInstance().font.draw(
                    pose,
                    String.format("PullRodAngle: %.3f, angle: <%.3f, %.3f>",pBlockEntity.pullRodAngle, angle.getFirst(), angle.getSecond()),
                    0,
                    0,
                    0xffffff
            );
            pose.popPose();
        }
    }
}
