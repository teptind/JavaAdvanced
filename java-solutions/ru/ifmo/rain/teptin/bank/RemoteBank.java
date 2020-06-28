package ru.ifmo.rain.teptin.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank extends UnicastRemoteObject implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) throws RemoteException {
        super(port);
        this.port = port;
    }

    private void checkAccount(final String id) {
        if (accounts.containsKey(id)) {
            throw new IllegalArgumentException(String.format("The account with id %s is already exists", id));
        }
    }

    private void checkPerson(final String passportID) {
        if (persons.containsKey(passportID)) {
            throw new IllegalArgumentException(String.format("The person with passport id %s is already exists", passportID));
        }
    }

    public Account createAccount(final String id) throws RemoteException {
        if (id == null) {
            throw new NullPointerException("Non-null account id is required");
        }
        checkAccount(id);
        System.out.println("Creating account " + id);
        final Account account = new RemoteAccount(id);
        UnicastRemoteObject.exportObject(account, port);
        accounts.put(id, account);
        return account;
    }
    public Account createPersonalAccount(final String id, final String passportID) throws RemoteException {
        if (id == null || passportID == null) {
            throw new NullPointerException("Non-null account id and person passport id are required");
        }
        checkAccount(id);
        Person person = persons.get(passportID);
        if (person == null) {
            throw new NullPointerException(String.format("No person with id %s found", passportID));
        }
        System.out.println("Creating personal account " + id + String.format(" (Person's passportID: %s)", passportID));
        final Account account = new RemoteAccount(id);
        accounts.put(id, account);
        persons.get(passportID).getAccounts().put(id, account);
        UnicastRemoteObject.exportObject(account, port);
        return account;
    }

    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    public Person createPerson(final String firstName, final String lastName, final String passportID) throws RemoteException {
//        System.out.println(String.format("%s %s %s", firstName, lastName, passportID));
        if (firstName == null || lastName == null || passportID == null) {
            throw new NullPointerException("Non-null person data is required");
        }
//        String new_id = String.format("%s:%s", passportID, id);
        checkPerson(passportID);
        System.out.println(String.format("Creating person %s %s (passportId: %s)", firstName, lastName, passportID));
        final Person person = new RemotePerson(persons.size(), firstName, lastName, passportID, new ConcurrentHashMap<>());
//        System.out.println("Person created successfully");
        persons.put(passportID, person);
//        System.out.println("put");
        UnicastRemoteObject.exportObject(person, port);
//        System.out.println("exported");
        createPersonalAccount(String.format("%s:%d", person.getPassportID(), person.getSubID()), passportID);
        System.out.println(String.format("Created person %s %s (passportId: %s)", firstName, lastName, passportID));
        return person;
    }

    public Person getRemotePerson(final String passportID) {
        System.out.println(String.format("Retrieving remote person with passport id %s", passportID));
        return persons.get(passportID);
    }

    private Map<String, Account> copyLocalAccounts(Map<String, Account> remotePersonAccounts) throws RemoteException {
        System.out.println("Creating local copies of remote accounts");
        Map<String, Account> localPersonAccounts = new ConcurrentHashMap<>();
        for (String id : remotePersonAccounts.keySet()) {
            localPersonAccounts.put(id, new LocalAccount(id, remotePersonAccounts.get(id).getAmount()));
        }
        return localPersonAccounts;
    }

    public Person getLocalPerson(final String passportID) throws RemoteException {
        System.out.println(String.format("Retrieving local person with passport id %s", passportID));
        Person person = persons.get(passportID);
        if (person == null) {
            return null;
        }
        return new LocalPerson(person.getSubID(), person.getFirstName(),
                person.getLastName(),
                person.getPassportID(),
                copyLocalAccounts(person.getAccounts()));
    }
}
