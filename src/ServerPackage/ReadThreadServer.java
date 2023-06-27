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
                       String clientFilesString = "FileID - FileName - FileType\n";
                       for(int id: Server.fileDescriptionMap.keySet()){
                           FileDescription fileDescription = Server.fileDescriptionMap.get(id);
                           if(fileDescription.getClientName().equalsIgnoreCase(clientName)){
                               clientFilesString += id + " - " + fileDescription.getFileName() + " - " + fileDescription.getFileType() + "\n";
                           }
                       }
                       System.out.println("Sending All files List of " + clientName + " to " + clientName);
                       networkUtil.write(clientFilesString);
                       Object object1 = networkUtil.read();
                       Message message = (Message) object1;
                       if(message.getText().equalsIgnoreCase("No")){
                          networkUtil.write("Going back to menu");
                          continue;
                       }
                       networkUtil.write("You can download files from the above list");
                       int fid;
                       FileDescription fileDescription1;
                       while(true){
                           Object object2 = networkUtil.read();
                           Message message1 = (Message) object2;
                           fid = message1.getFileID();
                           fileDescription1 = Server.fileDescriptionMap.get(fid);
                           Message message2 = new Message("Wrong FID!", "FID status");
                           if(fileDescription1 != null && fileDescription1.getClientName().equalsIgnoreCase(clientName)){
                               message2.setText("Correct FID");
                               message2.setFileDescription(fileDescription1);
                               message2.setChunkSize(Server.MAX_CHUNK_SIZE);
                               networkUtil.write(message2);
                               break;
                           }
                           networkUtil.write(message2);
                       }
                       String str2 = (String) networkUtil.read();
                       System.out.println(str2);
                       if(str2.equalsIgnoreCase(fileDescription1.getFileName() + " already exists in the download directory of " + clientName)){
                           networkUtil.write("File Download Failed");
                           System.out.println("File Download Failed");
                           continue;
                       }
                       System.out.println("File Name: " + fileDescription1.getFileName() + ", Chunk Size: " + Server.MAX_CHUNK_SIZE);
                       int chunkNo = 0;
                       byte[] buffer = new byte[Server.MAX_CHUNK_SIZE];
                       int bytesRead;
                       ObjectOutputStream oos = networkUtil.getOos();
                       BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileDescription1.getFilePath()));
                       while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                           oos.write(buffer, 0, bytesRead);
                           oos.flush();
                           ++chunkNo;
                           System.out.println("ChunkNo: " + chunkNo + " with " + bytesRead + " bytes is sent");
                           String str1 = (String) networkUtil.read();
                       }
                       System.out.println("File Download Complete");
                       networkUtil.write("File Download Complete");
                   }
                   else if(str.equalsIgnoreCase("List of All Public Files of Other Clients")){
                       String clientFilesString = "FileID - FileName - FileType\n";
                       for(int id: Server.fileDescriptionMap.keySet()){
                           FileDescription fileDescription = Server.fileDescriptionMap.get(id);
                           if(!(fileDescription.getClientName().equalsIgnoreCase(clientName)) && fileDescription.getFileType().equalsIgnoreCase("Public")){
                               clientFilesString += id + " - " + fileDescription.getFileName() + " - " + fileDescription.getClientName() + "\n";
                           }
                       }
                       System.out.println("Sending All files List of Other Clients to " + clientName);
                       networkUtil.write(clientFilesString);
                       Object object1 = networkUtil.read();
                       Message message = (Message) object1;
                       if(message.getText().equalsIgnoreCase("No")){
                           networkUtil.write("Going back to menu");
                           continue;
                       }
                       networkUtil.write("You can download files from the above list");
                       int fid;
                       FileDescription fileDescription1;
                       while(true){
                           Object object2 = networkUtil.read();
                           Message message1 = (Message) object2;
                           fid = message1.getFileID();
                           fileDescription1 = Server.fileDescriptionMap.get(fid);
                           Message message2 = new Message("Wrong FID!", "FID status");
                           if(fileDescription1 != null && !fileDescription1.getClientName().equalsIgnoreCase(clientName) && fileDescription1.getFileType().equalsIgnoreCase("Public")){
                               message2.setText("Correct FID");
                               message2.setFileDescription(fileDescription1);
                               message2.setChunkSize(Server.MAX_CHUNK_SIZE);
                               networkUtil.write(message2);
                               break;
                           }
                           networkUtil.write(message2);
                       }
                       String str2 = (String) networkUtil.read();
                       System.out.println(str2);
                       if(str2.equalsIgnoreCase(fileDescription1.getFileName() + " already exists in the download directory of " + clientName)){
                           networkUtil.write("File Download Failed");
                           System.out.println("File Download Failed");
                           continue;
                       }
                       System.out.println("File Name: " + fileDescription1.getFileName() + ", Chunk Size: " + Server.MAX_CHUNK_SIZE);
                       int chunkNo = 0;
                       byte[] buffer = new byte[Server.MAX_CHUNK_SIZE];
                       int bytesRead;
                       ObjectOutputStream oos = networkUtil.getOos();
                       BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileDescription1.getFilePath()));
                       while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                           oos.write(buffer, 0, bytesRead);
                           oos.flush();
                           ++chunkNo;
                           System.out.println("ChunkNo: " + chunkNo + " with " + bytesRead + " bytes is sent");
                           String str1 = (String) networkUtil.read();
                       }
                       System.out.println("File Download Complete");
                       networkUtil.write("File Download Complete");
                   }
               }
               else if(object instanceof Message){
                   Message message = (Message) object;
                   if(message.getFunction().equalsIgnoreCase("Upload")){
                       System.out.println("Upload request from " + clientName);
                       System.out.println("File name: " + message.getText() + ", File Size: " + message.getFileSize() + ", File Type: " + message.getFileType());
                       String fileName = message.getText();
                       String fileType = message.getFileType();
                       String filepath = "src/ServerPackage/" + clientName + "/" + fileName;
                       File file1 = new File(filepath);
                       if(file1.exists()){
                           System.out.println(fileName + " already exists in the server directory of " + clientName);
                           Message message2 = new Message(fileName + " already exists in the server directory of " + clientName, "File Transmission Status");
                           networkUtil.write(message2);
                           continue;
                       }
                       Random random = new Random();
                       int chunkSize = random.nextInt(Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE + 1) + Server.MIN_CHUNK_SIZE;
                       Message message1 = new Message("You can start file transmission", "File Transmission Status");
                       message1.setChunkSize(chunkSize);
                       int fileid = ++(Server.fileId);
                       message1.setFileID(fileid);
                       networkUtil.write(message1);
                       int chunkNo = 0;
                       byte[] buffer = new byte[chunkSize];
                       int bytesRead;
                       ObjectInputStream ois = networkUtil.getOis();
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
                           FileDescription fileDescription = new FileDescription(fileName, clientName, fileType, filepath);
                           Server.fileDescriptionMap.put(fileid, fileDescription);
                       }
                       else{
                           System.out.println("File Upload Failed");
                           networkUtil.write("File Upload Failed");
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
