package com.example.memorytracker;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    // Writes all files of the files dir into a zip at the location the user picked
    public static boolean zipFilesDir(Context context, Uri toLocation) {
        File filesDir = context.getFilesDir();
        try (OutputStream dest = context.getContentResolver().openOutputStream(toLocation);
             ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest))) {
            zipSubFolder(out, filesDir, filesDir.getPath().length() + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void zipSubFolder(ZipOutputStream out, File folder,
                                     int basePathLength) throws IOException {
        byte[] data = new byte[8192];
        File[] fileList = folder.listFiles();
        if (fileList == null) {
            return;
        }
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                String relativePath = file.getPath().substring(basePathLength);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                try (InputStream origin = new BufferedInputStream(new FileInputStream(file))) {
                    int count;
                    while ((count = origin.read(data)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                out.closeEntry();
            }
        }
    }

    // Restores a backup zip into the files dir. Works with zips from the export, from the
    // old export (entries under "files/") and with a zipped backup folder.
    public static boolean unzipToFilesDir(Context context, Uri zipUri) {
        File filesDir = context.getFilesDir();
        String root = findBackupRoot(context, zipUri);
        if (root == null) {
            return false;
        }
        try (InputStream is = context.getContentResolver().openInputStream(zipUri);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {
            String filesPath = filesDir.getCanonicalPath();
            byte[] buffer = new byte[8192];
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = normalizeEntryName(entry.getName());
                if (!name.startsWith(root)) {
                    continue;
                }
                String relativePath = name.substring(root.length());
                if (relativePath.isEmpty()) {
                    continue;
                }
                File target = new File(filesDir, relativePath);
                // Guard against entries escaping the files dir ("../")
                if (!target.getCanonicalPath().startsWith(filesPath + File.separator)) {
                    continue;
                }
                if (entry.isDirectory()) {
                    target.mkdirs();
                    continue;
                }
                target.getParentFile().mkdirs();
                if (relativePath.endsWith(".json")) {
                    // Stored file paths point to the files dir of the old device, adjust them
                    ByteArrayOutputStream json = new ByteArrayOutputStream();
                    int count;
                    while ((count = zis.read(buffer)) != -1) {
                        json.write(buffer, 0, count);
                    }
                    String content = json.toString("UTF-8")
                            .replaceAll("/data/(user/\\d+|data)/" + context.getPackageName() + "/files", filesDir.getAbsolutePath());
                    try (FileOutputStream out = new FileOutputStream(target)) {
                        out.write(content.getBytes("UTF-8"));
                    }
                } else {
                    try (FileOutputStream out = new FileOutputStream(target)) {
                        int count;
                        while ((count = zis.read(buffer)) != -1) {
                            out.write(buffer, 0, count);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Returns the path prefix inside the zip that contains the data json files, or null if none found
    private static String findBackupRoot(Context context, Uri zipUri) {
        try (InputStream is = context.getContentResolver().openInputStream(zipUri);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = normalizeEntryName(entry.getName());
                if (!entry.isDirectory() && name.endsWith("_data.json")) {
                    return name.substring(0, name.lastIndexOf('/') + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String normalizeEntryName(String name) {
        name = name.replace('\\', '/');
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        return name;
    }
}
