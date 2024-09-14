package me.chalmano.pixelSpawners.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import me.chalmano.pixelSpawners.PixelSpawners;
import me.chalmano.pixelSpawners.models.SpawnerData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

public class SpawnersReader {

    @Getter
    private static final SpawnersReader instance = new SpawnersReader();

    private List<SpawnerData> spawnerData = null;

    private final File file;

    public SpawnersReader() {
        file = new File(PixelSpawners.getInstance().getDataFolder(), "spawners.json");

        if (!file.exists()) {
            PixelSpawners.getInstance().saveResource("spawners.json", false);
        }

    }

    public List<SpawnerData> getSpawnerData() {
        if (this.spawnerData == null) {
            updateSpawnerData();
        }
        return this.spawnerData;
    }

    public void updateSpawnerData() {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new Gson();
        Type spawnerListType = new TypeToken<List<SpawnerData>>() {
        }.getType();
        this.spawnerData = gson.fromJson(fileReader, spawnerListType);
    }

}
