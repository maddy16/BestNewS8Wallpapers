package com.sizzling.apps.bests8wallpapers;

import android.app.Application;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by ahmed on 25/06/2017.
 */

public class App extends Application {
    InterstitialAd mInterstitialAd;
    static App instance;
    boolean testingMode = true;
    int countIntAd = 0;
    void loadAds(){
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-1474727696191193~8883344303"); // App Id admob
        mInterstitialAd = new InterstitialAd(this);
        if(testingMode){
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); // Test Ad Id
        } else{
            mInterstitialAd.setAdUnitId("ca-app-pub-1474727696191193/4603342032");
        }



        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
//                requestNewInterstitial();
            }
        });
        requestNewInterstitial();
    }
    public void requestNewInterstitial() {
        AdRequest adRequest = null;
        if(testingMode){
            adRequest= new AdRequest.Builder()
                    .addTestDevice("55757F6B6D6116FAC42122EC92E5A58C")
                    .build();
        } else {
            adRequest = new AdRequest.Builder()
                    .build();
        }
        mInterstitialAd.loadAd(adRequest);
    }
    boolean shouldShowAd(){
        boolean show=false;
        if(countIntAd==0)
            show=true;
        countIntAd++;
        if(countIntAd==2)
            countIntAd=0;
        return show;
    }
    public InterstitialAd getFullScreenAd(){
        return mInterstitialAd;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
//        Toast.makeText(this, "Application Created", Toast.LENGTH_SHORT).show();
    }
}
