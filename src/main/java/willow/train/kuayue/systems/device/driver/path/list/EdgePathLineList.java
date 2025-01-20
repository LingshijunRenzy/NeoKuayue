package willow.train.kuayue.systems.device.driver.path.list;

import java.util.*;

public class EdgePathLineList<T extends EdgePathLine> {
    protected double distance = 0.0D;

    protected TreeSet<T> startPointList = new TreeSet<>();
    protected TreeSet<T> endPointList = new TreeSet<>(
            Comparator.comparingDouble(a -> a.distance() + a.length())
    );

    // CurrentElements: The element start point <= distance, end point >= distance
    // and the element are sort by distance + length
    protected TreeSet<T> currentElements = new TreeSet<>(
            Comparator.comparingDouble(a -> a.distance() + a.length())
    );
    // CurrentElementReversals: The element start point <= distance, end point >= distance
    // and the element are sort by distance
    protected TreeSet<T> currentElementReversals = new TreeSet<>(
            Comparator.comparingDouble(EdgePathLine::distance)
    );

    // FrontElements: The element start point > distance, but < distance + frontSize
    // and the element are sort by distance
    protected TreeSet<T> frontElements = new TreeSet<>(
            Comparator.comparingDouble(EdgePathLine::distance)
    );
    // FrontElementReversals: The element start point > distance, but < distance + frontSize
    // and the element are sort by distance + length
    protected TreeSet<T> frontElementReversals = new TreeSet<>(
            Comparator.comparingDouble(a -> a.distance() + a.length())
    );

    // FrontElements: The element end point < distance, but > distance + backSize
    // and the element are sort by distance + length
    // For faster insert the backElements
    protected TreeSet<T> backElements = new TreeSet<>(
            Comparator.comparingDouble(a -> a.distance() + a.length())
    );

    // FrontElementReversals: The element end point < distance, but > distance + backSize
    // and the element are sort by distance
    // To faster remove the backElements
    protected TreeSet<T> backElementReversals = new TreeSet<>(
            Comparator.comparingDouble(EdgePathLine::distance)
    );

    
    protected final double frontSize;
    protected final double backSize;

    private EdgePathLineListener<T> listener;

    public EdgePathLineList(double frontSize, double backSize) {
        this.frontSize = frontSize;
        this.backSize = backSize;
    }

    public void setListener(EdgePathLineListener<T> listener) {
        this.listener = listener;
    }

    private void onInsertFront(T element) {
        if (listener != null) listener.onInsertFront(element);
    }
    
    private void onInsertBack(T element) {
        if (listener != null) listener.onInsertBack(element);
    }
    
    private void onRemoveFront(T element) {
        if (listener != null) listener.onRemoveFront(element);
    }
    
    private void onRemoveBack(T element) {
        if (listener != null) listener.onRemoveBack(element);
    }
    
    private void onInsertCurrent(T element) {
        if (listener != null) listener.onInsertCurrent(element);
    }
    
    private void onRemoveCurrent(T element) {
        if (listener != null) listener.onRemoveCurrent(element);
    }
    
    private void onPopCurrent() {
        if (listener != null) listener.onPopCurrent();
    }

    public void addPathLine(T line) {
        // 添加到主列表
        startPointList.add(line);
        endPointList.add(line);

        double deltaStart = line.distance() - distance;
        double deltaEnd = deltaStart + line.length();

        if (deltaStart <= 0 && deltaEnd >= 0) {
            currentElements.add(line);
            currentElementReversals.add(line);
            onInsertCurrent(line);
        } else if (deltaStart > 0 && deltaStart < frontSize) {
            frontElements.add(line);
            frontElementReversals.add(line);
            onInsertFront(line);
        }
        else if (deltaEnd < 0 && deltaEnd > backSize) {
            backElements.add(line);
            backElementReversals.add(line);
            onInsertBack(line);
        }
    }

    public void removePathLine(T line) {
        startPointList.remove(line);
        endPointList.remove(line);

        if (currentElements.remove(line)) {
            currentElementReversals.remove(line);
            onRemoveCurrent(line);
        } else if (frontElements.remove(line)) {
            frontElementReversals.remove(line);
            onRemoveFront(line);
        } else if (backElements.remove(line)) {
            backElementReversals.remove(line); 
            onRemoveBack(line);
        }
    }

    public void addDistance(double delta) {
        this.distance += delta;

        //noinspection unchecked
        T currentDistanceLine = (T) new EdgePathLine.Simple(this.distance,0);

        if (delta > 0) {

            // 添加新的前方线段
            T next = frontElements.isEmpty() ? currentDistanceLine : frontElements.last();
            while (
                    (!startPointList.isEmpty()) &&
                    (next = startPointList.higher(next)) != null &&
                    next.distance() - this.distance < frontSize
            ) {
                frontElements.add(next);
                frontElementReversals.add(next);
                onInsertFront(next);
            }

            // 从前方列表移入当前范围的线段
            T prev;
            while (
                    (!frontElements.isEmpty()) &&
                    (prev = frontElements.first()) != null &&
                    prev.distance() - this.distance <= 0
            ) {
                frontElements.remove(prev);
                frontElementReversals.remove(prev);
                currentElements.add(prev);
                currentElementReversals.add(prev);
                onRemoveFront(prev);
                onInsertCurrent(prev);
            }

            // 移除超出 currentElements 范围的线段
            T first;
            while (!currentElements.isEmpty() && (first = currentElements.last()) != null) {
                double endDistance = first.distance() + first.length();
                if (endDistance - this.distance < 0) {
                    currentElements.remove(first);
                    currentElementReversals.remove(first);
                    onRemoveCurrent(first);
                    backElements.add(first);
                    backElementReversals.add(first);
                    onInsertBack(first);
                } else {
                    break;
                }
            }
            // 移除超出 backElements 范围的线段
            T last;
            while (!backElements.isEmpty() && (last = backElements.first()) != null) {
                if (last.distance() + last.length() - this.distance < backSize) {
                    backElements.remove(last);
                    backElementReversals.remove(last);
                    onRemoveBack(last);
                } else {
                    break;
                }
            }
        } else {
            // 从后方添加新的线段到 backElements
            T last;
            while (
                    (!endPointList.isEmpty()) &&
                    (last = endPointList.first()) != null &&
                    last.distance() + last.length() - this.distance < backSize
            ) {
                endPointList.remove(last);
                backElements.add(last);
                backElementReversals.add(last);
                onInsertBack(last);
            }

            // 从后方列表移入当前范围的线段
            T next;
            while (
                    (!backElementReversals.isEmpty()) &&
                    (next = backElementReversals.first()) != null &&
                    next.distance() + next.length() - this.distance > 0
            ) {
                backElements.remove(next);
                backElementReversals.remove(next);
                currentElements.add(next);
                currentElementReversals.add(next);
                onRemoveBack(next);
                onInsertCurrent(next);
            }

            // 从当前范围移入前方列表的线段
            T prev;
            while (
                    (!currentElementReversals.isEmpty()) &&
                    (prev = currentElementReversals.first()) != null &&
                    prev.distance() - this.distance > 0
            ) {
                currentElements.remove(prev);
                currentElementReversals.remove(prev);
                frontElements.add(prev);
                frontElementReversals.add(prev);
                onRemoveCurrent(prev);
                onInsertFront(prev);
            }

            // 移除超出 frontElements 范围的线段
            T first;
            while (!frontElements.isEmpty() && (first = frontElements.first()) != null) {
                if (first.distance() - this.distance > frontSize) {
                    frontElements.remove(first);
                    frontElementReversals.remove(first);
                    onRemoveFront(first);
                } else {
                    break;
                }
            }
        }
    }

    public TreeSet<T> getCurrentElements() {
        return currentElements;
    }

    public TreeSet<T> getFrontElements() {
        return frontElements;
    }

    public TreeSet<T> getBackElements() {
        return backElements;
    }

    public double getDistance() {
        return distance;
    }

    public void reset(){
        this.distance = 0.0D;
        this.startPointList.clear();
        this.endPointList.clear();
        this.currentElements.clear();
        this.currentElementReversals.clear();
        this.frontElements.clear();
        this.frontElementReversals.clear();
        this.backElements.clear();
        this.backElementReversals.clear();
    }
} 