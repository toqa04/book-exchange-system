package com.bookexchange.util;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileUploadUtil {
    private static final String UPLOAD_DIR = "images";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    public static String saveFile(Part filePart, String applicationPath) throws IOException {
        if (filePart == null || filePart.getSize() == 0) {
            return null;
        }
        
        // Check file size
        if (filePart.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds 5MB limit");
        }
        
        String fileName = getFileName(filePart);
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        
        // Generate unique filename
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
        
        // Create upload directory if it doesn't exist
        String uploadPath = applicationPath + File.separator + UPLOAD_DIR;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // Save file
        File file = new File(uploadDir, uniqueFileName);
        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        return UPLOAD_DIR + "/" + uniqueFileName;
    }
    
    private static String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            String[] tokens = contentDisposition.split(";");
            for (String token : tokens) {
                if (token.trim().startsWith("filename")) {
                    return token.substring(token.indexOf("=") + 2, token.length() - 1);
                }
            }
        }
        return null;
    }
    
    public static boolean deleteFile(String filePath, String applicationPath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        File file = new File(applicationPath + File.separator + filePath);
        return file.exists() && file.delete();
    }
}





