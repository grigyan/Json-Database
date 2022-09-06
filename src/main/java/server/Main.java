package server;

import com.google.gson.Gson;
import server.database.Database;
import server.database.DatabaseRequest;
import server.database.DatabaseResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Main {
    private static final int PORT = 23456;
    private static final String ADDRESS = "127.0.0.1";

    public static void main(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Database database = new Database();
        System.out.println("Server started!");
        try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))
        ) {
            while (!executor.isShutdown()) {
                Session session = new Session(server.accept(), database, executor);
                executor.execute(session);
                boolean terminated = executor.awaitTermination(500, TimeUnit.MILLISECONDS);

                if (terminated) {
                    System.out.println("The executor was successfully stopped");
                } else {
                    System.out.println("Timeout elapsed before termination");
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class Session extends Thread {
    private final Socket socket;
    private final Database database;
    private final ExecutorService executor;

    public Session(Socket socketForClient, Database db, ExecutorService executor) {
        database = db;
        socket = socketForClient;
        this.executor = executor;
    }

    @Override
    public void run() throws ArrayIndexOutOfBoundsException {
        try (DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            String dbRequestJson = input.readUTF();
            DatabaseRequest request = new Gson().fromJson(dbRequestJson, DatabaseRequest.class);
            DatabaseResponse response = database.sendRequest(request);

            if (response.isExit()) {
                response.setResponse("OK");
                output.writeUTF(response.getJsonString());
                socket.close();
                executor.shutdown();
            } else {
                output.writeUTF(response.getJsonString());
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
