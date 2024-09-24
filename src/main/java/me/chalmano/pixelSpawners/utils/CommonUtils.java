package me.chalmano.pixelSpawners.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CommonUtils {

    public static String firstToUpperCase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static Component toComponent(String str) {
        return LegacyComponentSerializer.legacy('&').deserialize(str.trim()).decoration(TextDecoration.ITALIC, false);
    }

}
