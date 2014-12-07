package com.abdennebi.photogift.utils;

public interface Callback<T> {

    public void success(T result);

    public void error(Exception e);

}
