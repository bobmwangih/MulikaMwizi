package com.example.technician.mulikamwizi.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.example.technician.mulikamwizi.activity.MyRecs;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import io.paperdb.Paper;

public class MyService extends JobService {

    /**private TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

     private String SIMSerialNumber = manager.getSimSerialNumber(); **/

    static final int PERMISSION_READ_STATE = 123;
    static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    String longitude, latitude;

    private String simcardSerial = Paper.book().read(MyRecs.SIM_MSI);
    private String simImei = Paper.book().read(MyRecs.SIM_IMEI);
    private String sending_location = Paper.book().read(MyRecs.SENDING_LOCATION);

    String default_phone = Paper.book().read(MyRecs.USER_PHONE);
    String default_user = Paper.book().read(MyRecs.USER_NAME);

    BackgroundTask backgroundTask;

    @Override
    public boolean onStartJob(final JobParameters job) {

        backgroundTask = new BackgroundTask() {
            @Override
            protected void onPostExecute(String s) {
                TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String SIMSerialNumber = manager.getSimSerialNumber();
                if (simcardSerial != null){
                    if ((SIMSerialNumber).equals(simcardSerial)){
                        //do nothing
                        Toast.makeText(MyService.this, "Authenticated", Toast.LENGTH_SHORT).show();
                    }else {
                        sendMessage();
                    }
                }
                //Toast.makeText(MyService.this, "message from task"+s, Toast.LENGTH_LONG).show();
                jobFinished(job,true);
            }
        };

        backgroundTask.execute();
        return true;
    }

    private void sendMessage() {
        String sms = ("Hello "+default_user+ ". Your line has been switched." + "\n PhONE LOCATION: "+sending_location +"." +"\n IMEI IS: "
                + simImei);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(default_phone, null, sms, null, null);
            Toast.makeText(this, "message sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

  /**  private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location!=null){
                double latti=location.getLatitude();
                double longi=location.getLongitude();
                latitude=String.valueOf(latti);
                longitude=String.valueOf(longi);
            sending_location =  String.format("location is:"+"\n"+"Lattitude: "+latitude+"\n"+"Longitude: "+longitude);
              //  textView.setText("Your mobile phone location is:"+"\n"+"Lattitude:  "+latitude+"\n"+"Longitude:  "+longitude);
            } else{
                Toast.makeText(this,"unable to trace the mobile phones location,try again later!",Toast.LENGTH_LONG).show();
            }

        }


    } **/

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }

    public static class BackgroundTask extends AsyncTask<Void,Void,String>
    {


        @Override
        protected String doInBackground(Void... voids) {
            return null;
        }
    }
}
