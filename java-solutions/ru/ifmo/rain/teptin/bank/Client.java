package ru.ifmo.rain.teptin.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Arrays;

import static ru.ifmo.rain.teptin.bank.RemoteUtils.*;

public class Client {
    public static void main(final String... args) {
        final String USAGE = "[<first_name> <last_name> <passportID>] <account_id> <delta>";
        if (Arrays.asList(args).contains(null) || (args.length != 5 && args.length != 2)) {
            System.out.println(USAGE);
            return;
        }
        try {
            final Bank bank;
            try {
                bank = (Bank) Naming.lookup(getUrl());
            } catch (final NotBoundException e) {
                System.out.println("Bank is not bound");
                return;
            } catch (final MalformedURLException e) {
                System.out.println("Bank URL is invalid");
                return;
            }


            Person person = null;
            if (args.length == 5) {
                final String firstName = args[0];
                final String lastName = args[1];
                final String passportID = args[2];
//                System.out.println("personGetting\n");
                person = bank.getRemotePerson(passportID);
//                System.out.println("personGot\n");
                if (person == null) {
                    person = bank.createPerson(firstName, lastName, passportID);
//                    System.out.println("personCreated\n");
                } else if (!person.getFirstName().equals(firstName) || !person.getLastName().equals(lastName)) {
                    System.out.println("The owner's name does not match to passportID");
                    return;
                }
//                System.out.println("account\n");
                final String accountID = args[3];
                final Integer delta = getDelta(args[4]);
                if (delta == null) {
                    System.err.println("delta must be an integer");
                    return;
                }
                Account account = person.getAccountByID(accountID);
                if (account == null) {
                    if (bank.getAccount(accountID) != null) {
                        System.out.println("The account already exists and is not owned by the person with id " + passportID);
                        return;
                    }
                    account = bank.createPersonalAccount(accountID, passportID);
                    System.out.println("Created new personal account");
                }
                addMoneyWithMsg(account, delta);
            } else {
                final String accountID = args[0];
                final Integer delta = getDelta(args[1]);
                if (delta == null) {
                    System.err.println("delta must be an integer");
                    return;
                }
                Account account = bank.getAccount(accountID);
                if (account == null) {
                    account = bank.createAccount(accountID);
                    System.out.println("Created new anonymous account");
                }
                addMoneyWithMsg(account, delta);
            }
        } catch (RemoteException e) {
            System.err.println("Problems with remote access: " + e.getMessage());
        }
    }

    private static void addMoneyWithMsg(Account account, Integer delta) throws RemoteException {
        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.setAmount(account.getAmount() + delta);
        System.out.println("Money: " + account.getAmount());
    }

    private static Integer getDelta(String arg) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
