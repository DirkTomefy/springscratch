package com.dirkfw.mapping;

import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

import com.dirkfw.config.ConfigLoader;

public class DfwMultipartFile {

    private final String originalFilename;
    private final String contentType;
    private final long size;
    private final byte[] bytes;
    private final String uploadDir;

    public DfwMultipartFile(Part part, String uploadDir) {
        this.uploadDir = uploadDir;
        try {
            this.originalFilename = sanitize(part.getSubmittedFileName());
            this.contentType = part.getContentType();
            this.size = part.getSize();

            try (InputStream in = part.getInputStream()) {
                this.bytes = in.readAllBytes();
            }
        } catch (IOException e) {
            throw new RuntimeException("Lecture multipart échouée", e);
        }
    }

    public String getOriginalFilename() { return originalFilename; }
    public String getContentType()      { return contentType; }
    public long   getSize()             { return size; }
    public byte[] getBytes()            { return bytes; }
    public String getUploadDir()        { return uploadDir; }
    public boolean isEmpty()            { return bytes == null || bytes.length == 0; }

    public String getExtension() {
        return extractExtension(originalFilename);
    }

    public String getBaseName() {
        if (originalFilename == null) return "";
        int dot = originalFilename.lastIndexOf('.');
        return dot < 0 ? originalFilename : originalFilename.substring(0, dot);
    }

    public String generateSafeFilename() {
        return UUID.randomUUID() + getExtension();
    }

    public Path transferTo(String destination) throws IOException {
        Path path = Paths.get(destination);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(path, bytes);
        return path;
    }

    public Path uploadDirectory() throws IOException {
        if (uploadDir == null || uploadDir.trim().isEmpty()) {
            throw new IllegalStateException(
                    "uploadDir non configuré dans dirkfw.propreties ("
                    + ConfigLoader.UPLOAD_DIR_KEY + ")");
        }
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Path target = dir.resolve(generateSafeFilename());
        Files.write(target, bytes);
        return target;
    }

    private static String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        String ext = filename.substring(dot).toLowerCase(Locale.ROOT);
        if (ext.length() > 10) return "";
        return ext;
    }

    private static String sanitize(String filename) {
        if (filename == null) return "";
        String name = filename.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        name = name.replaceAll("[\\p{Cntrl}]", "").trim();
        return name;
    }

    @Override
    public String toString() {
        return "DfwMultipartFile[" + originalFilename
                + ", " + contentType + ", " + size + " bytes"
                + ", uploadDir=" + uploadDir + "]";
    }
}