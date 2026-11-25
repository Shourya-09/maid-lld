package com.rupeek.maidbooking.model.user;

import java.util.UUID;

public class User {
    private String id;
    private String name;
    private String upiId;
    private String creditCardNumber;
    private String walletId;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(String id, String name, String upiId, String creditCardNumber, String walletId) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.upiId = upiId;
        this.creditCardNumber = creditCardNumber;
        this.walletId = walletId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }
}
