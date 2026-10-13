package com.dirkfw.mapping;

import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

public class DfwMultipartFile {

    private final String originalFilename;
    private final String contentType;
    private final long size;
    private final byte[] bytes;

    public DfwMultipartFile(Part part) {
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

    public void transferTo(String destination) throws IOException {
        Path path = Paths.get(destination);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(path, bytes);
    }

    public Path transferToDirectory(String directory) throws IOException {
        Path dir = Paths.get(directory);
        Files.createDirectories(dir);
        Path target = dir.resolve(generateSafeFilename());
        Files.write(target, bytes);
        return target;
    }

    public static String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        String ext = filename.substring(dot).toLowerCase(Locale.ROOT);
        if (ext.length() > 10) return "";
        return ext;
    }

  

    @Override
    public String toString() {
        return "DfwMultipartFile[" + originalFilename
                + ", " + contentType + ", " + size + " bytes]";
    }
}