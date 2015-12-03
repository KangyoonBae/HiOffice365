/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office365.connectmicrosoftgraph;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.office365.connectmicrosoftgraph.vo.ContactVO;
import com.microsoft.office365.connectmicrosoftgraph.vo.Envelope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * This activity handles the send mail operation of the app.
 * The app must be connected to Office 365 before this activity can send an email.
 * It also uses the MSGraphAPIController to send the message.
 *
 *
 * Added by KB 12/2
 * Import all the contact from Office 365 account
 * Sort them with Display Name
 * Display them in the List View
 * Let user to select group the email or individually.
 */
public class SendMailActivity extends AppCompatActivity {

    // arguments for this activity
    public static final String ARG_GIVEN_NAME = "givenName";
    public static final String ARG_DISPLAY_ID = "displayableId";

    // views
    private EditText mEmailEditText;
    private EditText mEmailBodyEditText;
    private ImageButton mSendMailButton;
    private ProgressBar mSendMailProgressBar;
    private String mGivenName;
    private ArrayList<String> selectedEmailAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);

        // find the views
        TextView mTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mEmailBodyEditText = (EditText) findViewById(R.id.emailBodyEditText);
        mSendMailButton = (ImageButton) findViewById(R.id.sendMailButton);
        mSendMailProgressBar = (ProgressBar) findViewById(R.id.sendMailProgressBar);

        //While Users enter the message hide some UI for better UX KB 12/2
        mEmailBodyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                ListView listView = (ListView) findViewById(R.id.contactListview);
                if (hasFocus) {
                    mSendMailButton.setVisibility(View.INVISIBLE);
                    listView.setVisibility(View.INVISIBLE);
                }
                else{
                    mSendMailButton.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.VISIBLE);
                }
            }
        });

        //When Keyboard is dismissed, show hidden UI back KB 12/2
        mEmailBodyEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    View view = SendMailActivity.this.getCurrentFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    mEmailBodyEditText.clearFocus();
                    return true;
                } else {
                    return false;
                }
            }
        });

        selectedEmailAddresses = new ArrayList<String>();
        // Extract the givenName and displayableId and use it in the UI.
        mGivenName = getIntent().getStringExtra(ARG_GIVEN_NAME);
        mTitleTextView.append(mGivenName + "!");
        mEmailEditText.setText(getIntent().getStringExtra(ARG_DISPLAY_ID));
        mSendMailProgressBar.setVisibility(View.VISIBLE);
        //Get Contact KB 12/1
        getContact();

    }

    //Get Contact by Using MSGraphAPIController KB 12/2
    private void getContact() {
        new MSGraphAPIController()
                .getContact(
                        new Callback<Envelope<ContactVO>>() {
                            @Override
                            public void success(Envelope<ContactVO> contactVOEnvelope, Response response) {
                                //Check the Status KB 12/1
                                if ((" " + response.getStatus()).contains("200")) {

                                    //List of ContactInfo Object KB 12/1
                                    List<ContactInfo> contactInfos = new ArrayList<ContactInfo>();
                                    for (int i = 0; i < contactVOEnvelope.value.length; i++) {
                                        contactInfos.add(new ContactInfo(contactVOEnvelope.value[i].displayName,
                                                contactVOEnvelope.value[i].emailAddresses[0].mName,
                                                contactVOEnvelope.value[i].emailAddresses[0].mAddress));
                                    }
                                    //Sort by Display Name KB 12/1
                                    if (contactInfos.size() > 1) {
                                        Collections.sort(contactInfos, new Comparator<ContactInfo>() {
                                            @Override
                                            public int compare(ContactInfo lhs, ContactInfo rhs) {
                                                return lhs.getDisplayName().compareTo(rhs.getDisplayName());

                                            }
                                        });
                                    }

                                    //Display List
                                    displayList(contactInfos);
                                  //Failed Request (Status is not 200)
                                } else {
                                    Toast.makeText(SendMailActivity.this, "Sorry but we failed to retrieve contact", Toast.LENGTH_SHORT).show();
                                }
                                mSendMailProgressBar.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.e("error", error.getMessage());
                                Toast.makeText(SendMailActivity.this, "Sorry but we failed to retrieve contact", Toast.LENGTH_SHORT).show();
                            }
                        });

    }

    //Display ContactInfos on the ListView KB 12/2
    private void displayList(List<ContactInfo> contactInfos){
        //Use Custom ArraryAdapter KB 12/2
        final ArrayAdapter<ContactInfo> arrayAdapter =
                new ContactArrayAdapter(SendMailActivity.this, R.layout.contact_item, contactInfos);
        ListView listView = (ListView) findViewById(R.id.contactListview);
        listView.setAdapter(arrayAdapter);

        //Set OnItemLongClickListener for group email case  KB 12/2
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int pos, long id) {
                ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);
                checkImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_plus));
                checkImageView.setVisibility(View.INVISIBLE);
                checkImageView.setTag("ADD");
                checkImageView.setVisibility(View.VISIBLE);
                return true;
            }

        });

        //Set OnItemClickListener for Single email case KB 12/2
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);
                checkImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                if (checkImageView.isShown() == false) {
                    checkImageView.setVisibility(View.VISIBLE);
                } else {
                    checkImageView.setVisibility(View.INVISIBLE);
                }
                checkImageView.setTag("SINGLE");
            }
        });

    }



    /**
     * Handler for the onclick event of the send mail button. It uses the MSGraphAPIController to
     * send an email. When the call is completed, the call will return to either the success()
     * or failure() methods in this class which will then take the next steps on the UI.
     * This method sends the email using the address stored in the mEmailEditText view.
     * The subject and body of the message is stored in the strings.xml file.
     *
     * @param v The view.
     */
    public void onSendMailButtonClick(View v) {
        //Variable for Body and Email Address KB 12/2
        String inputMessage = mEmailBodyEditText.getText().toString();
        String tempEmailAddition= "";

        //Show Error Case for empty message KB 12/2
        if(TextUtils.isEmpty(inputMessage)) {
            mEmailBodyEditText.setError("Please enter your message");
            return;
        }
        //Loop through ListView get email addresses  KB 12/2
        ListView tempListView = (ListView) findViewById(R.id.contactListview);
        for (int i = 0; i < tempListView.getCount(); i++) {
            View tempView = (View)tempListView.getChildAt(i);
            ImageView checkImageViewItem = (ImageView) tempView.findViewById(R.id.checkImageView);
            TextView addressTextView = (TextView) tempView.findViewById(R.id.emailTextView);

            //Check all the selected email address  KB 12/2
            if(checkImageViewItem.isShown()){
                //Check type (Single or Add)  KB 12/2
                if(checkImageViewItem.getTag().toString().equalsIgnoreCase("SINGLE")){
                    selectedEmailAddresses.add(addressTextView.getText().toString());
                }
                else{
                    tempEmailAddition = tempEmailAddition + addressTextView.getText().toString() + ";";
                }
            }

        }

        //Adding group email address to address list  KB 12/2
        if(tempEmailAddition.length()>0){
            selectedEmailAddresses.add(tempEmailAddition);
        }

        //If there is no selection then show message  KB 12/2
        if(selectedEmailAddresses.size()<1){
            Toast.makeText(
                    SendMailActivity.this,
                    "Pick someone!",
                    Toast.LENGTH_SHORT).show();
            return;
        }




        //Prepare body message and insert name of sender
        String body = mEmailBodyEditText.getText().toString();
        String emailsTo;

        //Sending Emails  KB 12/2
        for (int i=0;i< selectedEmailAddresses.size();i++) {
            emailsTo = selectedEmailAddresses.get(i);

            final String finalEmailsTo = emailsTo;
            new MSGraphAPIController()
                    .sendMail(
                            emailsTo,
                            "You've got a mail from " + mGivenName,
                            body,
                            new Callback<Void>() {
                                @Override
                                public void success(Void aVoid, Response response) {
                                    showSendMailSuccessUI(finalEmailsTo);
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    showSendMailErrorUI(finalEmailsTo);
                                }
                            });
        }

        resetUIForSendMail();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_mail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder additionalInfoBuilder = null;
        switch (item.getItemId()) {
            case R.id.disconnectMenuitem:
                AuthenticationManager.getInstance().disconnect();
                Intent connectIntent = new Intent(this, ConnectActivity.class);
                startActivity(connectIntent);
                finish();
                return true;

            //Information for Email Delivery  KB 12/2
            case R.id.InfoMenuitem:
                AlertDialog.Builder notificationDialog = new AlertDialog.Builder(this);
                LayoutInflater inflater = (LayoutInflater) this
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.popup,
                        (ViewGroup) findViewById(R.id.popup_root));

                notificationDialog
                        .setTitle("About The Delivery");
                notificationDialog.setMessage(R.string.info_text);

                SpannableString spanButtonNeg = new SpannableString("Got it!");
                notificationDialog.setNegativeButton(spanButtonNeg,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogOK, int which) {
                                dialogOK.dismiss();

                            }
                        });
                notificationDialog.show();
                return true;

        default:
        return super.onOptionsItemSelected(item);
    }
    }

    private void resetUIForSendMail() {
        mSendMailButton.setVisibility(View.GONE);
        mSendMailProgressBar.setVisibility(View.VISIBLE);
        mEmailBodyEditText.setText("");
        selectedEmailAddresses.clear();
    }

    private void showSendMailSuccessUI(String sentAddress) {
        mSendMailProgressBar.setVisibility(View.GONE);
        mSendMailButton.setVisibility(View.VISIBLE);
        //Show Success Message  KB 12/2
        Toast.makeText(
                SendMailActivity.this,getString(R.string.conclusion_text)
                 + sentAddress,
                Toast.LENGTH_SHORT).show();

        ListView tempListView = (ListView) findViewById(R.id.contactListview);

        //Reset ListView
        for (int i = 0; i < tempListView.getCount(); i++) {
            View tempView = (View)tempListView.getChildAt(i);
            ImageView checkImageViewItem = (ImageView) tempView.findViewById(R.id.checkImageView);
            checkImageViewItem.setVisibility(View.INVISIBLE);
        }
        selectedEmailAddresses.clear();

    }

    private void showSendMailErrorUI(String errorAddress) {
        mSendMailProgressBar.setVisibility(View.GONE);
        mSendMailButton.setVisibility(View.VISIBLE);
        //Show Error Message  KB 12/2
        Toast.makeText(
                SendMailActivity.this,getString(R.string.send_mail_toast_text_error) + errorAddress,
                Toast.LENGTH_LONG).show();
    }


}