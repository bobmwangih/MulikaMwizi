package com.example.technician.mulikamwizi.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;


import com.example.technician.mulikamwizi.constant.Constant;
import com.example.technician.mulikamwizi.model.LocationAlertPojo;
import com.sendgrid.SendGrid;
import com.sendgrid.SendGrid.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SendEmailAPI extends AsyncTask<LocationAlertPojo, Void, Void> {
    private Context context;

    public SendEmailAPI(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(LocationAlertPojo... params) {
        try {

            // Get user email from SharedPreference
           /* Set<String> keys =  new HashSet<>(Arrays.asList("email", "phone"));
            Map<String, String> data = SharedPreferenceAPI.readSharePreferences(context, Constant.USER, keys);
            String emailAddress = data.get("email"); */
            String emailAddress="bobbymwangih@gmail.com";
            String phoneNumber ="0702319245";

           // String phoneNumber  = data.get("phone");
           Log.d("user email is ", emailAddress);
           if (emailAddress == null && emailAddress.isEmpty() && emailAddress.equals("null"))
                   emailAddress="bobbymwangih@gmail.com";

            // Get user's previous locationAlertObject if available
            Set<String> locationKeys = new HashSet<>(Arrays.asList(Constant.LONGITUDE, Constant.LATITUDE, Constant.ROOTCAUSE));
            Map<String, String> locationData = SharedPreferenceAPI.readSharePreferences(context, Constant.LOCATION, locationKeys);
            LocationAlertPojo alertPojo = params[0];
            if (locationData.keySet().size() > 0) {
                if (!Objects.equals(locationData.get(Constant.LONGITUDE), alertPojo.getLongitude()) ||
                        !locationData.get(Constant.LATITUDE).equals(alertPojo.getLatitude()) ||
                        !locationData.get(Constant.ROOTCAUSE).equals(alertPojo.getRootcause())) {
                    String txtMsg = constructMsg(alertPojo);

                    // Send SMS if location change or different root cause
                    if (phoneNumber != null) {
                        Log.d("Send SMS", "activity");
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNumber, null, txtMsg, null, null);
                    }

                    // Only send email if location change or different root cause
                    if (emailAddress != null) {
                        Log.d("Send email", "activity");
                        SendGrid sendgrid = new SendGrid(Constant.SEND_EMAIL_API_KEY);
                        Email email = new Email();
                        email.addTo(emailAddress);
                        email.setFrom(emailAddress);
                        email.setSubject("AntiTheft Alert !!!!!!!");
                        email.setHtml(txtMsg);
                        SendGrid.Response response = sendgrid.send(email);
                    }
                }
            } else {
                Log.d("Same : " + alertPojo.getRootcause(), alertPojo.getFullAddress());
            }
            locationData.put(Constant.LONGITUDE, alertPojo.getLongitude());
            locationData.put(Constant.LATITUDE, alertPojo.getLatitude());
            locationData.put(Constant.ROOTCAUSE, alertPojo.getRootcause());
            SharedPreferenceAPI.storeSharedPreferences(context, Constant.LOCATION, locationData);

        } catch (Exception e) {
            Log.e("Exception ", e.getMessage());
        }

        return null;
    }

    public String constructMsg(LocationAlertPojo alertPojo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Root cause is: ").append(alertPojo.getRootcause()).append("\n");
        sb.append("Current location of the device: ").append(alertPojo.getFullAddress());
        return sb.toString();
    }

    public void execute(String s) {
    }
}
