import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class TFTPServer {
  public static final int TFTPPORT = 4970;
  public static final int BUFSIZE = 516;
  public static final String READDIR = "../read/";
  public static final String WRITEDIR = "../read/";
  // OP codes
  public static final int OP_RRQ = 1;
  public static final int OP_WRQ = 2;
  public static final int OP_DAT = 3;
  public static final int OP_ACK = 4;
  public static final int OP_Err = 5;

  /**
   * main program run.
   */
  public static void main(String[] args) {
    if (args.length > 0) {
      System.err.printf("usage: java %s\n", TFTPServer.class.getCanonicalName());
      System.exit(1);
    }
    // Starting the server
    try {
      TFTPServer server = new TFTPServer();
      server.start();
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  private void start() throws SocketException {
    byte[] buf = new byte[BUFSIZE];

    // Create socket
    DatagramSocket socket = new DatagramSocket(null);

    // Create local bind point
    SocketAddress localBindPoint = new InetSocketAddress(TFTPPORT);
    socket.bind(localBindPoint);

    System.out.printf("Listening at port %d for new requests\n", TFTPPORT);

    // Loop to handle client requests
    while (true) {

      final InetSocketAddress clientAddress = receiveFrom(socket, buf);

      // If clientAddress is null, an error occurred in receiveFrom()
      if (clientAddress == null) {
        continue;
      }
      final StringBuffer requestedFile = new StringBuffer();
      final int reqtype = parseRq(buf, requestedFile);

      new Thread() {
        public void run() {
          try {
            DatagramSocket sendSocket = new DatagramSocket(0);

            // Connect to client
            sendSocket.connect(clientAddress);

            System.out.printf("%s request for %s from %s using port %d\n",
                (reqtype == OP_RRQ) ? "Read" : "Write",
                clientAddress.getAddress(), clientAddress.getHostName(), clientAddress.getPort());

            // Read request
            if (reqtype == OP_RRQ) {
              requestedFile.insert(0, READDIR);
              handleRq(sendSocket, requestedFile.toString(), OP_RRQ);
            } else if (reqtype == OP_WRQ) { // Write request
              requestedFile.insert(0, WRITEDIR);
              handleRq(sendSocket, requestedFile.toString(), OP_WRQ);
            } else {
              handleRq(sendSocket, requestedFile.toString(), OP_Err);
            }

            sendSocket.close();
          } catch (SocketException e) {
            e.printStackTrace();
          }
        }
      }.start();
    }
  }

  /**
   * Reads the first block of data, i.e., the request for an action (read or
   * write).
   * 
   * @param socket (socket to read from)
   * @param buf    (where to store the read data)
   * @return socketAddress (the socket address of the client)
   */
  private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) {

    DatagramPacket packet = new DatagramPacket(buf, BUFSIZE);
    try {
      socket.receive(packet);
    } catch (IOException e) {
      e.printStackTrace();
    }
    InetSocketAddress socketAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
    return socketAddress;
  }

  /**
   * Parses the request in buf to retrieve the type of request and requestedFile.
   * 
   * @param buf           (received request).
   * @param requestedFile (name of file to read/write).
   * @return opcode (request type: RRQ or WRQ).
   */
  private int parseRq(byte[] buf, StringBuffer requestedFile) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(buf);

    int opcode = (int) byteBuffer.getShort();

    for (int i = 2; i < BUFSIZE; i++) {
      if (buf[i] == 0) {
        break;
      }
      requestedFile.append((char) buf[i]);
    }

    return opcode;
  }

  /**
   * Handles RRQ and WRQ requests.
   * 
   * @param sendSocket    (socket used to send/receive packets)
   * @param requestedFile (name of file to read/write)
   * @param opcode        (RRQ or WRQ)
   */
  private void handleRq(DatagramSocket sendSocket, String requestedFile, int opcode) {
    if (opcode == OP_RRQ) {
      // See "TFTP Formats" in TFTP specification for the DATA and ACK packet contents
      send_data_receive_ack(sendSocket, requestedFile);
    } else if (opcode == OP_WRQ) {
      receive_data_send_ack(sendSocket, requestedFile);
    } else {
      System.err.println("Invalid request. Sending an error packet.");
      try {
        sendSocket.send(send_Err(4));
      } catch (IOException e) {

        e.printStackTrace();
      }
      return;
    }
  }

  /**
   * sends the file asked for.
   */
  private boolean send_data_receive_ack(DatagramSocket sendSocket, String requstedFile) {

    int byteIndex = 0;
    File file = new File(requstedFile);
    byte[] data = new byte[BUFSIZE];
    ByteBuffer sendBuffer = ByteBuffer.wrap(data);
    byte[] receiveByte = new byte[BUFSIZE];
    ByteBuffer receiveBuffer = ByteBuffer.wrap(receiveByte);

    try {
      // checks wether the file exists, if not send error.
      if (file.exists()) {
        // Checks if the file can be accessed, and raises an error if it cannot be.
        if (!(file.canRead())) {
          System.out.println("The client asked for a file that require permissions!.");
          sendSocket.send(send_Err(2));
          return false;
        }

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        int block;

        // Divides the file into blocks of 512 bytes to determine the number of blocks
        // to send.
        if ((fileBytes.length % 512) == 0) {
          block = (fileBytes.length / 512);
        } else {
          block = (fileBytes.length / 512) + 1;
        }

        for (int blockNumber = 1; blockNumber <= block; blockNumber++) {
          // opcode data and the block number to send.
          sendBuffer.putShort((short) OP_DAT);
          sendBuffer.putShort((short) (blockNumber));
          // gets the right bytrs to be sended to client in the block.
          for (int i = 0; i < 512; i++) {
            if (byteIndex == fileBytes.length) {
              break;
            }
            data[i + 4] = fileBytes[byteIndex];
            byteIndex++;
          }

          // Attempts to send the data up to 5 times.
          // generates an error, if no acknowledgment is received.
          int retryAttempts = 0;
          while (retryAttempts != 5) {
            if (blockNumber == block) {
              sendSocket.send(new DatagramPacket(data, (fileBytes.length % 512) + 4));
            } else {
              sendSocket.send(new DatagramPacket(data, BUFSIZE));
            }
            sendSocket.setSoTimeout(10);

            try {
              sendSocket.receive(new DatagramPacket(receiveByte, receiveByte.length));
            } catch (SocketTimeoutException e) {
              retryAttempts++;
              continue;
            }

            // Verifies the ack received from the client.
            // sends an error if it is not correct.
            if ((int) receiveBuffer.getShort() == OP_ACK) {
              if ((int) receiveBuffer.getShort() != blockNumber) {
                retryAttempts++;
              } else {
                break;
              }
            } else {
              sendSocket.send(send_Err(0));
              return false;
            }
          }
          receiveBuffer.clear();
          sendBuffer.clear();
        }
        return true;
      } else {
        sendSocket.send(send_Err(1));
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Receives data from the client and writes it to a file.
   *
   * @param sendSocket    the socket to send data to the client.
   * @param requestedFile the file to write the received data to.
   * @return
   */
  private boolean receive_data_send_ack(DatagramSocket sendSocket, String requestedFile) {
    final int BUFFER_SIZE_ACK = 4;

    byte[] ackBuffer = new byte[BUFFER_SIZE_ACK];
    ByteBuffer ack = ByteBuffer.wrap(ackBuffer);
    int block = 0;
    byte[] dataBuffer = new byte[BUFSIZE];
    ByteBuffer data = ByteBuffer.wrap(dataBuffer);
    DatagramPacket packet = new DatagramPacket(dataBuffer, BUFSIZE);
    File file = new File(requestedFile);

    if (!(file.exists())) {
      try {
        FileOutputStream fos = new FileOutputStream(file);
        // First acknowledgement packet to send.
        ack.putShort((short) OP_ACK);
        ack.putShort((short) block);

        sendSocket.send(new DatagramPacket(ackBuffer, ackBuffer.length));
        // // Receive data and send acknowledgement packets.
        // If the length of the received datagram is less than 512 bytes,
        // then it is the last one to be received.
        while (!(packet.getLength() < BUFSIZE)) {
          sendSocket.receive(packet);
          fos.write(dataBuffer, 4, packet.getLength() - 4);
          block++;
          // Acknowledgement packet to send
          ack.clear();

          ack.putShort((short) OP_ACK);
          ackBuffer[2] = dataBuffer[2];
          ackBuffer[3] = dataBuffer[3];
          sendSocket.send(new DatagramPacket(ackBuffer, ackBuffer.length));
          data.clear();
        }
        fos.flush();
        fos.close();
        return true;
      } catch (Exception er) {
        er.printStackTrace();
        return false;
      }
    } else {
      try {
        // File with the same name already exists. Send error packet.
        sendSocket.send(send_Err(6));
        return false;
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }
  }

  /**
   * Creates a datagram packet to handle errors.
   * 
   * @param errorCode the specific error code.
   * @return a datagram packet containing the error code and message to be sent to
   *         the client.
   */
  private DatagramPacket send_Err(int errorCode) {
    final String FILE_NOT_FOUND = "File not found.";
    final String ACCESS_VIOLATION = "Access violation.";
    final String ILLEGAL_OPERATION = "Illegal TFTP operation.";
    final String FILE_EXISTS = "File already exists.";
    final String ERROR_NOT_DEFINED = "Not defined.";

    byte[] buffer = new byte[BUFSIZE];
    ByteBuffer packet_Ack = ByteBuffer.wrap(buffer);

    // Constructs an acknowledgment packet for an error to be sent, containing the
    // specified error code.
    packet_Ack.putShort((short) OP_Err);
    packet_Ack.putShort((short) errorCode);
    String errorMessage = "";
    switch (errorCode) {
      // Specifies the message to be sent to the client.
      case 0:
        errorMessage = ERROR_NOT_DEFINED;
        break;
      case 1:
        errorMessage = FILE_NOT_FOUND;
        break;
      case 2:
        errorMessage = ACCESS_VIOLATION;
        break;
      case 4:
        errorMessage = ILLEGAL_OPERATION;
        break;
      case 6:
        errorMessage = FILE_EXISTS;
        break;
      default:
        break;
    }
    for (int i = 0; i < errorMessage.length(); i++) {
      buffer[i + 4] = (byte) errorMessage.charAt(i);
    }
    return new DatagramPacket(buffer, buffer.length);

  }

}
