package ru.ifmo.rain.teptin.bank.test;

import org.junit.*;
import ru.ifmo.rain.teptin.bank.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ru.ifmo.rain.teptin.bank.RemoteUtils.PORT;
import static ru.ifmo.rain.teptin.bank.RemoteUtils.getUrl;

public class BankTest {
    Bank bank;
    final String baseFirstName = "Daniil";
    final String baseLastName = "Teptin";
    final String basePassport = "01072000";
    final String baseAccountId = "super_id";
    @BeforeClass
    public static void prepareRegistry() {
        RemoteUtils.startRegistry();
    }
    @Before
    public void prepareBank() throws RemoteException, MalformedURLException {
        bank = new RemoteBank(PORT);
        Naming.rebind(getUrl(), bank);
        System.out.println("Bank started");
    }
    @After
    public void releaseBank() throws NoSuchObjectException {
        UnicastRemoteObject.unexportObject(bank, false);
    }
    @Test
    public void SimpleTest() throws RemoteException {
        final int delta = 10;
        for (int i = 0; i < 100; i += delta) {
            Client.main(baseFirstName, baseLastName, basePassport, baseAccountId, Integer.toString(delta));
            Assert.assertEquals(bank.getAccount(baseAccountId).getAmount(), i + delta);
        }
    }
    @Test
    public void localMultipleTest() throws RemoteException {
        Client.main(baseFirstName, baseLastName, basePassport, baseAccountId, "200");
        for (int i = 0; i < 10; ++i) {
            final String accountID = baseAccountId + i;
            bank.createPersonalAccount(accountID, basePassport);
            Person remotePerson = bank.getRemotePerson(basePassport);
            int oldRemoteValue = remotePerson.getAccountByID(accountID).getAmount();

            Person localPerson = bank.getLocalPerson(basePassport);

            // local person contains the same information
            Assert.assertEquals(localPerson.toString(), remotePerson.toString());
            int localValue = 300 + i;
            localPerson.getAccountByID(accountID).setAmount(localValue);
            // local changes
            Assert.assertEquals(localPerson.getAccountByID(accountID).getAmount(), localValue);
            // but remote doesn't
            Assert.assertEquals(bank.getAccount(accountID).getAmount(), oldRemoteValue);

            int newRemoteValue = 228 + i;
            remotePerson.getAccountByID(accountID).setAmount(newRemoteValue);
            // remote changes
            Assert.assertEquals(bank.getAccount(accountID).getAmount(), newRemoteValue);
            // but local doesn't
            Assert.assertEquals(localPerson.getAccountByID(accountID).getAmount(), localValue);
        }
    }
    @Test
    public void subIdAccountsTest() throws RemoteException {
        for (int i = 0; i < 100; ++i) {
            final String firstName = baseFirstName + i;
            final String lastName = baseLastName + i;
            final String passport = basePassport + i;
            Person person = bank.createPerson(baseFirstName + i, baseLastName + i,
                    basePassport + i);
            Assert.assertNotNull(bank.getAccount(String.format("%s:%s", passport, person.getSubID())));
            Assert.assertNotNull(person.getAccountByID(String.format("%s:%s", passport, person.getSubID())));
        }
    }
    @Test
    public void samePassportPersonCreation() throws RemoteException {
        bank.createPerson(baseFirstName, baseLastName, basePassport);
        try {
            bank.createPerson(baseFirstName + "_clone", baseLastName + "_clone", basePassport);
            fail();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
    @Test
    public void sameAccountCreation() throws RemoteException {
        bank.createPerson(baseFirstName, baseLastName, basePassport);
        bank.createAccount(baseAccountId);
        try {
            bank.createPersonalAccount(baseAccountId, basePassport);
            fail();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
    @Test
    public void negativeSettingRefusing() throws RemoteException {
        Client.main(baseFirstName, baseLastName, basePassport, baseAccountId, "60");
        Client.main(baseFirstName, baseLastName, basePassport, baseAccountId, "-40");
        assertEquals(bank.getAccount(baseAccountId).getAmount(), 20);

        // remote access from the bank and from the person
        assertEquals(bank.getAccount(baseAccountId).getAmount(),
                bank.getRemotePerson(basePassport).getAccountByID(baseAccountId).getAmount());

        Client.main(baseFirstName, baseLastName, basePassport, baseAccountId, "-40");
        // the operation has been refused
        assertEquals(bank.getAccount(baseAccountId).getAmount(), 20);
    }
    @Test
    public void finalTest() throws RemoteException {
        int money = 0;
        for (int p = 0; p < 100; ++p) {
            final String firstName = baseFirstName + p;
            final String lastName = baseLastName + p;
            final String passport = basePassport + p;
            for (int a = 1; a < 100; ++a) {
                final String accountID = baseAccountId + "_" + p + "_" + a;
                Client.main(firstName, lastName, passport, accountID, Integer.toString(a));
                money += a;
                Client.main(firstName, lastName + "$", passport, accountID + "$", Integer.toString(a));
                // account isn't created
                Assert.assertNull(bank.getAccount(accountID + "$"));
            }
        }
        int personMoneySum = 0;
        for (int p = 0; p < 100; ++p) {
            Person person = bank.getRemotePerson(basePassport + p);
            for (String id : person.getAccounts().keySet()) {
                personMoneySum += bank.getAccount(id).getAmount();
            }
        }
        assertEquals(personMoneySum, money);
    }
}
