package br.com.qualidata.calendar.model;

import android.support.annotation.ColorInt;

/**
 * Created by Ricardo on 02/11/2015.
 */
public class Calendar {

    private final long id;
    private final String name, displayName;

    private String syncId;

    private String accountType, accountName;

    private String ownerAccount;
    private boolean isVisible, isPrimary;
    private boolean syncEvents;

    private @ColorInt int color;

    public Calendar(long id, String name, String displayName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSyncId() {
        return syncId;
    }

    public void setSyncId(String syncId) {
        this.syncId = syncId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getOwnerAccount() {
        return ownerAccount;
    }

    public void setOwnerAccount(String ownerAccount) {
        this.ownerAccount = ownerAccount;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public boolean isSyncEvents() {
        return syncEvents;
    }

    public void setSyncEvents(boolean syncEvents) {
        this.syncEvents = syncEvents;
    }

    public @ColorInt int getColor() {
        return color;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }
}
