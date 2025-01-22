package willow.train.kuayue.systems.device.driver.path.list;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface EdgePathLine extends Comparable<EdgePathLine> {
    public int compareTo(@NotNull EdgePathLine object);
    public double distance();
    public double length();

    public static class Simple implements EdgePathLine {
        protected double distance;
        protected double length;

        public Simple(double distance, double length) {
            this.distance = distance;
            this.length = length;
        }

        public double distance() {
            return distance;
        }

        public double length() {
            return length;
        }

        @Override
        public int compareTo(@NotNull EdgePathLine object) {
            return Double.compare(distance(), object.distance());
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Simple simple)) return false;
            return Double.compare(distance, simple.distance) == 0 
                && Double.compare(length, simple.length) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(distance, length);
        }

        @Override
        public String toString() {
            return "[simple," + "distance=" + distance + 
                   ",length=" + length + ']';
        }
    }
} 