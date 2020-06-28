package ru.ifmo.rain.teptin.hello;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class HelloUtils {
    static final String ANS_PREF = "Hello, ";
    static final String CLIENT_USAGE = "Usage: HelloUPDClient <address> <port> <prefix> <threads> <per thread>";
    static void closeExecutor(ExecutorService executorService, int mSecToAwait) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(mSecToAwait, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
    static String getMessage(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }
}
