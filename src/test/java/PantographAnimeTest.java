public class PantographAnimeTest {

    public static final double D_AB = 8.8;
    public static final double D_AC = 19.52;
    public static final double D_BD = 25.44;
    public static final double D_CD = 3.130;
    public static final double D_DE = 25.28;
    public static final double ANGLE_CDE = 161.0;

    public static void main(String[] args) {

        double alpha = 160;
        System.out.println("angle_alpha:" + alpha);
        double C_X = D_AC * Math.cos(Math.toRadians(alpha));
        double C_Y = D_AC * Math.sin(Math.toRadians(alpha));

        System.out.println("C("+ C_X + ", " + C_Y + ")");
        double beta = angleBetaCalc(alpha);
        System.out.println("angle_beta:" + beta);
        double D_X = D_AB + D_BD * Math.cos(Math.toRadians(beta));
        double D_Y = D_BD * Math.sin(Math.toRadians(beta));

        double gama =
                Math.toDegrees(
                        Math.acos(
                                (D_X - C_X) / D_CD
                        )
                );
        System.out.println("angle_gama:" + gama);

        double theta = ANGLE_CDE + gama - 180;
        double E_X = D_X + D_DE * Math.cos(Math.toRadians(theta));
        double E_Y = D_Y + D_DE * Math.sin(Math.toRadians(theta));
        System.out.println("E("+ E_X + ", " + E_Y + ")");
    }

    public static double angleBetaCalc(double alpha) {

        double kilo =
                (D_AC * D_AC + D_BD * D_BD + D_AB * D_AB - D_CD * D_CD) / (2 * D_AC * D_BD * D_AB);

        double ako = Math.sin(Math.toRadians(alpha)) / D_AB;
        double bump = Math.cos(Math.toRadians(alpha)) / D_AB - 1 / D_AC;
        double caret = Math.cos(Math.toRadians(alpha)) / D_BD - kilo;

        double tempDelta = bump * bump * caret * caret - (ako * ako + bump * bump) * (caret * caret - ako * ako);
        if (tempDelta < 0){
            throw new RuntimeException("delta < 0");
        }

        double delta = Math.sqrt(tempDelta);
        double beta = Math.toDegrees(Math.acos((-(bump * caret) + delta) / (ako * ako + bump * bump)));

        return beta;
    }
}
