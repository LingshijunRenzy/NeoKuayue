package willow.train.kuayue.systems.overhead_line.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
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
        pose.mulPose(Vector3f.YP.rotation((float) Math.atan2(dir.x, dir.z)));
    }

    public static void straightLine(RenderCurve.Builder builder, Vec3 a, Vec3 b, float r) {
        Vec3 dir = b.subtract(a);


        PoseStack pose = builder.getPoseStack();
        pose.mulPose(Vector3f.XP.rotation( - (float) Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z))));
        pose.scale(r, r, (float) a.distanceTo(b));
        pose.translate(-0.5, - 0.5, 0);

        builder.store();
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
            // From (thisPoint.x, thisPoint.y) to (previousPoint.x, previousPoint.y)
            float zRot = (float) Math.atan2(thisPoint.y - previousPoint.y, thisPoint.x - previousPoint.x);
            float length = (float) Math.sqrt(Math.pow(thisPoint.x - previousPoint.x, 2) + Math.pow(thisPoint.y - previousPoint.y, 2));
            pose.pushPose();
            pose.translate(0, thisPoint.y + currentOffset, -thisPoint.x);
            pose.mulPose(Vector3f.XP.rotation(zRot));
            pose.scale(r, r, length);
            builder.store();
            pose.popPose();

            if (i < segments) {
                // 计算直线基准位置
                float referenceY = calculateStraightLineY(straightSlope, -firstOffset, i * stepLength);
                // 计算曲线与直线的垂直偏移量
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
}
