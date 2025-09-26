# Web Server (Java HTTP File Server)

This project is a lightweight **HTTP file server** implemented in **Java**, built for a Computer Networks course.  
It supports static file serving, basic error handling, and file upload functionality.

---

##  Features

- Serves static files (HTML, CSS, images, etc.) from a specified directory  
- Handles common HTTP status responses:
  - **302** (redirect) for certain non-HTML/non-image resources  
  - **404** (Not Found) with a custom `404.html` page  
  - **500** (Internal Server Error) for unsupported methods  
- Provides a simple file upload endpoint through `upload.html`  
- Command-line interface to run the server:
  ```bash
  java Server <port> <directory>
  ```

---

##  Project Structure

Typical files and directories you might find here:

- `src/` — Java source code (e.g. `Server.java`, handler classes)  
- `upload.html` — Front-end form to upload files  
- `404.html` — Custom "Not Found" page  
- `public/` (or another directory) — The root directory being served  
- `bm222ia_mh224tb_assign2.pdf` — Project report / documentation  
- `README.md` — This file  

---

##  Requirements & Setup

- Java Development Kit (JDK) installed (Java 8+ recommended)  
- Compile the server:
  ```bash
  javac src/Server.java
  ```
- Run the server:
  ```bash
  java -cp src Server <port> <directory>
  ```

Make sure the directory you serve has:
- `index.html` or default files  
- `404.html` for not found responses  
- Adequate permissions for file upload if upload endpoint is used

---

##  Usage Examples

- Start the server on port 8080 serving the `public` folder:
  ```bash
  java Server 8080 public
  ```

- Access via browser:
  - `http://localhost:8080/index.html`  
  - Upload files via `http://localhost:8080/upload.html`

- If a non‑existent file (e.g. `http://localhost:8080/doesnotexist.txt`) is requested, the server should return the `404` page.

---

##  Known Limitations & Notes

- Not intended for production use (no security, concurrency limits, or advanced optimization)  
- Upload logic may be basic (no file size checks or concurrency)  
- Redirect behavior (302) for non-image/HTML paths may have quirks (see project report)  
- Only **GET** and **POST** methods are (or should be) supported

---

##  Learning Goals / Purpose

This project is meant to help you:
- Understand the basics of **HTTP** (requests, responses, status codes)  
- Practice **Java socket programming** for HTTP  
- Implement **file serving and upload** logic  
- Handle **errors and edge cases** in a web server

---

##  Author

**Mustafa Habeb**  
**Baker Mohamad**

---

##  License

This project is licensed under the **MIT License**
