package dslab.util;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESUtil {

    private Cipher encrypt;
    private Cipher decrypt;

    // https://www.baeldung.com/java-aes-encryption-decryption
    public AESUtil(String secret, String iv) {
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
}
