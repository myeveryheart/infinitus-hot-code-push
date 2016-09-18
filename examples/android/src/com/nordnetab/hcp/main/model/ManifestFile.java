package com.nordnetab.hcp.main.model;

/**
 * Created by M on 16/9/9.
 * <p/>
 * mainifest
 */
public class ManifestFile {

    /**
     * 名字
     */
    public final String name;

    /**
     * HASH
     */
    public final String hash;

    /**
     * Class constructor
     *
     * @param name 名字
     * @param hash HASH
     */
    public ManifestFile(String name, String hash) {
        this.name = name;
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof ManifestFile)) {
            return super.equals(o);
        }

        ManifestFile comparedFile = (ManifestFile) o;

        return comparedFile.name.equals(name) && comparedFile.hash.equals(hash);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
