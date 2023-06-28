package ClientPackage;

import util.NetworkUtil;

import java.io.File;
import java.util.Scanner;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Client {

    public Client(String serverAddress, int serverPort) {
        try {
            System.out.print("Enter name of the client: ");
            Scanner scanner = new Scanner(System.in);
            String clientName = scanner.nextLine();
            NetworkUtil networkUtil = new NetworkUtil(serverAddress, serverPort);
            networkUtil.getSocket().setSoTimeout(30000);
            networkUtil.write(clientName);
            String str = (String) networkUtil.read();
            System.out.println(str);
            if(str.equalsIgnoreCase("Terminating connection with server")){
                System.exit(1);
            }
            else {
                String filepath = "src/ClientPackage/" + clientName + "Downloads";
                File directory = new File(filepath);
                if(!directory.exists()){
                    directory.mkdirs();
                    System.out.println("Download Directory Created for " + clientName);
                }
                else{
                    System.out.println("Download Directory Already Exists");
                }
                new WriteThreadClient(networkUtil, clientName);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String args[]) {
        String serverAddress = "127.0.0.1";
        int serverPort = 40000;
        Client client = new Client(serverAddress, serverPort);
    }
}
