package ru.ifmo.rain.teptin.bank;

import java.util.HashMap;
import java.util.Map;

public class RemotePerson extends BasePerson {
    RemotePerson(int subID, String firstName, String lastName, String passportID, Map<String, Account> accounts) {
        this.subID = subID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportID = passportID;
        this.accounts = new HashMap<>(accounts);
    }
}
