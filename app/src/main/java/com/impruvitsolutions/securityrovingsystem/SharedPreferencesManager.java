package com.impruvitsolutions.securityrovingsystem;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class SharedPreferencesManager {

    private static final String APP_PREFS = "AppPrefsFile";

    private SharedPreferences sharedPrefs;
    private static SharedPreferencesManager instance;



    public SharedPreferencesManager(Context context) {
        sharedPrefs =
                context.getApplicationContext().getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
    }


    public static synchronized SharedPreferencesManager getInstance(Context context){
        if(instance == null)
            instance = new SharedPreferencesManager(context);

        return instance;
    }

    public void setKey(String name, String val) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(name, val);
        editor.apply();
    }

    public void setKey(String name, Set<String> stringList) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet(name, stringList);
        editor.apply();
    }

    public String getKey(String key) {
        String someValue = sharedPrefs.getString(key, null);
        return someValue;
    }

    public Set<String> getKeyList(String key) {
        Set<String> someValue = sharedPrefs.getStringSet(key, null);
        return someValue;
    }
}