package ru.ifmo.rain.teptin.bank;

public class RemoteAccount extends BaseAccount {

    public RemoteAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    public RemoteAccount(final String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }
}
