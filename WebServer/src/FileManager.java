import java.io.File;
import java.util.ArrayList;

/**
 * Handles server's file.
 */
public class FileManager {

  private String directoryPath = "./public";
  private String uploadPath = "./uploaded";

  /**
   * Sets the server path.
   * 
   * @param clientPath a path.
   */
  public void setDirectoryPath(String clientPath) {
    this.uploadPath = "../uploaded";
    this.directoryPath = "../" + clientPath;
  }

  /**
   * gets files without argument.
   * 
   * @return All files in public folder.
   */
  public ArrayList<File> getDirectoryFiles() {
    return getFiles(directoryPath);
  }

  public String getDirectoryPath() {
    return directoryPath;
  }

  /**
   * Gets all files.
   * 
   * @param dir the path to a certain directory.
   * @return All files from the public folder on our server.
   */
  private ArrayList<File> getFiles(String dir) {
    File directory = new File(dir);

    try {
      if (directory.isDirectory()) {
        File[] pubFiles = directory.listFiles();
        ArrayList<File> files = new ArrayList<>();
        for (File file : pubFiles) {
          if (file.isFile()) {
            files.add(file);
          } else if (file.isDirectory()) {
            for (File f : getFiles(file.getPath())) {
              files.add(f);
            }
          }
        }
        return files;
      } else {
        System.out.println("Intialized folder is not a directory.");
        System.out.println("Terminating the server!");
        System.exit(1);
      }
    } catch (Exception e) {
      System.out.println("Error occured when reading files on the server.");
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  public String getUploadFolder() {
    return uploadPath;
  }

}