package ObjectPackage;

import java.io.Serializable;

public class FileDescription implements Serializable {
    private String fileName;
    private String clientName;
    private String fileType;
    private String filePath;

    public FileDescription(String fileName, String clientName, String fileType, String filePath) {
        this.fileName = fileName;
        this.clientName = clientName;
        this.fileType = fileType;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
