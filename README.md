# Network Protocol Implementations

This repository contains implementations of classic **network protocols** developed as part of a Computer Networks course.  
It demonstrates how to build basic client/server applications in **Java** and **Python**, focusing on **HTTP** and **TFTP**.

---

##  Projects

###  1. Web Server (Java)
A lightweight HTTP file server built with **Java sockets**.

**Features:**
- Serves static files (HTML, CSS, images) from a chosen directory  
- Handles common HTTP status codes:
  - **302** redirects  
  - **404** custom error page  
  - **500** for unsupported methods  
- Includes a file upload endpoint via `upload.html`  

 See [WebServer/README.md](WebServer/README.md) for setup and usage details.

---

###  2. TFTP Server (Python)
An implementation of the **Trivial File Transfer Protocol (TFTP)** using **Python and UDP sockets**.

**Features:**
- Supports **RRQ (read request)** and **WRQ (write request)**  
- Handles file transfers in 512-byte blocks  
- Implements acknowledgments (ACK), error handling, and basic retransmission logic  
- Includes a simple client (`tftpclient.py`) and test scripts  

 See [TFTPServer/README.md](TFTPServer/README.md) for setup and usage details.

---

##  Requirements

### WebServer (Java)
- JDK 8 or higher  

Compile and run:
```bash
javac src/Server.java
java -cp src Server <port> <directory>
```

### TFTPServer (Python)
- Python 3.x  
- Install dependencies:
```bash
pip install -r requirements.txt
```

Run server:
```bash
python tftp_server.py <port> <directory>
```

---

##  Purpose

These projects were created to:
- Learn **socket programming** in Java and Python  
- Understand the inner workings of **application-layer protocols** (HTTP, TFTP)  
- Practice implementing **file transfer, error handling, and packet exchange** at a low level  
- Gain hands-on experience with **protocol design and debugging**  

---

##  Author

**Mustafa Habeb** 

**Baker Mohamad** 

---

##  License

This repository is licensed under the **MIT License**.
