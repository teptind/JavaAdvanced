package ru.ifmo.rain.teptin.bank;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public abstract class BasePerson implements Person, Serializable {
    String firstName, lastName, passportID;
    int subID;
    protected Map<String, Account> accounts;

    @Override
    public int getSubID() {
        return subID;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassportID() {
        return passportID;
    }

    @Override
    public Account getAccountByID(String id) {
        return accounts.get(id);
    }

    @Override
    public Map<String, Account> getAccounts() {
        return accounts;
    }

    @Override
    public String toString() {
        return "BasePerson{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", passportID='" + passportID + '\'' +
                ", subID=" + subID +
                '}';
    }
}
