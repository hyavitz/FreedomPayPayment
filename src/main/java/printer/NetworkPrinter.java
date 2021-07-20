package printer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class NetworkPrinter implements Printable {
   private OutputStream printer = null;
   private final String address;
   private final int port;

   public NetworkPrinter(String address, int port){
      this.address = address;
      this.port = port;
   }

   public void open(){
      try {
         Socket socket = new Socket(this.address, this.port);
         socket.setSoTimeout(1000);
         printer = socket.getOutputStream();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void write(byte[] command){
      try {
         printer.write(command);
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         close();
      }
   }

   public void close(){
      try {
         printer.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

}
