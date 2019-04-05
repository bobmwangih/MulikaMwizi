package com.example.technician.mulikamwizi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.AndroidException;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.technician.mulikamwizi.activity.MyRecs;
import com.example.technician.mulikamwizi.activity.ServiceActivity;
import com.example.technician.mulikamwizi.constant.Constant;
import com.example.technician.mulikamwizi.service.MyService;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sendgrid.SendGrid;

import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private static final String TAG = "HomeActivity";
    private Button btnRemoveUser, remove, signOut;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    private static final String Job_Tag = "my_job_tag";
    private FirebaseJobDispatcher jobDispatcher;

    Button btnStart;
    TextView varText;
    public String info;
    String strPhoneType = "";
    static final int PERMISSION_READ_STATE = 123;
    static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    TextView textView;
    String longitude, latitude;
    Button button;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        Paper.init(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        getApplicationContext();


        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(Home.this, LoginActivity.class));
                    finish();
                }
            }
        };

        // btnRemoveUser = (Button) findViewById(R.id.remove_user_button);
        remove = findViewById(R.id.remove);
        // signOut = (Button) findViewById(R.id.sign_out);
        remove.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
//location stuff
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        textView = findViewById(R.id.id_textview);
        button = findViewById(R.id.button_location);
        button.setOnClickListener(this);
        //send sms prmissions
   /**   if(ContextCompat.checkSelfPermission(Home.this,Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED) {
          if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.SEND_SMS)) {
              ActivityCompat.requestPermissions(Home.this,
                      new String[]{Manifest.permission.SEND_SMS}, 1);
          } else {
              ActivityCompat.requestPermissions(Home.this,
                      new String[]{Manifest.permission.SEND_SMS}, 1);
          }
      }else{
              //do nothing
          } **/


          if (ContextCompat.checkSelfPermission(Home.this,
         android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
         if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this,
         android.Manifest.permission.SEND_SMS)) {
         ActivityCompat.requestPermissions(Home.this,
         new String[]{android.Manifest.permission.SEND_SMS}, 1);
         } else {
         ActivityCompat.requestPermissions(Home.this,
         new String[]{Manifest.permission.SEND_SMS}, 1);
         }
         } else {
         //do nothing
         }

    }


 public void startJob(){
     Job job = jobDispatcher.newJobBuilder().
             setService(MyService.class).
             setLifetime(Lifetime.FOREVER).
             setRecurring(true).
             setTag(Job_Tag).
             setTrigger(Trigger.executionWindow(10,15)).
             setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL).
             setReplaceCurrent(false).
              setConstraints(Constraint.ON_ANY_NETWORK)
             .build();

     jobDispatcher.mustSchedule(job);
     Toast.makeText(this, "background service started", Toast.LENGTH_SHORT).show();
 }

 public  void stopJob(){
jobDispatcher.cancel(Job_Tag);
     Toast.makeText(this, "job cancelled", Toast.LENGTH_SHORT).show();
 }

    public void removeUser() {
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(Home.this, LoginActivity.class));
                    finish();
                }
            }
        };
        progressBar.setVisibility(View.VISIBLE);
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Home.this, "Your profile is deleted:( Create a account now!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(Home.this, RegisterActivity.class));
                                finish();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(Home.this, "Failed to delete your account!", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }





    public void signOut() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        TextView nagText = findViewById(R.id.textView);
        SharedPreferences sharedPref = getSharedPreferences("registerInfo", Context.MODE_PRIVATE);
        String name = sharedPref.getString("name", "");
        String email = sharedPref.getString("email", "");
        String phone = sharedPref.getString("phone", "");
        nagText.setText("Name: " + name + "\n" + "Email: " + email + "\n Phone: " + phone);

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_signout) {
            signOut();
        } else if (id == R.id.nav_remove) {
            removeUser();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void Start(View view) {

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            MyTelephonyManager();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSION_READ_STATE);
        }

    }

  /**  @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: PERMISSION_READ_STATE: {
                if (grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MyTelephonyManager();
                } else {
                    Toast.makeText(this, "You dont have the required permission!", Toast.LENGTH_LONG).show();

                }
            }
            case 2:{
                if(grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(Home.this,
                            Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(this, "no permission granted", Toast.LENGTH_SHORT).show();
                } return;
            }
        }
    } **/

  @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(Home.this,
                            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No permission granted", Toast.LENGTH_SHORT).show();
                }

                return;
            }
            case 2: PERMISSION_READ_STATE: {
                if (grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MyTelephonyManager();
                } else {
                    Toast.makeText(this, "You dont have the required permission!", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void MyTelephonyManager() {

        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int phoneType = manager.getPhoneType();
        switch (phoneType) {
            case (TelephonyManager.PHONE_TYPE_CDMA):
                strPhoneType = "CDMA";
                break;
            case (TelephonyManager.PHONE_TYPE_GSM):
                strPhoneType = "GSM";
                break;
            case (TelephonyManager.PHONE_TYPE_NONE):
                strPhoneType = "NONE";
                break;

        }
        boolean isRoaming = manager.isNetworkRoaming();

        String PhoneType = strPhoneType;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String IMEINumber = manager.getImei();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String subscriberID = manager.getSubscriberId();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
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
        String networkCountryISO = manager.getNetworkCountryIso();
        String SIMCountryISO = manager.getSimCountryIso();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String softwareVersion = manager.getDeviceSoftwareVersion();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String voiceMailNumber = manager.getVoiceMailNumber();

        info = "Phone Details: \n";
        info += "\n Phone network type:" + PhoneType;
        info += "\n IMEI NUMBER:" + IMEINumber;
        info += "\n SUBSCRIBER:" + subscriberID;
        info += "\n SIM SERIAL:" + SIMSerialNumber;
        info += "\n NETWORK COUNTRY:" + networkCountryISO;
        info += "\n SIM COUNTRY:" + SIMCountryISO;
        info += "\n SOFTWARE VERSION:" + softwareVersion;
        info += "\n VOICEMAIL NUMBER:" + voiceMailNumber;
        info += "\n ROAMING:" + isRoaming;
        btnStart = findViewById(R.id.idBtnStart);


        varText = findViewById(R.id.idTxtView);
        varText.setText(info);

    }
//start background service
    public void startService(View view) {


    }
/*
    public void scheduleJob(View view) {
        ComponentName componentName = new ComponentName(this, ExampleJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }

    public void cancelJob(View view) {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Log.d(TAG, "job cancelled");
    }
*/

    @Override
    public void onClick(View view) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
            startJob();
            Intent i = new Intent(getApplicationContext(), ServiceActivity.class);
            startActivity(i);
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location!=null){
                double latti=location.getLatitude();
                double longi=location.getLongitude();
                latitude=String.valueOf(latti);
                longitude=String.valueOf(longi);
                Paper.book().write(MyRecs.SENDING_LOCATION,latitude+" , "+longitude);
                textView.setText("Your mobile phone location is:"+"\n"+"Lattitude:  "+latitude+"\n"+"Longitude:  "+longitude);
            } else{
                Toast.makeText(this,"",Toast.LENGTH_LONG).show();
            }

        }


    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage("Please turn your GPS on!").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog,final int id) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

            }
        })
       .setNegativeButton("NO", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(final DialogInterface dialog,final int id) {
               dialog.cancel();

           }
       });
        final AlertDialog alert=builder.create();
        alert.show();
    }


    public void testEmail(View view) {
        stopJob();

}

}
