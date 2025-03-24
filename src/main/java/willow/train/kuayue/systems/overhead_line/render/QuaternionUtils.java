package willow.train.kuayue.systems.overhead_line.render;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.animation.neo_neo.VectorUtil;
import net.minecraft.world.phys.Vec3;

public class QuaternionUtils {
    public static Quaternion fromTwoVectors(Vec3 a, Vec3 b) {
        Vec3 crossVector = a.cross(b);

        double ls = Math.sqrt(a.lengthSqr() * b.lengthSqr());

        double dt = a.dot(b);

        if (dt / ls == -1)
        {
            Vec3 orthogonalVector = orthogonal(a);
            return new Quaternion((float)orthogonalVector.x, (float)orthogonalVector.y, (float)orthogonalVector.z, 0);
        }

        Quaternion q = new Quaternion(
                (float) crossVector.x,
                (float) crossVector.y,
                (float) crossVector.z,
                (float) (ls + dt)
        );

        q.normalize();

        return q;
    }

    public static Vec3 orthogonal(Vec3 v) {
        float x = (float) Math.abs(v.x);
        float y = (float) Math.abs(v.y);
        float z = (float) Math.abs(v.z);

        Vec3 other = x < y ? (x < z ? new Vec3(1, 0, 0) : new Vec3(0, 0, 1)) : (y < z ? new Vec3(0, 1, 0) : new Vec3(0, 0, 1));
        return v.cross(other);
    }

    public static void test(){

        Vector3f vector3f = new Vector3f(500,0,0);
        vector3f.transform(QuaternionUtils.fromTwoVectors(new Vec3(1000,0,0), new Vec3(1000,1000,1000)));
        System.out.println(vector3f);
        vector3f.transform(QuaternionUtils.fromTwoVectors(new Vec3(1000,1000,1000), new Vec3(1000,0,0)));
        System.out.println(vector3f);
    }
}
