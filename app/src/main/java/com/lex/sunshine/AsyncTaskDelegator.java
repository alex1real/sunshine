package com.lex.sunshine;

/**
 * Created by Alex on 16/07/2016.
 */
                 //AsyncTaskDelegator<T> -> T = Result Type
public interface AsyncTaskDelegator<T> {

    public void updateAsyncResult(T result);

}
