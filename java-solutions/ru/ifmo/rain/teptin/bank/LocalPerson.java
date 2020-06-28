package ru.ifmo.rain.teptin.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LocalPerson extends BasePerson implements Serializable {
    LocalPerson(int subID, String firstName, String lastName, String passportID, Map<String, Account> accounts) {
        this.subID = subID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportID = passportID;
        this.accounts = new HashMap<>(accounts);
    }
}
