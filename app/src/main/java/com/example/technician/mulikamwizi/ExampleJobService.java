package com.example.technician.mulikamwizi;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public class ExampleJobService extends JobService {
    private static final String TAG ="ExampleJobService";
    private  boolean jobCancelled=false;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG,"job started");
        doBackgroundWork(params);
        return true;
    }
    private void doBackgroundWork(final JobParameters params){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<10;i++){
                    Log.d(TAG,"run: "+i);
                    if (jobCancelled){
                        return;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG,"Job Finished ");
                jobFinished(params,false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG,"Job Cancelled");
        jobCancelled=true;
        return true;
    }
}
