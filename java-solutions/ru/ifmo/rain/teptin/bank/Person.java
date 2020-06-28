package ru.ifmo.rain.teptin.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Person extends Remote {
    public int getSubID() throws RemoteException;

    public String getFirstName() throws RemoteException;

    public String getLastName() throws RemoteException;

    public String getPassportID() throws RemoteException;

    public Map<String, Account> getAccounts() throws RemoteException;

    public Account getAccountByID(String id) throws RemoteException;
}
