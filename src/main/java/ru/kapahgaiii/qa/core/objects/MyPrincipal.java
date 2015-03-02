package ru.kapahgaiii.qa.core.objects;

import java.security.Principal;

public class MyPrincipal implements Principal {

    private String name;

    public MyPrincipal() {
    }

    public MyPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
