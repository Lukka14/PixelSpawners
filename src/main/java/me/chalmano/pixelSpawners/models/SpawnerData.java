package me.chalmano.pixelSpawners.models;

import lombok.Data;

import java.util.List;

@Data
public class SpawnerData {
    private String spawner_type;
    private int price;
    private String display_item;
    private int spawn_per_minute;
    private List<Drop> drops;
}
