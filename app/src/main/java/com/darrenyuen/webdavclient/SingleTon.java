package com.darrenyuen.webdavclient;

/**
 * Create by yuan on 2021/3/14
 */

/**
 *
 * @param <T> target
 * @param <P> param
 */
public abstract class SingleTon<T, P> {

    public volatile T instance;

    protected abstract T create(P p);

    public T getInstance(P p) {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) instance = create(p);
            }
        }
        return instance;
    }

}
