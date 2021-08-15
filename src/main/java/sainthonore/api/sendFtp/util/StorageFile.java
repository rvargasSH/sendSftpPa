package sainthonore.api.sendFtp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sainthonore.api.sendFtp.util.execeptions.FileStorageException;

@Service
public class StorageFile {

    private final Path fileStorageLocation;

    @Autowired
    public StorageFile(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.",
                    ex);
        }
    }

    public void CreateFile(String body, String fileName) {

        File dir = new File(this.fileStorageLocation.toAbsolutePath() + "/");
        if (!dir.exists()) {
            dir.setReadable(true, false);
            dir.setExecutable(true, false);
            dir.setWritable(true, false);
            dir.mkdirs();
        }
        fileName = dir.getAbsolutePath() + "/" + fileName + ".txt";
        System.out.println(fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(fileName);
            byte[] strToBytes = body.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (IOException ex) {
            System.out.println("error " + ex.getMessage());
        }
    }

    public void CreateFileWithName(String body, String fileName) {

        File dir = new File(this.fileStorageLocation.toAbsolutePath() + "/logs");
        if (!dir.exists()) {
            dir.setReadable(true, false);
            dir.setExecutable(true, false);
            dir.setWritable(true, false);
            dir.mkdirs();
        }

        String inicialFecha = fileName + ".txt";
        fileName = dir.getAbsolutePath() + "/" + inicialFecha;
        System.out.println(fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(fileName);
            byte[] strToBytes = body.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (IOException ex) {
            System.out.println("error " + ex.getMessage());
        }
    }

}