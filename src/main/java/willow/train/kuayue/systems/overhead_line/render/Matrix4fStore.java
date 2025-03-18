package willow.train.kuayue.systems.overhead_line.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import org.jetbrains.annotations.NotNull;

import java.nio.FloatBuffer;
import java.util.Iterator;

public class Matrix4fStore implements Iterable<Matrix4f> {
    private final FloatBuffer buffer;
    private final int size;
    private final Matrix4f handler;

    public Matrix4fStore(FloatBuffer buffer, int size) {
        if(buffer.capacity() < 16 * size) {
            throw new IndexOutOfBoundsException(String.format("Invalid PointStack cache result: should the buffer capacity %d, %d got." , buffer.capacity(), size * 16));
        }
        this.buffer = buffer;
        this.size = size;
        this.handler = new Matrix4f();
    }

    public void write(int needle, Matrix4f matrix) {
        matrix.store(buffer.slice(needle * 16,16));
    }

    public Matrix4f getHandler(){
        return handler;
    }

    public void load(int needle){
        handler.load(buffer.slice(needle * 16, 16));
    }

    @NotNull
    @Override
    public Iterator<Matrix4f> iterator() {
        return new Iterator<>() {
            Matrix4f matrix4f = new Matrix4f();
            int index = 0;
            @Override
            public boolean hasNext() {
                return index < size;
            }
            @Override
            public Matrix4f next() {
                matrix4f.load(buffer.slice(index * 16, 16));
                index++;
                return matrix4f;
            }
        };
    }
}
