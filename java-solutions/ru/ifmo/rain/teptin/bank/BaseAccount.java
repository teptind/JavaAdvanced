package ru.ifmo.rain.teptin.bank;

import java.io.Serializable;

public abstract class BaseAccount implements Account, Serializable {
    protected String id;
    protected int amount;
    public String getId() {
        return id;
    }

    public synchronized int getAmount() {
//        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    public synchronized void setAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        if (amount < 0) {
            System.out.println("The amount of money cannot be negative. The setting has been rejected");
            return;
        }
        this.amount = amount;
    }
}
