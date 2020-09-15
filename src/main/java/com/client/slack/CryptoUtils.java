package com.client.slack;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {



    // Password derived AES 256 bits secret key
    public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // iterationCount = 65536
        // keyLength = 256
        KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret;

    }

    
    
}