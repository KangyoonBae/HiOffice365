/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office365.connectmicrosoftgraph.vo;

import com.google.gson.annotations.SerializedName;

public class EmailAddressVO {

    @SerializedName("name")
    public String mName;

    @SerializedName("address")
    public String mAddress;

}