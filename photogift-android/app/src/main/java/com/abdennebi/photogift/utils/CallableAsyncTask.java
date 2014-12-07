package com.abdennebi.photogift.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.Callable;


public class CallableAsyncTask<T> extends AsyncTask<Void,Double,T> {

    private static final String TAG = CallableAsyncTask.class.getName();

    public static <V> void invoke(Callable<V> call, Callback<V> callback){
        new CallableAsyncTask<V>(call, callback).execute();
    }

    private Callable<T> callable;

    private Callback<T> callback;
    
    private Exception exception;

    public CallableAsyncTask(Callable<T> callable, Callback<T> callback) {
        this.callable = callable;
        this.callback = callback;
    }

    @Override
    protected T doInBackground(Void... ts) {
        T result = null;
        try{
            result = callable.call();
        } catch (Exception e){
            Log.e(TAG, "Error invoking callable in AsyncTask callable: " + callable, e);
            exception = e;
        }
        return result;
    }

    @Override
    protected void onPostExecute(T r) {
    	if(exception != null){
    		callback.error(exception);
    	}
    	else {
    		callback.success(r);
    	}
    }
}

