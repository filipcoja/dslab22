package dslab.util;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {

    private Cipher encrypt;
    private Cipher decrypt;
    private String iv;
    private String secret;

    // https://www.baeldung.com/java-aes-encryption-decryption
    public AESUtil(String secret, String iv) {
        this.secret = secret;
        this.iv = iv;
        try {
            SecretKey key = new SecretKeySpec(secret.getBytes(), "AES");

            encrypt = Cipher.getInstance("AES/CTR/NoPadding");
            encrypt.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv.getBytes()));

            decrypt = Cipher.getInstance("AES/CTR/NoPadding");
            decrypt.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv.getBytes()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            System.out.println("error initializing aes cipher");
        }
    }

    public AESUtil() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey key = keyGenerator.generateKey();
            secret = Base64.getEncoder().encodeToString(key.getEncoded());


            byte[] ivB = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(ivB);
            iv = Base64.getEncoder().encodeToString(ivB);

            // create aes class
            // würde gerne this(secret, iv) machen, aber das müsste ich als erste Zeile schreiben, weil es Java sonst nicht erlaubt
            encrypt = Cipher.getInstance("AES/CTR/NoPadding");
            encrypt.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivB));

            decrypt = Cipher.getInstance("AES/CTR/NoPadding");
            decrypt.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivB));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            System.out.println("error initializing aes cipher");

        }
    }

    public String encryptBase64(String payload) {
        try {
            return Base64.getEncoder().encodeToString(encrypt.doFinal(payload.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
    }

    public String decryptBase64(String base64Encoded) {
        try {
            return new String(decrypt.doFinal(Base64.getDecoder().decode(base64Encoded)));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
    }

    public String getIv() {
        return iv;
    }

    public String getSecret() {
        return secret;
    }
}
