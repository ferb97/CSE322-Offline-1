package ClientPackage;

import ObjectPackage.Message;
import util.NetworkUtil;

import java.io.*;
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
                System.out.println("4 - Upload a File");
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
                else if(option == 4){
                    String fileName = "";
                    File file;
                    String fileType = "";
                    input.nextLine();
                    while(true){
                        System.out.print("Enter File Name: ");
                        fileName = input.nextLine();
                        file = new File(fileName);
                        if(!file.exists()){
                            System.out.println(fileName + " does not exist in the current directory\nEnter an existing file name");
                        }
                        else{
                            break;
                        }
                    }
                    while(true){
                        System.out.print("Enter File Type(Public or Private): ");
                        fileType = input.nextLine();
                        if(fileType.equalsIgnoreCase("Public") || fileType.equalsIgnoreCase("Private")){
                            break;
                        }
                        else{
                            System.out.println("Enter Public or Private");
                        }
                    }
                    long fileSize = file.length();
                    Message message = new Message(fileName, "Upload");
                    message.setFileSize(fileSize);
                    message.setFileType(fileType);
                    System.out.println("File Size: " + fileSize + ", File Type: " + fileType);
                    networkUtil.write(message);
                    Object object = networkUtil.read();
                    Message message1 = (Message) object;
                    if(message1.getText().equalsIgnoreCase("You can start file transmission")){
                        System.out.println("Chunk Size: " + message1.getChunkSize() + ", File ID: " + message1.getFileID());
                        int chunkNo = 0;
                        byte[] buffer = new byte[message1.getChunkSize()];
                        int bytesRead;
                        ObjectOutputStream oos = networkUtil.getOos();
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileName));
                        while ((bytesRead = bufferedInputStream.read(buffer)) != -1){
                            oos.write(buffer, 0, bytesRead);
                            oos.flush();
                            ++chunkNo;
                            System.out.println("ChunkNo: " + chunkNo + " with " + bytesRead + " bytes is sent");
                            String str1 = (String) networkUtil.read();
                            System.out.println("Server: " + str1);
                        }
                        System.out.println("File Upload Finished");
                        oos.writeUnshared("File Upload Finished");
                        String str2 = (String) networkUtil.read();
                        System.out.println("Server: " + str2);
                    }
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
