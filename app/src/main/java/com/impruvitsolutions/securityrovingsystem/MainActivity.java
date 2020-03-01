package com.impruvitsolutions.securityrovingsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements Listener{

    private List<String> user_lists = Arrays.asList(new String[]{"gabe", "apars","ron"});
    public static Set<String> recipients_lists = new HashSet<>(Arrays.asList(new String[]{"09053635521", "09563371805","09665515556"}));
    private String userLogIn ="noOne";

    public static final String TAG = MainActivity.class.getSimpleName();

    private TextView mEtMessage;
    private NFCReadFragment mNfcReadFragment;

    private boolean isDialogDisplayed = false;

    private NfcAdapter mNfcAdapter;
    private Button add_number;
    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Ask for permision
            ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.SEND_SMS}, 1);
        } else {

        }
         mContext = this;
        initViews();

        //Check if NFC is available on device
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            mEtMessage.setText("This device doesn't support NFC.");
            mEtMessage.setTextColor(Color.RED);

        }
        initNFC();
        SharedPreferencesManager sharedPreferences = new SharedPreferencesManager(getApplicationContext());
        sharedPreferences.setKey("recipients",recipients_lists);
    }

    private void initViews() {
        mEtMessage = findViewById(R.id.textViewLoginStatus);
        add_number = findViewById(R.id.add_number_btn);
        add_number.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Add recipient number");

// Set up the input
                final EditText input = new EditText(mContext);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recipients_lists.add(input.getText().toString());
                        SharedPreferencesManager sharedPreferences = new SharedPreferencesManager(mContext);
                        sharedPreferences.setKey("recipients",recipients_lists);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    private void initNFC(){

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    private void showReadFragment(String user) {

        mNfcReadFragment = (NFCReadFragment) getSupportFragmentManager().findFragmentByTag(NFCReadFragment.TAG);

        if (mNfcReadFragment == null) {

            mNfcReadFragment = NFCReadFragment.newInstance(user);
        }
        onDialogDisplayed();
        getSupportFragmentManager().beginTransaction().add(R.id.main_layout,mNfcReadFragment,NFCReadFragment.TAG).commit();
        getSupportFragmentManager().executePendingTransactions();

    }

    @Override
    public void onDialogDisplayed() {

        isDialogDisplayed = true;
    }

    @Override
    public void onDialogDismissed() {

        isDialogDisplayed = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if(mNfcAdapter!= null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter!= null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNfcIntent(intent);

    }

    private void handleNfcIntent(Intent NfcIntent) {


        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(NfcIntent.getAction())) {
            Parcelable[] receivedArray =
                    NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            Tag myTag = NfcIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if(receivedArray != null) {
                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();
                for (NdefRecord record:attachedRecords) {
                    byte[] payloadBytes = record.getPayload();
                    boolean isUTF8 = (payloadBytes[0] & 0x080) == 0;  //status byte: bit 7 indicates encoding (0 = UTF-8, 1 = UTF-16)
                    int languageLength = payloadBytes[0] & 0x03F;     //status byte: bits 5..0 indicate length of language code
                    int textLength = payloadBytes.length - 1 - languageLength;
                    try {
                        String languageCode = new String(payloadBytes, 1, languageLength, "US-ASCII");
                        String payloadText = new String(payloadBytes, 1 + languageLength, textLength, isUTF8 ? "UTF-8" : "UTF-16");
                        //Make sure we don't pass along our AAR (Android Application Record)
                        if (payloadText.equals(getPackageName())) { continue; }

                        if (isDialogDisplayed) {
                            mNfcReadFragment = (NFCReadFragment)getSupportFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
                            mNfcReadFragment.onNfcDetected(payloadText, userLogIn);

                        }else {
                            if(authenticate(payloadText)){
                                mEtMessage.setText("Welcome"+ userLogIn);
                                mEtMessage.setTextColor(Color.GREEN);
                                String messageToSend = "(SRS) BADGED IN SUCCESSFULLY !\n USER : "+payloadText+"\n\n\n\nPOWEREDBY: IMPRUVITSOLUTIONS";
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                String currentDateandTime = sdf.format(new Date());
                                //String messageToSend = "Hortelano, "+payloadText+" (071-014526)"+" has logged in and is inside the school presmises at " +currentDateandTime+" This ia an official text message from IMPRUV UNIVERSITY";
                                SMSManager sm = new SMSManager();
                                sm.sendSMS(recipients_lists,messageToSend);
                                showReadFragment(payloadText);
                                mNfcReadFragment.onNfcDetected(payloadText, userLogIn);
                            }else{
                                mEtMessage.setText("Invalid USER");
                                mEtMessage.setTextColor(Color.RED);
                            }

                        }
                    }catch (UnsupportedEncodingException ex){
                        mEtMessage.setText("ERROR READING NFC TAG "+ ex.toString());
                        mEtMessage.setTextColor(Color.RED);
                    }
                }
            }
            else {
                mEtMessage.setText("ERROR EMPTY NFC TAG ");
                mEtMessage.setTextColor(Color.RED);
            }
        }
    }

    public boolean authenticate (String tagValue) {
        for (String user : user_lists) {
            if (tagValue.equals(user)) {
                userLogIn = user;
                return true;
            }
        }

        return false;
    }
}
