package com.impruvitsolutions.securityrovingsystem;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

    public void sendSMS(Set<String> recipients, String message){
        SmsManager smgr = SmsManager.getDefault();
        for (String recipient:recipients){
            Log.d("send SMS To: ",recipient);
            smgr.sendTextMessage(recipient,null,message,null,null);
        }
    }

    public void sendSMS(String recipient, List<String> message){

    }


}
