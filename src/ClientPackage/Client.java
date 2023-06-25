package ClientPackage;

import util.NetworkUtil;

import java.util.Scanner;

public class Client {
    public Client(String serverAddress, int serverPort) {
        try {
            System.out.print("Enter name of the client: ");
            Scanner scanner = new Scanner(System.in);
            String clientName = scanner.nextLine();
            NetworkUtil networkUtil = new NetworkUtil(serverAddress, serverPort);
            networkUtil.write(clientName);
            String str = (String) networkUtil.read();
            System.out.println(str);
            if(str.equalsIgnoreCase("Terminating connection with server")){
                System.exit(1);
            }
            else {
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
