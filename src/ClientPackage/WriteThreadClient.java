package ClientPackage;

import util.NetworkUtil;

import java.io.IOException;
import java.util.Scanner;

public class WriteThreadClient implements Runnable{
    private Thread thr;
    private NetworkUtil networkUtil;
    String name;

    public WriteThreadClient(NetworkUtil networkUtil, String name) {
        this.networkUtil = networkUtil;
        this.name = name;
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        try {
            Scanner input = new Scanner(System.in);
            while (true) {
                System.out.println("1 - Show All the Clients");
                System.out.println("2 - Show All Files of Mine");
                System.out.println("3 - Show ALl Public Files of Other Clients");
                System.out.println("7 - Exit");
                System.out.print("Enter an Option: ");
                int option = input.nextInt();
                if(option == 7){
                    networkUtil.write("Exiting");
                    networkUtil.closeConnection();
                    System.exit(0);
                }
                else if(option == 1){
                    networkUtil.write("List of Clients");
                    String clientListString = (String) networkUtil.read();
                    System.out.println(clientListString);
                }
                else if(option == 2){
                    networkUtil.write("List of All Files of Mine");
                    String clientListString = (String) networkUtil.read();
                    System.out.println(clientListString);
                }
                else if(option == 3){
                    networkUtil.write("List of All Public Files of Other Clients");
                    String clientListString = (String) networkUtil.read();
                    System.out.println(clientListString);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                networkUtil.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
