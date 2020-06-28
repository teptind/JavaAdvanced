package ru.ifmo.rain.teptin.bank;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RemoteUtils {
    public final static String HOST = "localhost";
    public final static int PORT = 1488;
    public final static String PATH = "bank";
    public static String getUrl() {
        return String.format("//%s:%d/%s", HOST, PORT, PATH);
    }
    public static void startRegistry() {
        try {
            LocateRegistry.createRegistry(PORT);
        } catch (RemoteException ignored) {}
    }
}
