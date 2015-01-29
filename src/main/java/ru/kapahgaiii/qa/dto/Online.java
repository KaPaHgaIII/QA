package ru.kapahgaiii.qa.dto;

public class Online {
    private int users;
    private int guests;

    public Online() {
    }

    public Online(int users, int guests) {
        this.users = users;
        this.guests = guests;
    }

    public int getUsers() {
        return users;
    }

    public void setUsers(int users) {
        this.users = users;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }
}
