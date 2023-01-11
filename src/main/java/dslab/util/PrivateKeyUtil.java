package dslab.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class PrivateKeyUtil {
    private Cipher cipher;

    public PrivateKeyUtil(String keyPath) {
        cipher = null;
        try {
            PrivateKey privateKey = Keys.readPrivateKey(new File(keyPath));
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            System.out.println("error reading and initializing private key");
        }
    }

    public String decryptBase64(String base64Encoded) {
        try {
            return new String(cipher.doFinal(Base64.getDecoder().decode(base64Encoded)));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
           return null;
        }
    }
}
