package ServerPackage;

import ObjectPackage.FileDescription;
import ObjectPackage.UnreadMessages;
import util.NetworkUtil;

import java.io.File;
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
    static HashMap<Integer, FileDescription> fileDescriptionMap;
    static List<UnreadMessages> unreadMessagesList;
    static List<UnreadMessages> requestIdList;
    static int reqID = 0;
    static int fileId = 0;
    static long currentBufferSize = 0;
    static int MIN_CHUNK_SIZE = 200;
    static int MAX_CHUNK_SIZE = 300;
    static int MAX_BUFFER_SIZE = 7000;

    Server() {
        clientMap = new HashMap<>();
        activeClients = new ArrayList<>();
        fileDescriptionMap = new HashMap<>();
        unreadMessagesList = new ArrayList<>();
        requestIdList = new ArrayList<>();
        fileId = 0;

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
            String filepath = "src/ServerPackage/" + clientName;
            File directory = new File(filepath);

            if(!directory.exists()){
                directory.mkdirs();
                System.out.println("Directory Created for " + clientName);
            }
            else{
                System.out.println("Directory Already Exists");
            }

            System.out.println("Log in successful for " + clientName);
            networkUtil.write("Welcome: " + clientName);

            for(UnreadMessages unreadMessages: requestIdList){
                UnreadMessages unreadMessages1 = new UnreadMessages(unreadMessages.getRequestID());
                unreadMessages1.setFunction(unreadMessages.getFunction());
                unreadMessages1.setFrom(unreadMessages.getFrom());
                unreadMessages1.setText(unreadMessages.getText());
                unreadMessages1.setTo(clientName);
                unreadMessagesList.add(unreadMessages1);
            }

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
