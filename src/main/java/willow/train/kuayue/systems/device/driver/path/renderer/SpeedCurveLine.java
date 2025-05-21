package willow.train.kuayue.systems.device.driver.path.renderer;

import java.util.ArrayList;

public class SpeedCurveLine {
    ArrayList<Float> baked = new ArrayList<>();
    double maxSpeed = 0.0F;
    double acceleration = 1.0F;
    double step = 0.05F;

    public SpeedCurveLine(double maxSpeed, double acceleration) {
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
    }

    public void bake(){
        baked.clear();
        double breakDistance = Math.pow(maxSpeed, 2) / (2 * acceleration);
        int totalSteps = ((int) (breakDistance / step)) + 1;
        for(int position = 0;position < totalSteps ; position++){
            // v^2 = x * 2 * acceleration
            // v = sqrt(x * 2 * acceleration)
            baked.add((float) ( Math.sqrt((position * step) * 2 * acceleration) ));
        }
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public ArrayList<Float> getBaked() {
        return baked;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }
}
