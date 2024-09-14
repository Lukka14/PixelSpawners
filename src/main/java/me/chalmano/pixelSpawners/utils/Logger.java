package me.chalmano.pixelSpawners.utils;

import me.chalmano.pixelSpawners.PixelSpawners;

public class Logger {

    private static java.util.logging.Logger LOGGER;

    public static void info(String message) {
        checkLogger();

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // The element at index 2 is methodB, index 3 will be methodA
        String callingMethodName = stackTrace[2].getMethodName();

        if(PixelSpawners.getInstance().getConfig().getBoolean("enable_debug")) {
            LOGGER.info(callingMethodName+": "+message);
        }
    }

    private static void checkLogger() {
        if (LOGGER == null) {
            LOGGER = PixelSpawners.getInstance().getLogger();
        }
    }

}
