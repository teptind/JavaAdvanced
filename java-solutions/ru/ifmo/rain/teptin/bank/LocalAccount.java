package ru.ifmo.rain.teptin.bank;

public class LocalAccount extends BaseAccount {
    public LocalAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    public LocalAccount(final String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }
}
