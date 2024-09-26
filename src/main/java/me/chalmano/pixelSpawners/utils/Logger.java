package me.chalmano.pixelSpawners.utils;

import me.chalmano.pixelSpawners.PixelSpawners;

public class Logger {

    private static java.util.logging.Logger LOGGER;

    public static void info(String message) {

        if (!PixelSpawners.getInstance().getConfig().getBoolean("enable_debug")) {
            return;
        }

        checkLogger();

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // The element at index 2 is methodB, index 3 will be methodA
        String callingMethodName = stackTrace[2].getMethodName();

        LOGGER.info(callingMethodName + ": " + message);


//        Class<?> caller = StackWalker
//                .getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
//                .getCallerClass();
//        System.out.println(caller.getCanonicalName());
    }

    private static void checkLogger() {
        if (LOGGER == null) {
            LOGGER = PixelSpawners.getInstance().getLogger();
        }
    }

}
