package me.chalmano.pixelSpawners.models;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SpawnerData {
    private String spawner_type;
    private int price;
    private String display_item;
    private int spawn_time;
    private List<Drop> drops;
}
