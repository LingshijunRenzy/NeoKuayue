package willow.train.kuayue.systems.device.driver.path.list;

import java.util.LinkedList;
import java.util.TreeSet;

public class EdgePathPointList<T extends EdgePathPoint> {
    private EdgePathPointListener<T> listener;
    protected double distance = 0.0D;
    protected TreeSet<T> pathPointList = new TreeSet<>();
    protected LinkedList<T> selectedPointList = new LinkedList<>();
    protected T frontElement = null;
    protected T backElement = null;
    protected final double frontSize;
    protected final double backSize;

    public EdgePathPointList(double frontSize, double backSize){
        this.frontSize = frontSize;
        this.backSize = backSize;
    }

    public void setListener(EdgePathPointListener<T> listener) {
        this.listener = listener;
    }

    public void addPathPoint(T pathPoint) {
        pathPointList.add(pathPoint);

        double deltaDistance = pathPoint.distance() - distance;

        if(
                (frontElement == null || pathPoint.distance() < frontElement.distance()) &&
                        pathPoint.distance() >= distance
        ){
            frontElement = pathPoint;
            onUpdateFront(frontElement);
        }
        if(
                (backElement == null || pathPoint.distance() > backElement.distance()) &&
                        pathPoint.distance() < distance
        ) {
            backElement = pathPoint;
            onUpdateBack(backElement);
        }

        if(deltaDistance < frontSize && deltaDistance > backSize){
            T left = pathPointList.lower(pathPoint);
            if(left != null){
                int index = selectedPointList.indexOf(left) + 1;
                selectedPointList.add(index ,pathPoint);
                onInsertCurrent(pathPoint, index);
            } else {
                selectedPointList.addFirst(pathPoint);
                onInsertCurrent(pathPoint, 0);
            }
        }
    }

    public void removePathPoint(T pathPoint) {
        pathPointList.remove(pathPoint);
        if(selectedPointList.remove(pathPoint)){
            onRemoveCurrent(pathPoint);
        }
        if(pathPoint == frontElement || pathPoint == backElement){
            notifyUpdateFrontBack();
        }
    }

    public void notifyUpdateFrontBack(){
        //noinspection unchecked
        T updateFrontElement = pathPointList.higher((T) new EdgePathPoint.Simple(distance));
        //noinspection unchecked
        T updateBackElement = pathPointList.lower((T) new EdgePathPoint.Simple(distance));
        if(this.frontElement != updateFrontElement){
            this.frontElement = updateFrontElement;
            this.onUpdateFront(updateFrontElement);
        }
        if(this.backElement != updateBackElement){
            this.backElement = updateBackElement;
            this.onUpdateBack(updateBackElement);
        }
    }

    public void addDistance(double distance){
        this.distance += distance;
        if(distance > 0){
            if(this.frontElement != null && this.frontElement.distance() < this.distance){
                notifyUpdateFrontBack();
            }
            T last = selectedPointList.peekLast();
            if(last == null) last = (T) new EdgePathPoint.Simple(this.distance);
            while((last = pathPointList.higher(last)) != null && last.distance() - this.distance < frontSize){
                selectedPointList.addLast(last);
                onPushCurrent(last);
            }
            T first;
            while((first = selectedPointList.peekFirst()) != null && first.distance() - this.distance < backSize){
                T result = selectedPointList.removeFirst();
                removePathPoint(result);
            }
        } else {
            if(this.backElement != null && this.backElement.distance() >= this.distance){
                notifyUpdateFrontBack();
            }
            T first = selectedPointList.peekFirst();
            if(first == null) first = (T) new EdgePathPoint.Simple(this.distance);
            while((first = pathPointList.lower(first)) != null && first.distance() - this.distance > backSize){
                selectedPointList.addFirst(first);
                onInsertCurrent(frontElement, 0);
            }
            T last;
            while((last = selectedPointList.peekLast()) != null && last.distance() - this.distance > frontSize){
                selectedPointList.removeLast();
                onPopCurrent();
            }
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (T point : this.selectedPointList) {
            if(!sb.isEmpty())sb.append(";");
            sb.append(point.toString());
        }

        return String.format(
                "[EdgePathList] Front = %s, Back = %s, Displaying: %s",
                frontElement == null ? "[NULL]" : frontElement.toString(),
                backElement == null ? "[NULL]" : backElement.toString(),
                sb.toString()
        );
    }

    // Preserved for syncs
    private void onUpdateFront(T frontElement) {
        if (listener != null) listener.onUpdateFront(frontElement);
    }
    
    private void onUpdateBack(T frontElement) {
        if (listener != null) listener.onUpdateBack(frontElement);
    }
    
    private void onInsertCurrent(T element, int index) {
        if (listener != null) listener.onInsertCurrent(element, index);
    }
    
    private void onPushCurrent(T element) {
        if (listener != null) listener.onPushCurrent(element);
    }
    
    private void onRemoveCurrent(T element) {
        if (listener != null) listener.onRemoveCurrent(element);
    }
    
    private void onPopCurrent() {
        if (listener != null) listener.onPopCurrent();
    }

    public TreeSet<T> getPoints(){
        return pathPointList;
    }

    public LinkedList<T> getSelectedPoints(){
        return selectedPointList;
    }

    public T getFrontElement() {
        return frontElement;
    }

    public T getBackElement() {
        return backElement;
    }

    public double getDistance(){
        return distance;
    }

    public void reset(){
        this.distance = 0.0D;
        this.pathPointList.clear();
        this.selectedPointList.clear();
        this.frontElement = null;
        this.backElement = null;
    }
}
