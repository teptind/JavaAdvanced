package ru.ifmo.rain.teptin.bank;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.net.*;

import static ru.ifmo.rain.teptin.bank.RemoteUtils.*;

public class Server {

    public static void main(final String... args) {
        startRegistry();
        try {
            final Bank bank = new RemoteBank(PORT);
//            UnicastRemoteObject.exportObject(bank, PORT);
            Naming.rebind(getUrl(), bank);
            System.out.println("Server started");
//            Thread.sleep(10000);
//            UnicastRemoteObject.unexportObject(bank, false);
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        } // catch (InterruptedException ignored) {}
    }
}
