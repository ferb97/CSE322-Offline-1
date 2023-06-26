package ServerPackage;

import ObjectPackage.FileDescription;
import ObjectPackage.Message;
import util.NetworkUtil;

import java.io.*;
import java.util.Random;

public class ReadThreadServer implements Runnable{
    private Thread thr;
    private NetworkUtil networkUtil;
    private String clientName;

    public ReadThreadServer(String clientName, NetworkUtil networkUtil) {
        this.networkUtil = networkUtil;
        this.clientName = clientName;
        this.thr = new Thread(this);
        thr.start();
    }

    public void run() {
        try {
            while (true) {
               Object object = networkUtil.read();
               if(object instanceof String){
                   String str = (String) object;
                   System.out.println(clientName + ": " + str);
                   if(str.equalsIgnoreCase("Exiting")){
                       System.out.println(clientName + " is going offline");
                       Server.activeClients.remove(clientName);
                       break;
                   }
                   else if(str.equalsIgnoreCase("List of Clients")){
                       String clientListString = "";
                       for(String name: Server.clientMap.keySet()){
                           clientListString += name + " - ";
                           if(Server.activeClients.contains(name)){
                               clientListString += "Online\n";
                           }
                           else{
                               clientListString += "Offline\n";
                           }
                       }
                       System.out.println("Sending Client List to " + clientName);
                       networkUtil.write(clientListString);
                   }
                   else if(str.equalsIgnoreCase("List of All Files of Mine")){
                       String clientFilesString = "";
                       for(FileDescription fileDescription: Server.fileDescriptionList){
                           if(fileDescription.getClientName().equalsIgnoreCase(clientName)){
                               clientFilesString += fileDescription.getFileName() + " - " + fileDescription.getFileType() + "\n";
                           }
                       }
                       System.out.println("Sending All files List of " + clientName + " to " + clientName);
                       networkUtil.write(clientFilesString);
                   }
                   else if(str.equalsIgnoreCase("List of All Public Files of Other Clients")){
                       String clientFilesString = "";
                       for(FileDescription fileDescription: Server.fileDescriptionList){
                           if(!(fileDescription.getClientName().equalsIgnoreCase(clientName)) && fileDescription.getFileType().equalsIgnoreCase("Public")){
                               clientFilesString += fileDescription.getFileName() + " - " + fileDescription.getClientName() + "\n";
                           }
                       }
                       System.out.println("Sending All files List of Other Clients to " + clientName);
                       networkUtil.write(clientFilesString);
                   }
               }
               else if(object instanceof Message){
                   Message message = (Message) object;
                   if(message.getFunction().equalsIgnoreCase("Upload")){
                       System.out.println("Upload request from " + clientName);
                       System.out.println("File name: " + message.getText() + ", File Size: " + message.getFileSize() + ", File Type: " + message.getFileType());
                       Random random = new Random();
                       int chunkSize = random.nextInt(Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE + 1) + Server.MIN_CHUNK_SIZE;
                       Message message1 = new Message("You can start file transmission", "File Transmission Status");
                       message1.setChunkSize(chunkSize);
                       message1.setFileID(++(Server.fileId));
                       networkUtil.write(message1);
                       String fileName = message.getText();
                       int chunkNo = 0;
                       byte[] buffer = new byte[chunkSize];
                       int bytesRead;
                       ObjectInputStream ois = networkUtil.getOis();
                       String filepath = "src/ServerPackage/" + clientName + "/" + fileName;
                       BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filepath));
                       int totalBytesRead = 0;
                       while ((bytesRead = ois.read(buffer)) != -1){
                           int chunkReceived = 0;
                           bufferedOutputStream.write(buffer, 0, bytesRead);
                           bufferedOutputStream.flush();
                           chunkReceived += bytesRead;
                           while(ois.available() > 0) {
                               bytesRead = ois.read(buffer);
                               bufferedOutputStream.write(buffer, 0, bytesRead);
                               bufferedOutputStream.flush();
                               chunkReceived += bytesRead;
                           }
                           totalBytesRead += chunkReceived;
                           ++chunkNo;
                           System.out.println("ChunkNo: " + chunkNo + " received which is " + chunkReceived + " bytes");
                           networkUtil.write("ChunkNo: " + chunkNo + " received which is " + chunkReceived + " bytes");
                       }
                       String str1 = (String) networkUtil.read();
                       System.out.println(str1);
                       if(totalBytesRead == message.getFileSize()){
                           System.out.println("File Uploaded Successfully");
                           networkUtil.write("File Uploaded Successfully");
                       }
                       else{
                           System.out.println("File Upload Failed");
                           networkUtil.write("File Upload Failed");
                           File file1 = new File(filepath);
                           file1.delete();
                       }
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
