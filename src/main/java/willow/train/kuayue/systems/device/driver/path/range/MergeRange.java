package willow.train.kuayue.systems.device.driver.path.range;

import kasuga.lib.core.util.data_type.Couple;

import java.util.*;

public class MergeRange extends LinearRange{
    private final List<LinearRange> ranges;
    private final MergingRole mergingRole;

    public static enum MergingRole {
        MAX,
        MIN
    }

    public MergeRange(List<LinearRange> ranges, MergingRole mergingRole) {
        super();
        this.ranges = ranges;
        this.mergingRole = mergingRole;
        this.ranges.forEach(this::listenTo);
    }

    @Override
    public void calculateRange() {
        this.range.clear();

        List<Iterator<Map.Entry<Double, HashSet<Couple<Double>>>>> iterators = new ArrayList<>();
        List<Map.Entry<Double, HashSet<Couple<Double>>>> entries = new ArrayList<>();

        for (LinearRange range : ranges) {
            var iter = range.getRange().entrySet().iterator();
            iterators.add(iter);
            if(iter.hasNext()){
                entries.add(iter.next());
            } else {
                entries.add(null);
            }
        }

        TreeMap<Double, HashSet<Double>> activate = new TreeMap<>();

        Optional<Couple<Double>> lastDistanceSpeed = Optional.empty();

        while(
                iterators.stream().anyMatch(Iterator::hasNext) ||
                !activate.isEmpty() ||
                lastDistanceSpeed.isPresent()
        ) {
            double minDistance = Double.MAX_VALUE;
            for (var entry : entries) {
                if(entry == null)
                    continue;
                minDistance = Math.min(minDistance, entry.getKey());
            }

            if(!activate.isEmpty() && activate.firstKey() == minDistance){
                activate.pollFirstEntry();
            }

            if(!activate.isEmpty() && activate.firstKey() < minDistance) {
                minDistance = activate.firstKey();
                activate.pollFirstEntry();
            } else if(minDistance != Double.MAX_VALUE){
                for(var entry : entries) {
                    if(entry == null)
                        continue;
                    if(entry.getKey() == minDistance) {
                        for(var couple : entry.getValue()) {
                            activate.computeIfAbsent(couple.getFirst(), k -> new HashSet<>())
                                    .add(couple.getSecond());
                        }
                        if(iterators.get(entries.indexOf(entry)).hasNext()) {
                            entries.set(entries.indexOf(entry), iterators.get(entries.indexOf(entry)).next());
                        } else {
                            entries.set(entries.indexOf(entry), null);
                        }
                    }
                }
            }



            if(activate.isEmpty()){
                if(lastDistanceSpeed.isEmpty())
                    continue;

                range.computeIfAbsent(
                        lastDistanceSpeed.get().getFirst(),
                        k -> new HashSet<>()
                ).add(
                        Couple.couple(
                                minDistance - lastDistanceSpeed.get().getFirst(),
                                lastDistanceSpeed.get().getSecond()
                        )
                );

                lastDistanceSpeed = Optional.empty();
                continue;
            }

            Optional<Double> mergedSpeed =
                    this.mergingRole == MergingRole.MAX
                            ? activate.values().stream().flatMap(Collection::stream).max(Double::compareTo)
                            : activate.values().stream().flatMap(Collection::stream).min(Double::compareTo);

            if(lastDistanceSpeed.isPresent()){
                range.computeIfAbsent(
                        lastDistanceSpeed.get().getFirst(),
                        k -> new HashSet<>()
                ).add(
                        Couple.couple(
                                minDistance - lastDistanceSpeed.get().getFirst(),
                                lastDistanceSpeed.get().getSecond()
                        )
                );
                lastDistanceSpeed = Optional.empty();
            }

            if(mergedSpeed.isPresent()){
                lastDistanceSpeed = Optional.of(Couple.couple(minDistance, mergedSpeed.get()));
            }
        }
    }

    protected boolean isFinalDirty = true;
    protected TreeMap<Double, Double> finalRange = new TreeMap<>();

    @Override
    public void notifyUpdate() {
        super.notifyUpdate();
        isFinalDirty = true;
    }

    public TreeMap<Double, Double> finalGet(){
        if(!isFinalDirty)
            return finalRange;

        finalRange.clear();

        for(var range : getRange().entrySet()){
            Couple<Double> item = range.getValue().stream().findFirst().orElseGet(()->new Couple<>(0d,0d));
            double from = range.getKey();
            double to = from + item.getFirst();
            double speed = item.getSecond();
            finalRange.put(from, speed);
            finalRange.put(to, 0d);
        }

        isFinalDirty = false;

        return finalRange;
    }
}
