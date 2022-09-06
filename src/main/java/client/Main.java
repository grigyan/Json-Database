package client;

import com.beust.jcommander.JCommander;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Main {
    private static final int SERVER_PORT = 23456;
    private static final String SERVER_ADDRESS = "127.0.0.1";

    public static void main(String[] args) {
        System.out.println("Client started!");
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {
            ClientRequest request = new ClientRequest();
            JCommander.newBuilder().addObject(request).build().parse(args);

            String requestJson = request.getRequestJson();
            System.out.printf("Sent: %s%n", requestJson);
            output.writeUTF(requestJson);

            String serverResponse = input.readUTF();
            System.out.printf("Received: %s%n", serverResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}