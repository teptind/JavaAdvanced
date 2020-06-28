package ru.ifmo.rain.teptin.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     *
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;

    Account createPersonalAccount(String id, String passportID) throws RemoteException;

    Person createPerson(final String firstName, final String lastName, final String passportID) throws RemoteException;

    Person getRemotePerson(final String passportID) throws RemoteException;

    Person getLocalPerson(final String passportID) throws RemoteException;
}
