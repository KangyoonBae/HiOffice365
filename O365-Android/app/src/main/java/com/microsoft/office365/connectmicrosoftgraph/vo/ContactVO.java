package com.microsoft.office365.connectmicrosoftgraph.vo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by baek on 12/1/2015.
 */
public class ContactVO {
    @SerializedName("displayName")
    public String displayName;
    @SerializedName("emailAddresses")
    public EmailAddressVO[] emailAddresses;
}
