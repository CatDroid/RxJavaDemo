package com.cat.rxjavademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Observable.just(1,2,3).observeOn(Schedulers.io()).doOnNext(
                new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.d(TAG,"doOnNext @ " + Thread.currentThread().getName() + " integer is " + integer );
                    }
                }
        ).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Log.d(TAG, "OnNext @ " + Thread.currentThread().getName() + " integer is " + integer);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.e(TAG, "OnError @ " + Thread.currentThread().getName() + " throwable is " + throwable.getMessage());
            }
        }, new Action0() {
            @Override
            public void call() {
                Log.d(TAG, "OnComplete @ " + Thread.currentThread().getName()  );
            }
        });
    }
}
