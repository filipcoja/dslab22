package dslab.util;

import javax.crypto.Mac;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class IntegrityUtil {
    private Mac mac = null;

    public IntegrityUtil() {
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(Keys.readSecretKey(new File("keys/hmac.key")));
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            System.out.println("error loading secret key");
        }
    }

    public boolean checkHash(String payload, String hash) {
        return calculateHash(payload).equals(hash);
    }

    public String calculateHash(String data) {
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
    }
}
