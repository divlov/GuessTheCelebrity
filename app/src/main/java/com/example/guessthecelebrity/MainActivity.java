package com.example.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView actorImage;
    ProgressBar mProgressBar;

    final String urlString="https://www.imdb.com/list/ls023242359/";
    ArrayList<String> actorsNamesList=new ArrayList<>();
    ArrayList<String> actorsFacesURLList=new ArrayList<>();
    String html="", correctName;
    int index;
    ArrayList<Integer> indexUsed=new ArrayList<>();

    URL url;
    Handler mHandler;
    HandlerThread handlerThread;
    Random r;
    Bitmap actorFace;
    Set<Integer> unique= new HashSet<>();

    final String TAG="guessthecelebritydebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actorImage=findViewById(R.id.imageView);
        mProgressBar=findViewById(R.id.progressBar);
        r=new Random();

        try {
            url=new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        startBackgroundThread();
        mProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mHandler.post(getContent);
        mHandler.post(loadData);
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"In onResume");
        super.onResume();
        if(handlerThread==null){
            startBackgroundThread();
        }

    }

    Runnable getContent=new Runnable() {
        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    Log.d(TAG, "Processing");
                    html += line + "\n";
                }
                Log.d(TAG, html);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable loadData=new Runnable() {
        @Override
        public void run() {
            String[] splitString=html.split("<div class=\"lister list detail sub-list\">");
            String content=splitString[1];
            Pattern pattern=Pattern.compile("<img alt=\"(.*?)\"");
            Matcher matcher=pattern.matcher(content);
            while(matcher.find()){
                actorsNamesList.add(matcher.group(1));
            }
            pattern=Pattern.compile("height=\"209\"\nsrc=\"(.*?)\"");
            matcher=pattern.matcher(content);
            while (matcher.find()){
                actorsFacesURLList.add(matcher.group(1));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView();
                    mProgressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
        }
    };

    private void updateView() {
//        for a Play Again option -->
//        if(indexUsed.size()==100){
//
//        }
        int temp=unique.size();
        while(temp==unique.size()) {
            index=r.nextInt(100);
            unique.add(index);
        }
        indexUsed.add(index);
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    url=new URL(actorsFacesURLList.get(index));
                    actorFace= BitmapFactory.decodeStream(url.openStream());
                    actorImage.post(new Runnable() {
                        @Override
                        public void run() {
                            actorImage.setImageBitmap(actorFace);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        int n=r.nextInt(5);
        while(n==0){
            n=r.nextInt(5);
        }
        int id=getResources().getIdentifier("option"+n,"id",getPackageName());
        Button button=findViewById(id);
        correctName=actorsNamesList.get(index);
        button.setText(correctName);
        int ran=0;
        for(int i=1;i<=4;i++){
            temp=unique.size();
            if(i==n) {
                continue;
            }
            id=getResources().getIdentifier("option"+i,"id",getPackageName());
            button=findViewById(id);
            while(temp==unique.size()){
                ran=r.nextInt(100);
                unique.add(ran);
            }
            button.setText(actorsNamesList.get(ran));
            unique.clear();
            unique.addAll(indexUsed);
        }
    }


    public void checkAnswer(View view) {
        Button selection=(Button)view;
        if(selection.getText()==correctName){
            Toast.makeText(this,"Correct Answer!",Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this,"Wrong Answer!",Toast.LENGTH_SHORT).show();
        updateView();
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        handlerThread=new HandlerThread("background thread");
        handlerThread.start();
        mHandler=new Handler(handlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread=null;
            mHandler=null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}