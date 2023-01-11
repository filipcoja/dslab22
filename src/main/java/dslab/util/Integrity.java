package dslab.util;

import javax.crypto.Mac;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Integrity {
    private Mac mac = null;

    public Integrity() {
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(Keys.readSecretKey(new File("keys/hmac.key")));
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            System.out.println("error loading secret key");
        }
    }

    public boolean checkHash(String payload, String hash) {
        String computedHash = Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes()));
        return computedHash.equals(hash);
    }
}
