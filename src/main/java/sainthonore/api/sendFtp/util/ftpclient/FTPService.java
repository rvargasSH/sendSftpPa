package sainthonore.api.sendFtp.util.ftpclient;

import java.io.File;

import sainthonore.api.sendFtp.util.execeptions.FTPErrors;

public interface FTPService {

    void connectToFTP(String host, String user, String pass) throws FTPErrors;

    void uploadFileToFTP(File file, String ftpHostDir, String serverFilename) throws FTPErrors;

    void downloadFileFromFTP(String ftpRelativePath, String copytoPath) throws FTPErrors;

    void disconnectFTP() throws FTPErrors;
}
