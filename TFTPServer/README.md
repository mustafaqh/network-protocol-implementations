# TFTP Server (Python)

This project is a Python implementation of a **TFTP (Trivial File Transfer Protocol) server**.  
It’s part of a set of network protocol experiments (alongside an HTTP file server) for a Computer Networks course.

---

##  Description

- Implements the **TFTP protocol** to allow clients to **read** or **write** files over UDP.
- Supports **RRQ (read request)** and **WRQ (write request)** operations.
- Handles data packet exchange, acknowledgments, retransmissions, and error conditions.
- Designed for educational purposes (protocol behavior rather than production use).

---

##  Files & Structure

- `tftp_server.py` — main server implementation  
- `tftpclient.py` — sample client for testing  
- `test_tftp.py` — test script for transfers  
- `genfiles.sh` — script to generate sample files  
- `requirements.txt` — dependencies (e.g. pytest, numpy for testing)  
- `bm222ia_mh224tb_assign3.pdf` — project report  
- `ReadMe.txt` — original course submission notes  

---

##  Requirements

- Python 3.x  
- Install dependencies with:  
  ```bash
  pip install -r requirements.txt
  ```

---

##  How to Run

1. Start the server:

   ```bash
   python tftp_server.py <port> <directory>
   ```

   - `<port>` = port number for the TFTP server (default is 69 if privileged, else use >1024)  
   - `<directory>` = directory where files to be read/written are stored

2. Run the client:

   ```bash
   python tftpclient.py <server_ip> <port> <operation> <filename>
   ```

   Examples:
   - **Read (download):**
     ```bash
     python tftpclient.py 127.0.0.1 6969 get f512blks.bin
     ```

   - **Write (upload):**
     ```bash
     python tftpclient.py 127.0.0.1 6969 put clown.png
     ```

---

##  Features & Behavior

- Supports **file read and write requests**  
- Follows TFTP packet structure (DATA, ACK, ERROR)  
- Implements simple **retransmission & timeout** logic  
- Sends appropriate error codes on invalid requests  
- Safe operations within the designated directory (prevents directory traversal)  

---

##  Limitations & Known Issues

- Not production hardened (no authentication, concurrency, or large-file optimizations)  
- May not support very large files or advanced TFTP extensions  
- Timeout/retry logic may be simplistic  
- Best used in controlled/local environments  

---

##  Purpose & Learning Goals

This project was built to:

- Understand the **TFTP protocol internals**  
- Practice **UDP socket programming** in Python  
- Explore **error handling, packet retransmission, and timeouts**  
- Demonstrate protocol implementation from specification  

---

## ✨ Author

**Mustafa Habeb**  
**Baker Mohamad**

---

##  License

This project is licensed under the **MIT License**  
