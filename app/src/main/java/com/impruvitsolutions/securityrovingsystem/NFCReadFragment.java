package com.impruvitsolutions.securityrovingsystem;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.net.sip.SipSession;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.tech.Ndef;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NFCReadFragment extends Fragment {
    public static final String TAG = NFCReadFragment.class.getSimpleName();
    private int counter = 0;
    private List<String> location_lists = Arrays.asList(new String[]{"Column1", "Column2","Column3","Column4","Column5","Column6","Column7"});
    private List<String> recipients_lists = Arrays.asList(new String[]{"09053635521", "09563371805","09665515556"});

    public static NFCReadFragment newInstance() {

        return new NFCReadFragment();
    }

    private TextView mTvTargetLoacation;
    private TextView mTvStatus;
    private TextView mTvNextLocation;
    private Listener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nfcread,container,false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mTvTargetLoacation = view.findViewById(R.id.target_location_tv);
        mTvStatus = view.findViewById(R.id.textViewStatus);
        mTvStatus.setText("PUT READER ON BEACON.");
        mTvNextLocation = view.findViewById(R.id.next_location_tv);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (MainActivity)context;
        mListener.onDialogDisplayed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDialogDismissed();
    }

    public void onNfcDetected(String output, String user){

        readFromNFC(output, user);
    }

    private void readFromNFC(String output, String user) {
        if(doRoving(output, user)){
            counter++;
        }
    }

    private boolean doRoving(String output, String user){
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        for (String location : location_lists) {
            if (count == counter && location.equals(output)) {
                mTvTargetLoacation.setText(output);
                mTvStatus.setText("SUCCESS! \n"+currentDateandTime);
                mTvStatus.setTextColor(Color.GREEN);
                if(counter == location_lists.size()-1)
                {
                    mTvStatus.setText("Completed! \n"+currentDateandTime);
                    mTvStatus.setTextColor(Color.GREEN);
                    counter = 0;
                }else{
                    mTvNextLocation.setText(location_lists.get(count+1));
                }
                notifySMS(output,user,location, currentDateandTime);
                return true;
            }
            count++;
        }
        mTvStatus.setText("ERROR! \n"+currentDateandTime);
        mTvStatus.setTextColor(Color.RED);
        return  false;

    }

    private void notifySMS(String output, String user, String location, String datetime){
        String messageToSend = "(SRS) CHECKED IN SUCCESSFULLY ! LOCATION: "+output+" BY : "+user+ " DATETIME: "+datetime+"\n\n\n\nPOWEREDBY: IMPRUVITSOLUTIONS";
        SMSManager sm = new SMSManager();
        sm.sendSMS(recipients_lists,messageToSend);

    }
}
