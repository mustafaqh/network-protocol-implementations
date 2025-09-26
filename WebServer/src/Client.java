import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Runs threads to handle multi-client.
 */
public class Client implements Runnable {
  private FileManager fileMan;
  protected Socket socket;
  protected InputStream inputStream;
  protected OutputStream outputStream;
  // Client's file-number.
  protected int file = 1;

  /**
   * Consruct two program arguments.
   * 
   * @param socket  to establish .
   * @param fileMan deals with files on the server in public folder.
   */
  public Client(Socket socket, FileManager fileMan) {
    this.socket = socket;

    try {
      this.inputStream = socket.getInputStream();
      this.outputStream = socket.getOutputStream();
    } catch (Exception er) {
      er.printStackTrace();
    }
    this.fileMan = fileMan;

  }

  @Override
  public void run() {

    StringBuilder str = new StringBuilder();
    ArrayList<File> publicFiles = fileMan.getDirectoryFiles();
    try {
      // Reads bytes, sent to the server.
      byte[] bytes = new byte[inputStream.available()];
      inputStream.read(bytes);

      // Recieved bytes as chars will be appended to strings.
      for (byte byt : bytes) {
        str.append((char) byt);
      }

      String recieved = str.toString();
      String requestedFile;
      // Extracts information

      String[] clientRequest = recieved.split(" ");

      if (clientRequest.length == 1) {
        requestedFile = fileMan.getDirectoryPath() + "/" + "index.html";
      } else {
        requestedFile = fileMan.getDirectoryPath() + clientRequest[1];
        System.out.println("Client " + socket.getPort() + " requisting: " + clientRequest[0] + " " + requestedFile);
        System.out.println();
      }

      // Check the request.
      if (clientRequest[0].equals("GET")) {
        if (!(requestedFile.endsWith(".html")) && !(requestedFile.endsWith(".png"))) {
          handleRequestWithoutExtension(requestedFile, publicFiles);
          socket.close();
        } else {
          handleRequestWithExtension(requestedFile, publicFiles);
          socket.close();
        }
      } else if (clientRequest[0].equals("POST")) {
        sendResponse("200 OK", fileMan.getDirectoryPath() + "/upload.html", outputStream);
        managePosts(bytes);
        socket.close();
      } else {
        sendResponse("500 Internal Server Error", requestedFile, outputStream);
        socket.close();
      }

      inputStream.close();
      outputStream.close();
      socket.close();

    } catch (SocketException e) {
      System.out.println("Failer in request reading.");
      System.out.println("Error 500 has sent to client.");
      e.printStackTrace();
      sendResponse("500 Internal Server Error", null, outputStream);
      try {
        socket.close();
      } catch (IOException iex) {

        iex.printStackTrace();
      }
    } catch (IOException error) {
      System.out.println("Failer in request handling.");
      System.out.println("Error 500 has sent to client.");
      error.printStackTrace();
      sendResponse("500 Internal Server Error", null, outputStream);
      error.printStackTrace();
    }

  }

  /**
   * Takes care of post request
   * Saves into the file.
   * 
   * @param bytes array of data.
   */
  private void managePosts(byte[] bytes) {

    byte[] data;
    try {
      // reads data recievev from client.
      // used if image has a larg size.
      data = inputStream.readAllBytes();

      // adds data to one byte array.
      StringBuilder stBuilder = new StringBuilder();
      for (byte byt : bytes) {
        stBuilder.append((char) byt);
      }
      for (byte by : data) {
        stBuilder.append((char) by);
      }

      String[] splittedString = stBuilder.toString().split("\n");
      int x = 0;

      // Deals with png and denote the defualt name If the file has no name.
      String fileName = "file" + file + "from" + socket.getPort() + ".png";

      // Seek where the file data starts.
      for (String str : splittedString) {
        if (str.contains("filename=")) {
          fileName = str;
        }
        if (str.startsWith("Content-Type:")) {
          if (str.startsWith("Content-Type: multi")) {
            System.out.println();
          } else {
            x += 2;
            break;
          }
        }
        x++;
      }

      // Extracts the name of file.
      if (!(fileName.startsWith("file"))) {
        String[] s = fileName.split("filename=");
        fileName = s[1].substring(1, s[1].length() - 2);
      }

      // Creates a new file.
      File incomingFile = new File((fileMan.getUploadFolder()), fileName);
      FileOutputStream fos = new FileOutputStream(incomingFile);
      StringBuilder dataStr = new StringBuilder();

      while (x != (splittedString.length - 6)) { // Because the last four lines contains no needed data.
        if (x == splittedString.length - 6) {
          dataStr.append(splittedString[x]);
          break;
        }
        dataStr.append(splittedString[x] + "\n"); // Adds \n, because we have splitted the string on\n.
        x++;
      }

      // Converts string to char-array which in turns will be used as bytes in file.
      char[] ch = dataStr.toString().toCharArray();
      for (char c : ch) {
        fos.write((byte) c);
      }
      fos.flush();
      fos.close();
      file++;
      System.out.println("the specified file is uploded from client: " + socket.getPort());

    } catch (IOException e) {
      System.out.println("Failer when handling POST request");
      System.out.println("Check the message bellow.");
      System.out.println("Send Error 500 to client!");
      sendResponse("500 Internal Server Error", null, outputStream);
      e.printStackTrace();
    }
  }

  /**
   * Manages a response sent to a client.
   * 
   * @param responseStatus the status of response.
   * @param requestedFile  requested file.
   * @param outputStream   outputStream.
   */
  private void sendResponse(String responseStatus, String requestedFile, OutputStream outputStream) {
    if (responseStatus.equals("200 OK")) {
      try {
        Path file = Paths.get(requestedFile);
        byte[] fileContent;
        fileContent = Files.readAllBytes(file);
        outputStream.write(("HTTP/1.1 " + responseStatus + System.lineSeparator()).getBytes());
        outputStream.write(("content-type: " + Files.probeContentType(file)
            + System.lineSeparator()).getBytes());
        outputStream.write(("content-length: " + fileContent.length + System.lineSeparator()).getBytes());
        sendExtensions(fileContent);

      } catch (IOException error) {
        System.out.println("the file requested could not be sent");
        System.out.println("Check error-message!");
        System.out.println("Attempting to send Error-Type 500 to client");
        sendResponse("500 Internal Server Error", null, outputStream);
        error.printStackTrace();
      }

    } else if (responseStatus.equals("404 Not Found")) {
      try {
        Path file = Paths.get(fileMan.getDirectoryPath() + "/404.html");

        byte[] content;
        content = Files.readAllBytes(file);
        outputStream.write(("HTTP/1.1 " + responseStatus + System.lineSeparator()).getBytes());
        outputStream.write(("content-type: " + Files.probeContentType(file)
            + System.lineSeparator()).getBytes());
        outputStream.write(("content-length: " + content.length + System.lineSeparator()).getBytes());
        sendExtensions(content);

      } catch (IOException e) {
        System.out.println("the requested page is not availble"
            + "an error occured attemting to send a response");
        System.out.println("Checks error-message");
        System.out.println("Attempting to send Error-Type 500 to client");
        sendResponse("500 Internal Server Error", null, outputStream);

        e.printStackTrace();
      }
    } else if (responseStatus.equals("302 Found")) {
      try {
        Path file = Paths.get(fileMan.getDirectoryPath() + "/302.html");
        byte[] fileContent;
        fileContent = Files.readAllBytes(file);
        outputStream.write(("HTTP/1.1 " + responseStatus + System.lineSeparator()).getBytes());
        outputStream.write(("refresh : 5; url=" + requestedFile.substring(8) + System.lineSeparator()).getBytes());

        sendExtensions(fileContent);

      } catch (IOException er) {
        System.out.println("the requested page was found"
            + ", an error occured while redirecting client to right page.");
        System.out.println("Check error-message!");
        System.out.println("Attempting to resend response to client");
        sendResponse("200 OK", requestedFile.substring(8), outputStream);
        er.printStackTrace();
      }

    } else {
      try {
        Path file = Paths.get(fileMan.getDirectoryPath() + "/500.html");
        byte[] fileContent;
        fileContent = Files.readAllBytes(file);
        outputStream.write(("HTTP/1.1 " + responseStatus + System.lineSeparator()).getBytes());
        outputStream.write(("content-type: " + Files.probeContentType(file)
            + System.lineSeparator()).getBytes());
        outputStream.write(("content-length: " + fileContent.length + System.lineSeparator()).getBytes());
        sendExtensions(fileContent);

      } catch (IOException e) {
        System.out.println("Server-Error has occured");
        System.out.println("Check error-message");
        System.out.println("Attemting to resend error-message to client");
        sendResponse("500 Internal Server Error", null, outputStream);
        e.printStackTrace();
        System.out.println("Terminating the server...");
        System.exit(1);
      }
    }
  }

  /**
   * send the end of the file requested and.
   * 
   * @param content the file to send.
   */
  private void sendExtensions(byte[] content) {

    try {
      outputStream.write((System.lineSeparator()).getBytes());
      outputStream.write(content);
      outputStream.write((System.lineSeparator() + System.lineSeparator()).getBytes());
      outputStream.flush();
    } catch (IOException e) {
      System.out.println("Could not send the file.");
      System.out.println("Check Error-message!");
      System.out.println("Error message is bellow-listed");
      e.printStackTrace();
      System.out.println("sending error 500");
      sendResponse("500 Internal Server Error", null, outputStream);
    }

  }

  /**
   * Deals with the GET request if the extension is not html or png.
   * 
   * @param rf    requested file.
   * @param files all files availble on server.
   */
  private void handleRequestWithoutExtension(String reqFilPath, ArrayList<File> files) {
    File reFi = new File(reqFilPath);
    if (reFi.isDirectory()) {
      sendResponse("200 OK", reqFilPath + "/index.html", outputStream);
    } else {
      boolean found = false;
      for (File f : files) {
        if (f.getPath().equals(reqFilPath + ".html") || f.getPath().equals(reqFilPath + ".png")) {
          reqFilPath = f.getPath();
          found = true;
          break;
        }

      }
      if (found) {
        sendResponse("302 Found", reqFilPath, outputStream);
      } else {
        sendResponse("404 Not Found", reqFilPath, outputStream);
      }
    }
  }

  /**
   * Deals with a Get-request that ends with html or png.
   * 
   * @param requestedFile requested file.
   * @param files         all files availble on server.
   */
  private void handleRequestWithExtension(String requestedFile, ArrayList<File> directoryFiles) {
    // check if file exists in server's directory
    File file = new File(requestedFile);
    if (file.exists()) {
      System.out.println(requestedFile);
      sendResponse("200 OK", requestedFile, outputStream);
    } else {
      String[] extensions = { ".html", ".htm", "png" };

      // check if file exists with known extensions
      for (String extension : extensions) {
        String filePath = requestedFile + extension;
        for (File serverFile : directoryFiles) {
          if (serverFile.getPath().equals(filePath)) {
            System.out.println(filePath);
            sendResponse("200 OK", filePath, outputStream);
            return;
          }
        }
      }
      // file not found
      sendResponse("404 Not Found", requestedFile, outputStream);
    }
  }

}