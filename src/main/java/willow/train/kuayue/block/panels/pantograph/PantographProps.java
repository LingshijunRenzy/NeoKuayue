package willow.train.kuayue.block.panels.pantograph;

import lombok.Data;

@Data
public class PantographProps {

    private double baselineLength;
    private double largeArmLength;
    private double pullRodLength;
    private double connectingRodLength;
    private double smallArmAngle;
    private double smallArmLength;

    public PantographProps(double baselineLength, double largeArmLength, double pullRodLength,
                           double connectingRodLength, double smallArmAngle, double smallArmLength) {
        this.baselineLength = baselineLength;
        this.largeArmLength = largeArmLength;
        this.pullRodLength = pullRodLength;
        this.connectingRodLength = connectingRodLength;
        this.smallArmAngle = smallArmAngle;
        this.smallArmLength = smallArmLength;
    }
}
