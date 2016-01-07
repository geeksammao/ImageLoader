package geeksammao.bingyan.net.imageloader.cache;

import java.security.MessageDigest;

/**
 * Created by Geeksammao on 1/6/16.
 */
public class MD5 {
    public static String hashKeyForDisk(String key) {
        String cacheKey;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(key.getBytes());
            cacheKey = byteToHexString(md.digest());
        } catch (Exception e) {
            e.printStackTrace();
            cacheKey = String.valueOf(key.hashCode());
        }

        return cacheKey;
    }

    private static String byteToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int aByte : bytes) {
            String str = Integer.toHexString(0xff & aByte);

            if (str.length() == 1) {
                hexString.append('0');
            }
            hexString.append(str);
        }

        return hexString.toString();
    }
}
