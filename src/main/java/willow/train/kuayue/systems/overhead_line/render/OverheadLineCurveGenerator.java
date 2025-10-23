package willow.train.kuayue.systems.overhead_line.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.KuayueConfig;

import java.util.ArrayList;
import java.util.List;

public class OverheadLineCurveGenerator {

    public static final Vec3 UP = new Vec3(0, 1, 0);

    public static RenderCurve straightLine(Level level, Vec3 a, Vec3 b, float r) {
        RenderCurve.Builder builder = RenderCurve.create();
        prepareLine(builder, a, b.subtract(a), r);
        straightLine(builder, a, b, r);
        return builder.build(level);
    }

    public static void prepareLine(RenderCurve.Builder builder, Vec3 a, Vec3 dir, float r){
        PoseStack pose = builder.getPoseStack();
        pose.translate(a.x - 0.5 * r, a.y  - 0.5 * r, a.z  - 0.5 * r);
        pose.mulPose(Axis.YP.rotation((float) Math.atan2(dir.x, dir.z)));
    }

    public static void straightLine(RenderCurve.Builder builder, Vec3 a, Vec3 b, float r) {
        Vec3 dir = b.subtract(a);

        float rotZ = - (float) Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z));
        Quaternionf q = Axis.XP.rotation(rotZ);
        Quaternionf qZ = Axis.ZP.rotation(rotZ);
        float distance = (float) a.distanceTo(b);

        Vector3f one = new Vector3f(1, 0, 0);
        one.rotate(qZ);
        one.mul(distance);
        Vec2 vecResult = new Vec2((float) Math.sqrt(one.x() * one.x() + one.z() * one.z()), one.y());

        PoseStack pose = builder.getPoseStack();
        pose.mulPose(q);
        pose.scale(r, r, distance);
        pose.translate(-0.5, - 0.5, 0);

        builder.store(vecResult);
    }

    public static RenderCurve conicHangLine(Level level, Vec3 first, Vec3 last, float firstOffset, float secondOffset,
                                            float distance, float setonSpacing, float r) {
        RenderCurve.Builder builder = RenderCurve.create();
        conicHangLine(builder, first, last, firstOffset, secondOffset, distance, setonSpacing, r);
        builder.getPoseStack().setIdentity();
        prepareLine(builder, first, last.subtract(first), r);
        straightLine(builder, first, last, r);
        return builder.build(level);
    }

    public static void conicHangLine(RenderCurve.Builder builder, Vec3 first, Vec3 last,
                                   float firstOffset, float secondOffset,
                                   float distance, float segmentSpacing,
                                     float r
    ) {
        Vec3 hangPointFirst = first.add(UP.scale(firstOffset));
        Vec3 hangPointLast = last.add(UP.scale(secondOffset));
        float projectionDistance = (float) hangPointLast.subtract(hangPointFirst).horizontalDistance();

        float curveDeltaY = (float) (hangPointLast.y() - hangPointFirst.y());
        float straightDeltaY = (float) (last.y() - first.y());

        float straightSlope = straightDeltaY/projectionDistance;

        float projectedRadius  = distance / ((float) (Math.cos(Math.atan(straightSlope))));

        float denominator = -firstOffset + projectedRadius;

        float quadraticA = (float) Math.pow(projectionDistance, 2) / denominator;
        float quadraticB = -(float) (
                projectionDistance +
                        (2 * Math.pow(projectionDistance, 2) * straightSlope) / denominator
        );
        float quadraticC = (float) (
                (Math.pow(straightSlope, 2) * Math.pow(projectionDistance, 2)) / denominator +
                        curveDeltaY
        );

        float discriminant = (float) Math.pow(quadraticB, 2) - 4 * quadraticA * quadraticC;
        float paramB = (float) ((-quadraticB + Math.sqrt(discriminant)) / (2 * quadraticA));
        float paramA = (float) ((curveDeltaY - paramB * projectionDistance) / Math.pow(projectionDistance, 2));

        int segments = (int) Math.max(projectionDistance / segmentSpacing, 1);
        float stepLength =  (float) (projectionDistance / (double) segments);

        Vec2 previousPoint = new Vec2(0, calculateParabolaY(paramA, paramB, 0));
        float previousRotation = 0;

        prepareLine(builder, first, first.subtract(last), r);
        PoseStack pose = builder.getPoseStack();
        for(int i = 1; i <= segments; i++) {
            float currentOffset = firstOffset + (secondOffset - firstOffset) * (float) i / segments;
            Vec2 thisPoint = new Vec2(i * stepLength, calculateParabolaY(paramA, paramB, (float) i * stepLength));
            float zRot = (float) Math.atan2(thisPoint.y - previousPoint.y, thisPoint.x - previousPoint.x);
            float length = (float) Math.sqrt(Math.pow(thisPoint.x - previousPoint.x, 2) + Math.pow(thisPoint.y - previousPoint.y, 2));
            pose.pushPose();
            pose.translate(0, thisPoint.y + currentOffset, -thisPoint.x);
            pose.mulPose(Axis.XP.rotation(zRot));
            pose.scale(r, r, length);
            builder.store();
            pose.popPose();

            if (i < segments) {
                float referenceY = calculateStraightLineY(straightSlope, -firstOffset, i * stepLength);
                float verticalOffset = thisPoint.y - referenceY;
                pose.pushPose();
                pose.translate(0, thisPoint.y - verticalOffset + currentOffset , -thisPoint.x);
                pose.scale(r, verticalOffset, r);
                builder.store();
                pose.popPose();
            }

            previousRotation = zRot;
            previousPoint = thisPoint;
        }


    }

    private static float calculateParabolaY(float A, float B, float x) {
        return A * x * x + B * x;
    }

    private static float calculateStraightLineY(float slope, float intercept, float x) {
        return slope * x + intercept;
    }

    public static RenderCurve catenaryLine(Level level, Vec3 first, Vec3 last, float partSpacing, float r) {
        RenderCurve.Builder builder = RenderCurve.create();
        catenaryLine(builder, first, last, partSpacing, r);
        return builder.build(level);
    }

    public static void catenaryLine(RenderCurve.Builder builder, Vec3 first, Vec3 last, float partSpacing, float r) {
        float horizontalDistance = (float) last.subtract(first).horizontalDistance();
        float dy = (float) (last.y() - first.y());

        float constant_g = KuayueConfig.CONFIG.getDefaultDouble("OVERHEAD_LINE_SAGGING_COEFFICIENT").floatValue();
        float constant_step = Math.abs(dy);
        float offset_result;

        // 计算悬链线偏移
        if (dy == 0) {
            offset_result = -horizontalDistance / 2;
        } else {
            float d = dy > 0 ? -horizontalDistance : horizontalDistance;
            float x = d / 2;

            float first_y = cosh(constant_g, x);
            float last_y = cosh(constant_g, x - d);

            float gap = Math.abs(first_y - last_y) - Math.abs(dy);

            boolean positive = false;

            while(Math.abs(gap) > 0.01f) {

                if(gap > 0 && !positive) {
                    constant_step = - (float) .5 * constant_step;
                    positive = true;
                }

                if(gap < 0 && positive) {
                    constant_step = - (float) .5 * constant_step;
                    positive = false;
                }

                x += constant_step;
                first_y = cosh(constant_g, x);
                last_y = cosh(constant_g, x - d);

                gap = Math.abs(first_y - last_y) - Math.abs(dy);
            }
            offset_result = x;
        }

        // 准备渲染
        prepareLine(builder, first, last.subtract(first), r);
        PoseStack pose = builder.getPoseStack();

        // 计算分段
        float parts = (float) Math.ceil(horizontalDistance/partSpacing);
        float trueSpacing = horizontalDistance/parts;
        float x = offset_result;
        float last_y = cosh(constant_g, x);
        float spx = dy >= 0 ? trueSpacing : -trueSpacing;

        float actualLastY = 0f, actualLastX = 0f;
        Vector3f cache; Vec2 vec2;

        // 生成曲线段
        for (int i = 0; i < parts; i++) {
            x += spx;
            float current_y = cosh(constant_g, x);
            float deltaY = current_y - last_y;
            float zRot = (float) Math.atan2(deltaY, Math.abs(trueSpacing));
            float segmentLength = (float) Math.sqrt(deltaY * deltaY + trueSpacing * trueSpacing);

            actualLastY += (float) (segmentLength * Math.sin(zRot));
            actualLastX += (float) (segmentLength * Math.cos(zRot));
            Quaternionf rotation = Axis.XP.rotation(-zRot);
            Quaternionf rotationZ = Axis.ZP.rotation(zRot);


            cache = new Vector3f(1, 0, 0);
            cache.rotate(rotationZ);
            cache.mul(segmentLength);
            vec2 = new Vec2((float) Math.sqrt(cache.x() * cache.x() + cache.z() * cache.z()), cache.y());

            pose.pushPose();
            pose.translate(0, actualLastY, actualLastX);
            pose.mulPose(rotation);

            pose.scale(r, r, segmentLength);
            builder.store(vec2);
            pose.popPose();
            
            last_y = current_y;
        }
    }

    private static float cosh(float constant_g, float x) {
        return (float) (constant_g * (Math.pow(Math.E, x/constant_g) + Math.pow(Math.E, -x/constant_g))/2);
    }
}
