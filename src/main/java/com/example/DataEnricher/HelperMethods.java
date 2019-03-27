package com.example.DataEnricher;

public class HelperMethods {

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    /**
     * This methods converts a byte array in the corresponding hex representation as a string.
     * @param bytes The byte array we want to translate
     * @return A String containing the hex representation of the byte array
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
