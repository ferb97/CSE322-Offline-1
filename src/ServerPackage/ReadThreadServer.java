package ServerPackage;

import ObjectPackage.FileDescription;
import ObjectPackage.Message;
import ObjectPackage.UnreadMessages;
import util.NetworkUtil;

import java.io.*;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

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
        boolean connectionClosed = false;
        try {
            while (true) {
                System.out.println("\n\t\t\t------------------------\n");
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

                       if(clientFilesString.equalsIgnoreCase("FileID - FileName - FileType\n")){
                           continue;
                       }

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

                       if(clientFilesString.equalsIgnoreCase("FileID - FileName - FileType\n")){
                           continue;
                       }

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

                   else if (str.equalsIgnoreCase("Requesting a File")) {
                       ++Server.reqID;
                       UnreadMessages unreadMessages2 = new UnreadMessages(Server.reqID);
                       networkUtil.write(unreadMessages2);

                       Object object1 = networkUtil.read();
                       UnreadMessages unreadMessages = (UnreadMessages) object1;
                       Server.requestIdList.add(unreadMessages);

                       for (String clientName1 : Server.clientMap.keySet()) {
                           if (!clientName1.equalsIgnoreCase(unreadMessages.getFrom())) {
                               UnreadMessages unreadMessages1 = new UnreadMessages(unreadMessages.getRequestID());
                               unreadMessages1.setFrom(unreadMessages.getFrom());
                               unreadMessages1.setTo(clientName1);
                               unreadMessages1.setText(unreadMessages.getText());
                               unreadMessages1.setFunction(unreadMessages.getFunction());
                               Server.unreadMessagesList.add(unreadMessages1);
                           }
                       }

                       networkUtil.write("File Request added to Unread Messages of other clients");
                       System.out.println("File Request added to Unread Messages of other clients");
                   }

                   else if(str.equalsIgnoreCase("Show Unread Messages")){
                       String unreadMessagesString = "";
                       int messageNo = 0;

                       for(UnreadMessages unreadMessages: Server.unreadMessagesList){
                           if(unreadMessages.getTo().equalsIgnoreCase(clientName)){
                               ++messageNo;
                               unreadMessagesString += "\nMessage No: " + messageNo;
                               unreadMessagesString += "\nFrom: " + unreadMessages.getFrom();
                               unreadMessagesString += "\n" + unreadMessages.getFunction() + " with Request ID: " + unreadMessages.getRequestID();
                               unreadMessagesString += "\n" + unreadMessages.getText() + "\n\n";
                           }
                       }

                       networkUtil.write(unreadMessagesString);
                       System.out.println("Showing Unread Messages to " + clientName);

                       for(int i = 0; i < Server.unreadMessagesList.size(); i++){
                           if(Server.unreadMessagesList.get(i).getTo().equalsIgnoreCase(clientName)){
                               Server.unreadMessagesList.remove(i);
                               i--;
                           }
                       }

                       System.out.println("All unread messages of " + clientName + " have been removed");
                   }
               }

               else if(object instanceof Message){
                   Message message = (Message) object;

                   if(message.getFunction().equalsIgnoreCase("Upload")) {
                       System.out.println("Upload request from " + clientName);
                       System.out.println("File name: " + message.getText() + ", File Size: " + message.getFileSize() + ", File Type: " + message.getFileType());
                       String fileName = message.getText();
                       String fileType = message.getFileType();
                       String filepath = "src/ServerPackage/" + clientName + "/" + fileName;
                       File file1 = new File(filepath);

                       if (Server.currentBufferSize + message.getFileSize() > Server.MAX_BUFFER_SIZE) {
                           Message message2 = new Message("The combined size overflows the maximum buffer size", "File Transmission Status");
                           System.out.println("File upload Failed");
                           networkUtil.write(message2);
                           continue;
                       }

                       if (file1.exists()) {
                           System.out.println("Replacing the old version of " + fileName + " with the new version");
                           Iterator<Map.Entry<Integer, FileDescription>> iterator = Server.fileDescriptionMap.entrySet().iterator();
                           while (iterator.hasNext()) {
                               Map.Entry<Integer, FileDescription> entry = iterator.next();
                               FileDescription fileDescription1 = entry.getValue();
                               if (fileDescription1.getFilePath().equalsIgnoreCase(filepath)) {
                                   iterator.remove();
                               }
                           }
                       }

                       Random random = new Random();
                       int chunkSize = random.nextInt(Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE + 1) + Server.MIN_CHUNK_SIZE;
                       Message message1 = new Message("You can start file transmission", "File Transmission Status");
                       message1.setChunkSize(chunkSize);

                       int fileid = ++(Server.fileId);
                       message1.setFileID(fileid);
                       networkUtil.write(message1);

                       int chunkNo = 0;
                       byte[] buffer = new byte[message1.getChunkSize()];
                       int bytesRead;
                       ObjectInputStream ois = networkUtil.getOis();
                       BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filepath));
                       int totalBytesRead = 0;
                       Server.currentBufferSize += message1.getChunkSize();

                       while ((bytesRead = ois.read(buffer)) != -1) {
                           try {
                               int chunkReceived = 0;
                               bufferedOutputStream.write(buffer, 0, bytesRead);
                               bufferedOutputStream.flush();
                               chunkReceived += bytesRead;

                               while (ois.available() > 0) {
                                   bytesRead = ois.read(buffer);
                                   bufferedOutputStream.write(buffer, 0, bytesRead);
                                   bufferedOutputStream.flush();
                                   chunkReceived += bytesRead;
                               }

                               totalBytesRead += chunkReceived;
                               ++chunkNo;
                               //sleep(4000);

                               System.out.println("ChunkNo: " + chunkNo + " received which is " + chunkReceived + " bytes");
                               networkUtil.write("ChunkNo: " + chunkNo + " received which is " + chunkReceived + " bytes");
                           }
                           catch(SocketException e){
                               System.out.println(clientName + " has exited");
                               Server.activeClients.remove(clientName);
                               connectionClosed = true;
                               bufferedOutputStream.close();
                               file1.delete();
                               break;
                           }
                       }

                       //sleep(5000);
                       if(connectionClosed)
                           break;
                       Server.currentBufferSize -= message1.getChunkSize();
                       bufferedOutputStream.close();
                       String str1 = (String) networkUtil.read();
                       System.out.println(str1);

                       if(str1.equalsIgnoreCase("Timeout: Server did not acknowledge chunk in 30 seconds")){
                           System.out.println("File Upload Failed");
                           networkUtil.write("File Upload Failed");
                           file1.delete();
                           continue;
                       }

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

               else if(object instanceof UnreadMessages) {
                   UnreadMessages unreadMessages = (UnreadMessages) object;

                   if(unreadMessages.getFunction().equalsIgnoreCase("Uploading a Requested File")){
                       int reqId = unreadMessages.getRequestID();
                       String requestFrom = "";
                       boolean found = false;

                       for(UnreadMessages unreadMessages2: Server.requestIdList){
                           if(reqId == unreadMessages2.getRequestID()){
                               requestFrom = unreadMessages2.getFrom();
                               found = true;
                               break;
                           }
                       }

                       if(!found){
                           System.out.println("Invalid Request ID. Upload not possible");
                           networkUtil.write("Invalid Request ID. Upload not possible");
                           continue;
                       }

                       if(requestFrom.equalsIgnoreCase(clientName)){
                           System.out.println("The Requesting Person is Trying to Upload the File. Upload not possible");
                           networkUtil.write("The Requesting Person is Trying to Upload the File. Upload not possible");
                           continue;
                       }

                       networkUtil.write("Send File Name and File Size");
                       Object object1 = networkUtil.read();
                       Message message = (Message) object1;

                       System.out.println("File name: " + message.getText() + ", File Size: " + message.getFileSize() + ", File Type: " + message.getFileType());
                       String fileName = message.getText();
                       String fileType = message.getFileType();
                       String filepath = "src/ServerPackage/" + clientName + "/" + fileName;
                       File file1 = new File(filepath);

                       if(Server.currentBufferSize + message.getFileSize() > Server.MAX_BUFFER_SIZE){
                           Message message2 = new Message("The combined size overflows the maximum buffer size", "File Transmission Status");
                           System.out.println("File upload Failed");
                           networkUtil.write(message2);
                           continue;
                       }

                       if(file1.exists()){
                           System.out.println("Replacing the old version of " + fileName + " with the new version");
                           Iterator<Map.Entry<Integer, FileDescription> > iterator = Server.fileDescriptionMap.entrySet().iterator();
                           while (iterator.hasNext()) {
                               Map.Entry<Integer, FileDescription> entry = iterator.next();
                               FileDescription fileDescription1 = entry.getValue();
                               if(fileDescription1.getFilePath().equalsIgnoreCase(filepath)){
                                   iterator.remove();
                               }
                           }
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
                       Server.currentBufferSize += message1.getChunkSize();

                       while ((bytesRead = ois.read(buffer)) != -1) {
                           try {
                               int chunkReceived = 0;
                               bufferedOutputStream.write(buffer, 0, bytesRead);
                               bufferedOutputStream.flush();
                               chunkReceived += bytesRead;

                               while (ois.available() > 0) {
                                   bytesRead = ois.read(buffer);
                                   bufferedOutputStream.write(buffer, 0, bytesRead);
                                   bufferedOutputStream.flush();
                                   chunkReceived += bytesRead;
                               }

                               totalBytesRead += chunkReceived;
                               ++chunkNo;
                               //sleep(4000);

                               System.out.println("ChunkNo: " + chunkNo + " received which is " + chunkReceived + " bytes");
                               networkUtil.write("ChunkNo: " + chunkNo + " received which is " + chunkReceived + " bytes");
                           }
                           catch(SocketException e){
                               System.out.println(clientName + " has exited");
                               Server.activeClients.remove(clientName);
                               connectionClosed = true;
                               bufferedOutputStream.close();
                               file1.delete();
                               break;
                           }
                       }

                       //sleep(5000);
                       if(connectionClosed)
                           break;
                       Server.currentBufferSize -= message1.getChunkSize();
                       bufferedOutputStream.close();
                       String str1 = (String) networkUtil.read();
                       System.out.println(str1);

                       if(str1.equalsIgnoreCase("Timeout: Server did not acknowledge chunk in 30 seconds")){
                           System.out.println("File Upload Failed");
                           networkUtil.write("File Upload Failed");
                           file1.delete();
                           continue;
                       }

                       if(totalBytesRead == message.getFileSize()){
                           System.out.println("File Uploaded Successfully");
                           networkUtil.write("File Uploaded Successfully");

                           FileDescription fileDescription = new FileDescription(fileName, clientName, fileType, filepath);
                           Server.fileDescriptionMap.put(fileid, fileDescription);

                           UnreadMessages unreadMessages1 = new UnreadMessages(reqId);
                           unreadMessages1.setFunction("Uploading a Requested File");
                           unreadMessages1.setFrom(clientName);
                           unreadMessages1.setTo(requestFrom);
                           String str2 = "FileID: " + fileid + "\nFile Name: " + fileName;
                           str2 += "\nYou can Download the Requested File from the Public Files of " + clientName;
                           unreadMessages1.setText(str2);

                           Server.unreadMessagesList.add(unreadMessages1);
                           System.out.println("Notification of uploading the requested file from " + clientName + " has been sent to " + requestFrom);
                       }

                       else{
                           System.out.println("File Upload Failed");
                           networkUtil.write("File Upload Failed");
                           file1.delete();
                       }
                   }
               }
            }
        } catch (SocketException e) {
            System.out.println(clientName + " has exited");
            Server.activeClients.remove(clientName);
        }
        catch (Exception e) {
            System.out.println(e);
        }finally {
            try {
                if(!connectionClosed) {
                    networkUtil.closeConnection();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
