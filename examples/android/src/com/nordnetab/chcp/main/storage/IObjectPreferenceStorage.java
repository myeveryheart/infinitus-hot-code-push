package com.nordnetab.chcp.main.storage;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 保存和读取SharedPreferences的Interface.
 *
 * @see android.content.SharedPreferences
 */
public interface IObjectPreferenceStorage<T> {

    /**
     * 保存 shared preference
     *
     * @param object 保存对象
     * @return <code>true</code> if object is saved; <code>false</code> - otherwise
     */
    boolean storeInPreference(T object);

    /**
     * 读取 shared preference.
     *
     * @return 对象
     */
    T loadFromPreference();
}
