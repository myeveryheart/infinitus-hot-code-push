package com.nordnetab.hcp.main.storage;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 保存和读取manifest文件的接口
 */
public interface IObjectFileStorage<T> {

    /**
     * 保存到文件夹
     *
     * @param config 保存的对象
     * @param folder 文件夹路径
     * @return <code>true</code> 是; <code>false</code> - otherwise
     */
    boolean storeInFolder(T config, String folder);

    /**
     * 从文件夹读取
     *
     * @param folder 文件夹路径
     * @return 对象
     */
    T loadFromFolder(String folder);
}
