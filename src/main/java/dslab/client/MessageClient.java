package dslab.client;

import java.io.InputStream;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;

import dslab.ComponentFactory;
import dslab.model.Mail;
import dslab.util.AESUtil;
import dslab.util.Config;
import dslab.util.IntegrityUtil;
import dslab.util.PrivateKeyUtil;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class MessageClient implements IMessageClient, Runnable {

    /**
     * Creates a new client instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MessageClient(String componentId, Config config, InputStream in, PrintStream out) {

    }

    @Override
    public void run() {
        verify("test");
    }

    @Override
    public void inbox() {

    }

    @Override
    public void delete(String id) {

    }

    @Override
    public void verify(String id) {
        Mail mail = new Mail();
        mail.setFrom("deep@thought.ze");
        mail.setTo(new HashSet<>(Arrays.asList("zaphod@univer.ze", "trillian@planet.earth")));
        mail.setSubject("my answer");
        mail.setData("i have thought about it and the answer is clearly 42");
        mail.setHash();
        System.out.println(mail);

        System.out.println(mail.getHash());
    }

    @Override
    public void msg(String to, String subject, String data) {
        AESUtil aesUtil = new AESUtil();
        String clientChallenge = generateClientChallenge();
        String secret = aesUtil.getSecret();
        String iv = aesUtil.getIv();
    }

    @Override
    public void shutdown() {

    }

    public static String generateClientChallenge() {
        byte[] clientChallenge = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(clientChallenge);
        return Base64.getEncoder().encodeToString(clientChallenge);
    }

    public static void main(String[] args) throws Exception {
        IMessageClient client = ComponentFactory.createMessageClient(args[0], System.in, System.out);
        client.run();
    }
}
