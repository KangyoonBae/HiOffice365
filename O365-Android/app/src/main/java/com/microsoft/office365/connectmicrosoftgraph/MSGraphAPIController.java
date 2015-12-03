/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office365.connectmicrosoftgraph;

import com.microsoft.office365.connectmicrosoftgraph.vo.BodyVO;
import com.microsoft.office365.connectmicrosoftgraph.vo.ContactVO;
import com.microsoft.office365.connectmicrosoftgraph.vo.EmailAddressVO;
import com.microsoft.office365.connectmicrosoftgraph.vo.Envelope;
import com.microsoft.office365.connectmicrosoftgraph.vo.MessageVO;
import com.microsoft.office365.connectmicrosoftgraph.vo.MessageWrapper;
import com.microsoft.office365.connectmicrosoftgraph.vo.ToRecipientsVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.Callback;


/**
 * Handles the creation of the message and contacting the
 * mail service to send the message. The app must have
 * connected to Office 365 and discovered the mail service
 * endpoints before using the createDraftMail method.
 */
public class MSGraphAPIController {

    private MSGraphAPIService mMSGraphAPIService;

    public MSGraphAPIController() {
        mMSGraphAPIService = new RESTHelper()
                .getRestAdapter()
                .create(MSGraphAPIService.class);
    }

    /**
     * getContact retrieve all the contacts
     *
     * @param callback     UI callback to be invoked by Retrofit call when
     *                     operation completed
     */
    public void getContact( Callback<Envelope<ContactVO>> callback) {

        // send it using our service
        mMSGraphAPIService.getContact("application/json", callback);
    }

    /**
     * Sends an email message using the Microsoft Graph API on Office 365. The mail is sent
     * from the address of the signed in user.
     *
     * @param emailAddress The recipient email address.
     * @param subject      The subject to use in the mail message.
     * @param body         The body of the message.
     * @param callback     UI callback to be invoked by Retrofit call when
     *                     operation completed
     */
    public void sendMail(
            final String emailAddress,
            final String subject,
            final String body,
            Callback<Void> callback) {
        // create the email
        MessageWrapper msg = createMailPayload(subject, body, emailAddress);

        // send it using our service
        mMSGraphAPIService.sendMail("application/json", msg, callback);
    }


    private MessageWrapper createMailPayload(
            String subject,
            String body,
            String address) {


        //Create List for ToRecipientsVO for sending Mail  KB 12/2
        List<ToRecipientsVO> ToRecipientsVOList = new ArrayList<ToRecipientsVO>();



        //If Address string contains multiple emails, create multiple email toRecipientsVOItem
        //Then put them into ToRecipientsVOList  KB 12/2
        if(address.contains(";")){
            address = address.substring(0,address.length()-1);
            //Create List for Addresses (For Multiple Recipients) KB 12/2
            List<String> addressList = new ArrayList<String>(Arrays.asList(address.split(";")));



            for(String addressFromList:addressList){
                //Temp VO for Looping through Address List  KB 12/2
                EmailAddressVO emailAddressVOTemp = new EmailAddressVO();
                emailAddressVOTemp.mAddress = addressFromList;
                ToRecipientsVO toRecipientsVOItem = new ToRecipientsVO();
                toRecipientsVOItem.emailAddress = emailAddressVOTemp;
                ToRecipientsVOList.add(toRecipientsVOItem);
            }
        }
        //Other wise, create only one toRecipientsVOItem
        //Then put them into ToRecipientsVOList  KB 12/2
        else {
            //Temp VO for Looping through Address List  KB 12/2
            EmailAddressVO emailAddressVOTemp = new EmailAddressVO();
            ToRecipientsVO toRecipientsVOItem = new ToRecipientsVO();
            emailAddressVOTemp.mAddress = address;
            toRecipientsVOItem.emailAddress = emailAddressVOTemp;
            ToRecipientsVOList.add(toRecipientsVOItem);
        }

        //To match code signature, convert ArrayList to Array  KB 12/2
        ToRecipientsVO[] ToRecipientsVOArray = new ToRecipientsVO[ToRecipientsVOList.size()];
        ToRecipientsVOArray = ToRecipientsVOList.toArray(ToRecipientsVOArray);


        BodyVO bodyVO = new BodyVO();
        bodyVO.mContentType = "HTML";
        bodyVO.mContent = body;

        MessageVO sampleMsg = new MessageVO();
        sampleMsg.mSubject = subject;
        sampleMsg.mBody = bodyVO;
        sampleMsg.mToRecipients = ToRecipientsVOArray;
        return new MessageWrapper(sampleMsg);
    }

}
