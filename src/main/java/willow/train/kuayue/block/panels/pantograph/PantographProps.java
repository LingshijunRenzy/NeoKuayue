package willow.train.kuayue.block.panels.pantograph;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static willow.train.kuayue.block.panels.pantograph.IPantographModel.BOW_HEAD_POS_Y;

@Data
public class PantographProps implements IPantographAngleMapping {

    private double baselineLength;
    private double largeArmLength;
    private double pullRodLength;
    private double connectingRodLength;
    private double smallArmAngle;
    private double smallArmLength;
    private double basementHeight;

    public List<PantographMapping> heightList = new ArrayList<>();
    public boolean isMapping = false;

    public void heightListInit(PantographProps pantographProps, double risePullRodAngle, double downPullRodAngle) {

        PantographMapping pantographMapping = new PantographMapping(0.0, 0.0);

        for (double pullRodAngle = risePullRodAngle; pullRodAngle <= downPullRodAngle; pullRodAngle += 0.05) {
            HashMap<String, Double> pantoModelMap =
                    IPantographModel.getPantoModelMapByType(pantographProps, pullRodAngle);
            Double bowHeight = pantoModelMap.get(BOW_HEAD_POS_Y);
            pantographMapping.setBowHeight(bowHeight);
            pantographMapping.setPullRodAngle(pullRodAngle);
            heightList.add(new PantographMapping(pantographMapping.getPullRodAngle(), pantographMapping.getBowHeight()));
        }
        isMapping = true;
    }

    public boolean readyForMap() {
        return heightList != null && !heightList.isEmpty();
    }

    public double getAngleByHeight(double bowHeight) {

        if (heightList == null || heightList.isEmpty()) {
            throw new RuntimeException("Instance heightList is null or empty!");
        }

        if (bowHeight > heightList.get(0).getBowHeight())
            bowHeight = heightList.get(0).getBowHeight();
        if (bowHeight < heightList.get(heightList.size() - 1).getBowHeight())
            bowHeight = heightList.get(heightList.size() - 1).getBowHeight();

        int left = 0;
        int right = heightList.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            PantographMapping mappingPair = heightList.get(mid);
            double heightKey = mappingPair.getBowHeight();

            if (heightKey == bowHeight) {
                return mappingPair.getPullRodAngle();
            } else if (heightKey > bowHeight) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        if (right >= 0 && left < heightList.size()) {
            PantographMapping mLeft = heightList.get(left);
            PantographMapping mRight = heightList.get(right);
            double rodAngle1 = mLeft.getPullRodAngle();
            double rodAngle2 = mRight.getPullRodAngle();
            double heightLeft = mLeft.getBowHeight();
            double heightRight = mRight.getBowHeight();
            return rodAngle1 + (rodAngle2 - rodAngle1) * (bowHeight -  heightLeft) / (heightRight - heightLeft);
        }

        return 0.0;
        // throw new RuntimeException("Can not find target angle!");
    }

    @Override
    public double getBaseHeight() {
        return basementHeight;
    }

    public PantographProps(double baselineLength, double largeArmLength, double pullRodLength,
                           double connectingRodLength, double smallArmAngle, double smallArmLength,
                           double basementHeight) {
        this.baselineLength = baselineLength;
        this.largeArmLength = largeArmLength;
        this.pullRodLength = pullRodLength;
        this.connectingRodLength = connectingRodLength;
        this.smallArmAngle = smallArmAngle;
        this.smallArmLength = smallArmLength;
        this.basementHeight = basementHeight;
    }

    @Data
    public static class PantographMapping {
        private double pullRodAngle;
        private double bowHeight;

        public PantographMapping() {
        }

        public PantographMapping(double pullRodAngle, Double bowHeight) {
            this.pullRodAngle = pullRodAngle;
            this.bowHeight = bowHeight;
        }
    }
}
