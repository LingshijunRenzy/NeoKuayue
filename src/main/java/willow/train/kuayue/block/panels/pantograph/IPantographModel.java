package willow.train.kuayue.block.panels.pantograph;

import java.util.HashMap;

public interface IPantographModel {

    public static final String BASE_MODEL = "baseModel";
    public static final String LARGE_ARM_MODEL = "largeArmModel";
    public static final String PULL_ROD_MODEL = "pullRodModel";
    public static final String SMALL_ARM_MODEL = "smallArmModel";
    public static final String BOW_HEAD_MODEL = "bowHeadModel";

    public static final String LARGE_ARM_ANGLE = "largeArmAngle";
    public static final String SMALL_ARM_POS_X = "smallArmPosX";
    public static final String SMALL_ARM_POS_Y = "smallArmPosY";
    public static final String SMALL_ARM_ANGLE = "smallArmAngle";
    public static final String BOW_HEAD_POS_X = "bowHeadPosX";
    public static final String BOW_HEAD_POS_Y = "bowHeadPosY";

    default HashMap<String, Double> singleArmPantographModel(
            double baselineLength, double largeArmLength,
            double pullRodLength, double connectingRodLength,
            double smallArmAngle, double smallArmLength,
            double pullRodAngle) {

        double C_X = pullRodLength * Math.cos(Math.toRadians(pullRodAngle));
        double C_Y = pullRodLength * Math.sin(Math.toRadians(pullRodAngle));

        double kilo =
                (pullRodLength * pullRodLength + largeArmLength * largeArmLength + baselineLength * baselineLength - connectingRodLength * connectingRodLength)
                        / (2 * pullRodLength * largeArmLength * baselineLength);

        double ako = Math.sin(Math.toRadians(pullRodAngle)) / baselineLength;
        double bump = Math.cos(Math.toRadians(pullRodAngle)) / baselineLength - 1 / pullRodLength;
        double caret = Math.cos(Math.toRadians(pullRodAngle)) / largeArmLength - kilo;

        double tempDelta = bump * bump * caret * caret - (ako * ako + bump * bump) * (caret * caret - ako * ako);
        if (tempDelta < 0){
            throw new RuntimeException("delta < 0");
        }

        double delta = Math.sqrt(tempDelta);
        double beta = Math.toDegrees(Math.acos((-(bump * caret) + delta) / (ako * ako + bump * bump)));

        double D_X = baselineLength + largeArmLength * Math.cos(Math.toRadians(beta));
        double D_Y = largeArmLength * Math.sin(Math.toRadians(beta));

        double gama =
                Math.toDegrees(
                        Math.acos((D_X - C_X) / connectingRodLength)
                );

        double theta = smallArmAngle + gama - 180;
        double E_X = D_X + smallArmLength * Math.cos(Math.toRadians(theta));
        double E_Y = D_Y + smallArmLength * Math.sin(Math.toRadians(theta));

        HashMap<String, Double> singlePantoModel = new HashMap<>();
        singlePantoModel.put(LARGE_ARM_ANGLE, beta);
        singlePantoModel.put(SMALL_ARM_POS_X, C_X);
        singlePantoModel.put(SMALL_ARM_POS_Y, C_Y);
        singlePantoModel.put(SMALL_ARM_ANGLE, gama);
        singlePantoModel.put(BOW_HEAD_POS_X, E_X);
        singlePantoModel.put(BOW_HEAD_POS_Y, E_Y);
        return singlePantoModel;
    }

    default HashMap<String, Double> getPantoModelMapByType (PantographProps pantographProps, double pullRodAngle) {

        HashMap<String, Double> pantoModelMap = new HashMap<>();

        pantoModelMap = singleArmPantographModel(
                pantographProps.getBaselineLength(),
                pantographProps.getLargeArmLength(),
                pantographProps.getPullRodLength(),
                pantographProps.getConnectingRodLength(),
                pantographProps.getSmallArmAngle(),
                pantographProps.getSmallArmLength(),
                pullRodAngle
        );
        return pantoModelMap;
    }
}
