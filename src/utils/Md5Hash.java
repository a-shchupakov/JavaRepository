package utils;

import java.security.MessageDigest;

public class Md5Hash {
    public static byte[] getMd5Hash(byte[] content){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (java.security.NoSuchAlgorithmException e){
            return new byte[0];
        }
        return md.digest(content);
    }
}
