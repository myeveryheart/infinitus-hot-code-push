package com.nordnetab.hcp.main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 两个manifest文件的差异
 *
 * @see ManifestFile
 */
public class ManifestDiff {

    private List<ManifestFile> deleted;
    private List<ManifestFile> changed;
    private List<ManifestFile> added;

    /**
     * Class constructor
     */
    public ManifestDiff() {
        added = new ArrayList<ManifestFile>();
        changed = new ArrayList<ManifestFile>();
        deleted = new ArrayList<ManifestFile>();
    }

    /**
     * 获取删除文件
     *
     * @return 删除文件
     */
    public List<ManifestFile> deletedFiles() {
        return deleted;
    }

    /**
     * 获取修改文件
     *
     * @return 修改文件
     */
    public List<ManifestFile> changedFiles() {
        return changed;
    }

    /**
     * 获取新增文件
     *
     * @return 新增文件
     */
    public List<ManifestFile> addedFiles() {
        return added;
    }

    /**
     * 两个manifest是否一样
     *
     * @return <code>true</code> 是; <code>false</code> - otherwise
     */
    public boolean isEmpty() {
        return added.isEmpty() && changed.isEmpty() && deleted.isEmpty();
    }

    /**
     * 获取新增的和修改的文件
     *
     * @return 新增的和修改的文件
     */
    public List<ManifestFile> getUpdateFiles() {
        List<ManifestFile> updateList = new ArrayList<ManifestFile>();
        updateList.addAll(added);
        updateList.addAll(changed);

        return updateList;
    }
}
