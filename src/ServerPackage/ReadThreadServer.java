package ServerPackage;

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
