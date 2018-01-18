package com.cat.rxjavademo;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Handler mBgHandler = null  ;
    private Subscription mSubscr = null;

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

        HandlerThread backgroundThread = new HandlerThread("BackGroundThread", Process.THREAD_PRIORITY_BACKGROUND);
        backgroundThread.start();
        mBgHandler = new Handler(backgroundThread.getLooper());

        findViewById(R.id.BtnRxHandler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRunSchedulerExampleButtonClicked();
            }
        });

        findViewById(R.id.BtnCancelRx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSubscr != null){
                    if(!mSubscr.isUnsubscribed()){
                        mSubscr.unsubscribe();
                    }
                    mSubscr = null;
                }
                Log.d(TAG,"Cancel Done ");
            }
        });
    }

    /**
     * 整个流程就如同 取款机：
     * 先输入取款数目，（订阅,要钱的需求）
     * 然后取款机自动吐出对应数目的钱（观察输入信号)
     */
    void onRunSchedulerExampleButtonClicked() {
        if(mSubscr!=null) return ;
        mSubscr = generateObservable()
                .subscribeOn(HandlerScheduler.from(mBgHandler))//订阅在子线程(数据生产)  Run on a background thread
                .observeOn(AndroidSchedulers.mainThread())      //观察在主线程(数据消费) 也就是结果会在主线程中执行 Be notified on the main thread
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted() Subscriber Thread:" + Thread.currentThread().getName());  // Main
                        mSubscr = null;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override
                    public void onNext(String string) {
                        Log.d(TAG, "onNext(" + string + ") Subscriber Thread:" + Thread.currentThread().getName());     // 这将会在主线程中执行
                    }
                });
    }

    static Observable<String> generateObservable() {
        return Observable.defer(new Func0<Observable<String>>() {//定义一个方法，接口中的方法返回Observable<String>
            @Override
            public Observable<String> call() { // 任务:  花大部分时间生产数据
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));//睡眠5s  Do some long running operation
                    Log.d(TAG, "Observable call Thread:" + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    throw OnErrorThrowable.from(e);
                }
                return Observable.just("one", "two", "three", "four", "five");//发布5个字符串，子线程中
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mSubscr != null){
            if(!mSubscr.isUnsubscribed()){
                mSubscr.unsubscribe();
            }
            mSubscr = null;
        }
        mBgHandler.getLooper().quit();
    }
}
