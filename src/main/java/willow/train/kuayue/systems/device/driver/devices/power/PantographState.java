package willow.train.kuayue.systems.device.driver.devices.power;

public enum PantographState {
    RISEN(true, true),
    LOWERED(false, true),
    NONE(false, false);

    private final boolean update;
    private final boolean risen;

    PantographState(boolean risen, boolean update) {
        this.risen = risen;
        this.update = update;
    }

    public static PantographState fromBoolean(boolean risen) {
        return risen ? RISEN : LOWERED;
    }

    public boolean shouldUpdate(boolean source){
        if(!update)
            return false;
        return risen != source;
    }

    public boolean isRisen() {
        return risen;
    }
}
