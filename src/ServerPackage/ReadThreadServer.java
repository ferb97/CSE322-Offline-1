package ServerPackage;

import ObjectPackage.FileDescription;
import util.NetworkUtil;

import java.io.IOException;

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
