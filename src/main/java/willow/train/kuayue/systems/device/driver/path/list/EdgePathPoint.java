package willow.train.kuayue.systems.device.driver.path.list;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface EdgePathPoint extends Comparable<EdgePathPoint> {
    public int compareTo(@NotNull EdgePathPoint object);
    public double distance();

    public static class Simple implements EdgePathPoint {

        protected double distance;

        public Simple(double distance){
            this.distance = distance;
        }

        public double distance(){
            return distance;
        }

        @Override
        public int compareTo(@NotNull EdgePathPoint object) {
            return Double.compare(distance(), object.distance());
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Simple simple)) return false;
            return Double.compare(distance, simple.distance) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(distance);
        }

        @Override
        public String toString() {
            return "[simple," + "distance=" + distance + ']';
        }
    }
}
