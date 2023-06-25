package ServerPackage;

import util.NetworkUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    static HashMap<String, NetworkUtil> clientMap;
    static List<String> activeClients;

    Server() {
        clientMap = new HashMap<>();
        activeClients = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(40000);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                serve(clientSocket);
            }
        } catch (Exception e) {
            System.out.println("Server starts:" + e);
        }
    }

    public void serve(Socket clientSocket) throws IOException, ClassNotFoundException {
        NetworkUtil networkUtil = new NetworkUtil(clientSocket);
        String clientName = (String) networkUtil.read();
        if(clientMap.get(clientName) == null){
            clientMap.put(clientName, networkUtil);
            activeClients.add(clientName);
            System.out.println("Log in successful for " + clientName);
            networkUtil.write("Welcome: " + clientName);
            new ReadThreadServer(clientName, networkUtil);
        }
        else if(activeClients.contains(clientName)){
            System.out.println(clientName + " is already logged in");
            networkUtil.write("Terminating connection with server");
        }
        else{
            activeClients.add(clientName);
            System.out.println("Log in successful for " + clientName);
            networkUtil.write("Welcome Back: " + clientName);
            new ReadThreadServer(clientName, networkUtil);
        }
    }

    public static void main(String args[]) {
        Server server = new Server();
    }
}
