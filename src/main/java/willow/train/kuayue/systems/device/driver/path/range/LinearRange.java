package willow.train.kuayue.systems.device.driver.path.range;

import kasuga.lib.core.util.data_type.Couple;

import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class LinearRange {
    HashSet<Runnable> notifiers = new HashSet<>();
    HashSet<Runnable> disposeHooks = new HashSet<>();
    private boolean dirty = true;

    public Runnable listen(Runnable callback) {
        notifiers.add(callback);
        return () -> notifiers.remove(callback);
    }

    public void listenTo(LinearRange target) {
        disposeHooks.add(target.listen(this::notifyUpdate));
    }

    public void close() {
        disposeHooks.forEach(Runnable::run);
    }

    public void notifyUpdate() {
        notifiers.forEach(Runnable::run);
        this.dirty = true;
    }

    protected TreeMap<Double, HashSet<Couple<Double>>> range = new TreeMap<>();
    public abstract void calculateRange();

    public TreeMap<Double, HashSet<Couple<Double>>> getRange(){
        if(this.dirty) {
            calculateRange();
            this.dirty = false;
        }
        return range;
    }
}
