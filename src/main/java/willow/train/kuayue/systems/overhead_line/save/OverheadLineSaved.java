package willow.train.kuayue.systems.overhead_line.save;

import kasuga.lib.core.base.Saved;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;

public class OverheadLineSaved {
    Saved<OverheadLineMigrationStore> migrationStoreSaved = new Saved<>(
            "overhead_line_migration_store",
            OverheadLineMigrationStore::new,
            OverheadLineMigrationStore::load
    );

    public OverheadLineMigrationStore getMigration() {
        return migrationStoreSaved.getData().orElseThrow();
    }

    public void load(ServerLevel level) {
        migrationStoreSaved.loadFromDisk(level);
    }

    public void save(ServerLevel level) {
        migrationStoreSaved.saveToDisk(level);
    }
}
