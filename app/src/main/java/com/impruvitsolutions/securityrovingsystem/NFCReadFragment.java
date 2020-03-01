package com.impruvitsolutions.securityrovingsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NFCReadFragment extends Fragment {
    public static final String TAG = NFCReadFragment.class.getSimpleName();
    private int counter = 0;
    private String user = "";
    private List<String> location_lists = Arrays.asList(new String[]{"Column1", "Column2","Column3","Column4","Column5","Column6","Column7"});

    public static NFCReadFragment newInstance(String user) {

        return new NFCReadFragment();
    }

    public void NFCReadFragment(String user){
        this.user = user;
    }
    private Listener mListener;
    private TextView tv_status_message, tv_location, tv_checkpoint, tv_date, tv_time, tv_user;
    private ImageView image_view_holder;
    private Button add_number;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nfcread,container,false);
        context = this.getContext();
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        tv_status_message = view.findViewById(R.id.tv_status_message);
        tv_location = view.findViewById(R.id.tv_location);
        tv_checkpoint = view.findViewById(R.id.tv_checkpoint);
        tv_date = view.findViewById(R.id.tv_date);
        tv_time = view.findViewById(R.id.tv_time);
        tv_user = view.findViewById(R.id.tv_user);
        image_view_holder = view.findViewById(R.id.image_view_holder);
        tv_user.setText(user);
        add_number = view.findViewById(R.id.add_number_btn);
        add_number.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Add recipient number");

// Set up the input
                final EditText input = new EditText(context);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.recipients_lists.add(input.getText().toString());
                        SharedPreferencesManager sharedPreferences = new SharedPreferencesManager(context);
                        sharedPreferences.setKey("recipients",MainActivity.recipients_lists);
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

        SimpleDateFormat time = new SimpleDateFormat("kk:mm", Locale.getDefault());
        String time_t = time.format(new Date());

        SimpleDateFormat date = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
        String date_t = date.format(new Date());

        tv_user.setText(user);
        tv_time.setText(time_t);
        tv_date.setText(date_t);

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
        if(tv_user != null){
            tv_user.setText(user);
        }
        readFromNFC(output, user);
    }

    private void readFromNFC(String output, String user) {
        if(doRoving(output, user)){
            counter++;
        }
    }

    private boolean doRoving(String output, String user){
        int count = 0;
        SimpleDateFormat time = new SimpleDateFormat("kk:mm", Locale.getDefault());
        String time_t = time.format(new Date());

        SimpleDateFormat date = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
        String date_t = date.format(new Date());

        tv_user.setText(user);
        tv_time.setText(time_t);
        tv_date.setText(date_t);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        if(output.equals(user)){
            tv_user.setText(user);
            tv_location.setText(location_lists.get(0));
            return false;
        }
        for (String location : location_lists) {
            if (count == counter && location.equals(output)) {
                tv_location.setText(output);
                tv_status_message.setText("SUCCESS");
                tv_time.setTextColor(Color.GREEN);
                image_view_holder.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_green));
                if(counter == location_lists.size()-1)
                {
                    tv_status_message.setText("Completed!");
                    counter = 0;
                }else{
                    tv_location.setText(location_lists.get(count+1));
                }
                notifySMS(output,user,location, currentDateandTime);
                return true;
            }
            count++;
        }
        image_view_holder.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_red));
        tv_status_message.setText("FAIL");
        tv_time.setTextColor(Color.RED);
        return  false;

    }

    private void notifySMS(String output, String user, String location, String datetime){
        String messageToSend = "(SRS) CHECKED IN SUCCESSFULLY ! \nLOCATION: "+output+" \nBY : "+user+ "\nDATETIME: "+datetime+"\n\n\n\nPOWEREDBY: IMPRUVITSOLUTIONS";
        SMSManager sm = new SMSManager();
        sm.sendSMS(new SharedPreferencesManager(getActivity()).getKeyList("recipients"),messageToSend);

    }
}
