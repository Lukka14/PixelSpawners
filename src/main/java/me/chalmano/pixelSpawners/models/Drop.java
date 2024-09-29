package me.chalmano.pixelSpawners.models;

import lombok.Data;

import java.util.List;

@Data
public class Drop {
    private String item;
    private String name;
    private List<String> lores;
    private int amount;
    private double chance;
}
