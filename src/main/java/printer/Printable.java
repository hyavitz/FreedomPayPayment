package printer;

public interface Printable {
   void open();

   void write(byte[] command);

   void close();
}
