import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Opens my server.
 * Accepts clients on new threads.
 */
public class WebServer {

  static final FileManager fileM = new FileManager();

  /**
   * main method.
   */
  public static void main(String[] args) {
    if (args == null) {
      System.out.println("Invalid arguments");
      System.out.println("Server is not available");
      System.out.println("Terminating....");
      System.exit(1);
    } else if (args.length == 2) {
      fileM.setDirectoryPath(args[1]);
      int portNumber = Integer.parseInt(args[0]);

      try (ServerSocket ss = new ServerSocket(portNumber)) {
        System.out.println("\nServer is availble at port:" + portNumber);
        System.out.println("Server source file exists!");
        System.out.println("Detailed path: " + fileM.getDirectoryPath() + "\n");

        while (true) {
          Socket socket = ss.accept();
          System.out.println("Establishing a connection to client: " + socket.getPort());
          System.out.println();
          Client client = new Client(socket, fileM);
          new Thread(client).start();

        }
      } catch (IOException e) {
        System.out.println("Error at server-starting.");
        e.printStackTrace();
        System.exit(1);
      } catch (NumberFormatException n) {
        System.out.println("failer.");
        System.out.println(n);
        System.exit(1);
      }
    } else {
      System.out.println("invalid arguments.");
      System.out.println("Error at server-starting.");
      System.out.println("Server is terminating");
      System.exit(1);
    }
  }

}
