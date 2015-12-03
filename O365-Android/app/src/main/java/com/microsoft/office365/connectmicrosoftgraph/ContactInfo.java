package com.microsoft.office365.connectmicrosoftgraph;

/**
 * Created by baek on 12/2/2015.
 * ContactInfo Object which contain (Display Name, Name, Email)
 */

public class ContactInfo {
    private String displayName;
    private  String name;
    private String email;
    @Override
    public String toString() {
        return displayName + "\n" + name + "\n" + email + "\n";

    }
    public ContactInfo() {
        super();
    }

    public ContactInfo(String displayName,String name,String email) {
        this.displayName = displayName;
        this.name = name;
        this.email = email;
    }
    public String getDisplayName(){
        return this.displayName;
    }
    public String getName(){
        return this.name;
    }
    public String getEmail(){
        return this.email;
    }

}
