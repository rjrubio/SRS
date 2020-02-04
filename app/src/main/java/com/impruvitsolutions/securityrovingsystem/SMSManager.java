package com.impruvitsolutions.securityrovingsystem;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.List;

public class SMSManager {
    Context context = null;

    public void SMSManager (){
    }

    public void sendSMS(String recipient, String message){
        SmsManager smgr = SmsManager.getDefault();
        smgr.sendTextMessage(recipient,null,message,null,null);
    }

    public void sendSMS(List<HashMap<String,String>> reciepients_messages){

    }

    public void sendSMS(List<String> recipients, String message){
        SmsManager smgr = SmsManager.getDefault();
        for (String recipient:recipients){
            smgr.sendTextMessage(recipient,null,message,null,null);
        }
    }

    public void sendSMS(String recipient, List<String> message){

    }


}
