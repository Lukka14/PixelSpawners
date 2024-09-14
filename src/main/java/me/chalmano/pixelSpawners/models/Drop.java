package me.chalmano.pixelSpawners.models;

import lombok.Data;

@Data
public class Drop {
    private String item;
    private int amount;
    private double chance;
}
