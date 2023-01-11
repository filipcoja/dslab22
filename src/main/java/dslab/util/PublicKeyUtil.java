package dslab.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;

public class PublicKeyUtil {
    private Cipher cipher;

    public PublicKeyUtil(String keyPath) {
        cipher = null;
        try {
            PublicKey publicKey = Keys.readPublicKey(new File(keyPath));
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            System.out.println("error reading and initializing public key");
        }
    }

    public String encryptBase64(String payload) {
        try {
            return Base64.getEncoder().encodeToString(cipher.doFinal(payload.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
           return null;
        }
    }
}
