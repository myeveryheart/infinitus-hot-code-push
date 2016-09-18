package com.nordnetab.chcp.main.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 计算 MD5 hash 工具类
 *
 * @see MessageDigest
 */
public class MD5 {

    private MessageDigest digest;

    /**
     * Class constructor.
     */
    public MD5() {
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write bytes, based on which we will calculate hash later on.
     *
     * @param bytes  bytes
     * @param length number of bytes to take
     */
    public void write(byte[] bytes, int length) {
        if (digest == null) {
            return;
        }

        digest.update(bytes, 0, length);
    }

    /**
     * Calculate hash based on the received bytes.
     *
     * @return md5 hash string
     */
    public String calculateHash() {
        if (digest == null) {
            return "";
        }

        byte[] md5sum = digest.digest();
        BigInteger bigInt = new BigInteger(1, md5sum);
        String output = bigInt.toString(16);

        // Fill to 32 chars
        return String.format("%32s", output).replace(' ', '0');
    }
}