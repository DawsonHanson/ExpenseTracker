import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileOperations {

  private String userOS;
  private String userDir;
  private String workingFileDir;
  private String filePrefix;

  public FileOperations() {
    super();
    initializeVariables();
    try {
      createWorkingDir();
    } catch (IOException e) {
      System.exit(1);
    }
  }

  private void initializeVariables() {
    userDir = System.getProperty("user.dir");
    userOS = System.getProperty("os.name");
    if (userOS.startsWith("Windows")) {
      workingFileDir = userDir + "\\expense_files";
      filePrefix = "\\";
    } else {
      workingFileDir = userDir + "/expense_files";
      filePrefix = "/";
    }
  }

  private void createWorkingDir() throws IOException {
    Files.createDirectories(Paths.get(workingFileDir));
  }

  private void writeToFile(String fileName, String[] textFields) throws IOException {

    BufferedWriter writer = new BufferedWriter(new FileWriter(workingFileDir + fileName, true));

    String name = textFields[0].trim();
    String amount = textFields[1].trim();
    String date = textFields[2].trim();

    writer.write("Name: " + name + " " + "Amount: " + amount + " " + "Date: " + date);
      
    writer.newLine();
    writer.close();
  }

  public void createAndWriteToFile(String fileName, String[] textFields) throws IOException {
    fileName = filePrefix + fileName + ".txt";
    new File(workingFileDir + fileName);
    writeToFile(fileName, textFields);
  }

  public List<String> readFile(String fileName) throws IOException {
    List<String> list = new ArrayList<String>();
    fileName = filePrefix + fileName + ".txt";
    String line;

    BufferedReader reader = new BufferedReader(new FileReader(workingFileDir + fileName));
      
    while((line = reader.readLine()) != null) {
      list.add(line);
    }
      
    reader.close();
    return list;
  }

  public void removeRecord(String fileName, String itemToRemove) throws IOException {
    String orgFileName = filePrefix + fileName + ".txt";
    String tempFileName = filePrefix + "temp.txt";
    String line;

    File oldFile = new File (workingFileDir + orgFileName);
    File tempFile = new File (workingFileDir + tempFileName);

    BufferedReader reader = new BufferedReader(new FileReader(workingFileDir + orgFileName));
    BufferedWriter writer = new BufferedWriter(new FileWriter(workingFileDir + tempFileName, true));
      
    while((line = reader.readLine()) != null) {
      if (line.equals(itemToRemove) != true) {
        writer.write(line);
        writer.newLine();
      }
    }
    writer.close();
    reader.close();

    oldFile.delete();
    File newFile = new File(workingFileDir + orgFileName);
    tempFile.renameTo(newFile);
  }
}