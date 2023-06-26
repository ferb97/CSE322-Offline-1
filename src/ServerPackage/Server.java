package ServerPackage;

import ObjectPackage.FileDescription;
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
    static List<FileDescription> fileDescriptionList;

    Server() {
        clientMap = new HashMap<>();
        activeClients = new ArrayList<>();
        fileDescriptionList = new ArrayList<>();
        fileDescriptionList.add(new FileDescription("file1.txt", "Piyal", "Private"));
        fileDescriptionList.add(new FileDescription("file2.txt", "Abrar", "Public"));
        fileDescriptionList.add(new FileDescription("file3.txt", "Mahmud", "Private"));
        fileDescriptionList.add(new FileDescription("file4.txt", "Mahmud", "Public"));
        fileDescriptionList.add(new FileDescription("file5.txt", "Piyal", "Public"));
        fileDescriptionList.add(new FileDescription("file6.txt", "Mahmud", "Public"));
        fileDescriptionList.add(new FileDescription("file7.txt", "Abrar", "Private"));
        fileDescriptionList.add(new FileDescription("file8.txt", "Piyal", "Public"));
        fileDescriptionList.add(new FileDescription("file9.txt", "Piyal", "Private"));
        fileDescriptionList.add(new FileDescription("file10.txt", "Abrar", "Private"));
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
