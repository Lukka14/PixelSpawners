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
import java.util.ArrayList;
import java.util.List;

public class SpawnersReader {

    @Getter
    private static final SpawnersReader instance = new SpawnersReader();

    // file path, parent folder name and file name without extension
    private static final String FOLDER_NAME = "spawners";

    private static final String FILE_NAME_NO_EXT = "spawners";

    private static final String FILE_EXT = ".json";

    @Getter
//    private final Map<Integer, List<SpawnerData>> spawnerDataListMap = new HashMap<>();

    private final List<List<SpawnerData>> spawnerDataListList = new ArrayList<>();

    private final int UPPER_FILE_INDEX_THRESHOLD = 10;

    public SpawnersReader() {
        reloadSpawnerData();
    }

    // todo Working on this class is finished (i think),
    //  implement logic in other classes to check all List<SpawnerData> from spawnerDataListMap

    // responsible for generating file if not exists and re-reading the data.
    public void reloadSpawnerData() {
        generateSpawnerFileIfNotExists();
        updateSpawnerDataListMap();

        if (spawnerDataListList.isEmpty()) {
            PixelSpawners.getInstance().getLogger().severe("Something went wrong, could not find/read spawners file, make sure file " + getSpawnerFileFullName() + " exists. disabling the plugin...");
            PixelSpawners.getInstance().getServer().getPluginManager().disablePlugin(PixelSpawners.getInstance());
        }
    }

    public void generateSpawnerFileIfNotExists() {
        String spawnerFileName = getSpawnerFileFullName();
        File file = getFileFromServer(spawnerFileName);

        if (!file.exists()) {
            PixelSpawners.getInstance().saveResource(spawnerFileName, false);
            PixelSpawners.getInstance().getLogger().info("Generated new spawner file: " + spawnerFileName);
        }
    }

    public void updateSpawnerDataListMap() {
        spawnerDataListList.clear();
        String spawnerFileName = getSpawnerFileFullName();

        for (int i = 0; i <= UPPER_FILE_INDEX_THRESHOLD; i++) {
            File file = getFileFromServer(spawnerFileName);
            spawnerFileName = getSpawnerFileFullName(i);
            if (!file.exists()) {
                continue;
            }

            List<SpawnerData> spawnerData = readSpawnerDataList(file);
            spawnerDataListList.add(spawnerData);
            PixelSpawners.getInstance().getLogger().info("Loaded "+spawnerData.size()+" spawners from file: " + file.getName());
        }

        if (spawnerDataListList.isEmpty()) {
            PixelSpawners.getInstance().getLogger().warning("No Spawner file found to read data, re-generating...");
            generateSpawnerFileIfNotExists();
        }

    }

    public List<SpawnerData> readSpawnerDataList(File file) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new Gson();
        Type spawnerListType = new TypeToken<List<SpawnerData>>() {
        }.getType();
        return gson.fromJson(fileReader, spawnerListType);
    }

    private static String getSpawnerFileFullName(int index) {
        return FOLDER_NAME + "/" + FILE_NAME_NO_EXT + index + FILE_EXT;
    }

    private static String getSpawnerFileFullName() {
        return FOLDER_NAME + "/" + FILE_NAME_NO_EXT + FILE_EXT;
    }

    public static File getFileFromServer(String filePath){
        return new File(PixelSpawners.getInstance().getDataFolder(),filePath);
    }

}
