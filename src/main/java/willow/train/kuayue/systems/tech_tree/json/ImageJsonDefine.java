package willow.train.kuayue.systems.tech_tree.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kasuga.lib.core.client.render.texture.ImageMask;
import kasuga.lib.core.client.render.texture.StaticImageHolder;
import kasuga.lib.core.util.LazyRecomputable;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

@Getter
public class ImageJsonDefine {

    private final ResourceLocation id;
    private final ResourceLocation imageLoc;
    private final float u0, v0, u1, v1;

    private final Vector3f pos;
    private final int height, width;

    private final boolean horizontalCentered,
            verticalCentered,
            positionIsCenter,
            xPositive, yPositive;

    private final ImageMask.Axis axisX, axisY;

    private final LazyRecomputable<StaticImageHolder> holder;

    public ImageJsonDefine(JsonObject json) {
        id = new ResourceLocation(json.get("id").getAsString());
        imageLoc = new ResourceLocation(json.get("location").getAsString());
        JsonElement uvElement = json.get("uv");
        if (uvElement.isJsonArray()) {
            JsonArray uvArray = uvElement.getAsJsonArray();
            this.u0 = uvArray.get(0).getAsFloat();
            this.v0 = uvArray.get(1).getAsFloat();
            this.u1 = uvArray.get(2).getAsFloat();
            this.v1 = uvArray.get(3).getAsFloat();
        } else if (uvElement.isJsonObject()) {
            JsonObject uvObject = uvElement.getAsJsonObject();
            this.u0 = uvObject.get("u0").getAsFloat();
            this.v0 = uvObject.get("v0").getAsFloat();
            this.u1 = uvObject.get("u1").getAsFloat();
            this.v1 = uvObject.get("v1").getAsFloat();
        } else {
            u0 = 0;
            v0 = 0;
            u1 = 1;
            v1 = 1;
        }
        JsonElement positionElement = json.get("pos");
        if (!positionElement.isJsonObject()) {
            pos = Vector3f.ZERO;
            horizontalCentered = false;
            verticalCentered = false;
            positionIsCenter = false;
        } else {
            JsonObject posObject = positionElement.getAsJsonObject();
            horizontalCentered = posObject.has("horizontal_centered") &&
                    posObject.get("horizontal_centered").getAsBoolean();
            verticalCentered = posObject.has("vertical_centered") &&
                    posObject.get("vertical_centered").getAsBoolean();
            positionIsCenter = posObject.has("position_is_center") &&
                    posObject.get("position_is_center").getAsBoolean();
            if (!posObject.has("vec")) {
                pos = Vector3f.ZERO;
            } else {
                JsonElement vecElement = posObject.get("vec");
                if (!vecElement.isJsonArray()) {
                    pos = Vector3f.ZERO;
                } else {
                    JsonArray vecArray = vecElement.getAsJsonArray();
                    if (vecArray.size() < 3)
                        pos = Vector3f.ZERO;
                    else {
                        pos = new Vector3f(vecArray.get(0).getAsFloat(),
                                vecArray.get(1).getAsFloat(),
                                vecArray.get(2).getAsFloat());
                    }
                }
            }
        }
        JsonElement sizeElement = json.get("size");
        if (!sizeElement.isJsonObject()) {
            width = 0;
            height = 0;
            axisX = ImageMask.Axis.X;
            axisY = ImageMask.Axis.Y;
            xPositive = true;
            yPositive = true;
        } else {
            JsonObject sizeObject = sizeElement.getAsJsonObject();
            width = sizeObject.get("width").getAsInt();
            height = sizeObject.get("height").getAsInt();
            if (!sizeObject.has("axis_x"))
                axisX = ImageMask.Axis.X;
            else {
                ImageMask.Axis aX = getAxisByStr(sizeObject.get("axis_x").getAsString());
                axisX = aX == null ? ImageMask.Axis.X : aX;
            }
            if (!sizeObject.has("axis_y"))
                axisY = ImageMask.Axis.Y;
            else {
                ImageMask.Axis aY = getAxisByStr(sizeObject.get("axis_y").getAsString());
                axisY = aY == null ? ImageMask.Axis.Y : aY;
            }
            if (!sizeObject.has("x_positive")) {
                xPositive = true;
            } else {
                xPositive = sizeObject.get("x_positive").getAsBoolean();
            }
            if (!sizeObject.has("y_positive")) {
                yPositive = true;
            } else {
                yPositive = sizeObject.get("y_positive").getAsBoolean();
            }
        }
        holder = LazyRecomputable.of(
                () -> new StaticImageHolder(new ResourceLocation(imageLoc.getNamespace(),
                        "textures/" + imageLoc.getPath() + ".png"))
        );
    }

    private static ImageMask.Axis getAxisByStr(String axis) {
        if (axis.equalsIgnoreCase("x"))
            return ImageMask.Axis.X;
        else if (axis.equalsIgnoreCase("y"))
            return ImageMask.Axis.Y;
        else if (axis.equalsIgnoreCase("z"))
            return ImageMask.Axis.Z;
        return null;
    }

    public StaticImageHolder getImageHolder() {
        return holder.get();
    }

    public void clearImageCache() {
        holder.clear();
    }

    public ImageMask getImageMask() {
        return holder.get().getImageSafe().get().getMask();
    }

    public ImageMask getMaskWithUV() {
        return getImageMask().rectangleUV(u0, v0, u1, v1);
    }

    public ImageMask getMaskWithUVAndPos() {
        return getMaskWithUV()
                .rectangle(pos, axisX, axisY, xPositive, yPositive, width, height);
    }
}
