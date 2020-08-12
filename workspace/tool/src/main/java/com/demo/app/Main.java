package com.demo.app;

import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        ZipFile zipFile = new ZipFile("/home/clark/source/android/home/dex-rewriter-demo/workspace/demo/build/outputs/apk/debug/demo-debug.apk");
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().endsWith(".dex")) {
                InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                ReWriter.rewrite(DexBackedDexFile.fromInputStream(null, inputStream));
                inputStream.close();
            }
        }
        zipFile.close();

        String filename = "/home/clark/source/android/home/dex-rewriter-demo/workspace/demo/build/outputs/apk/debug/demo-debug.apk";
        File srcFile = new File(filename);
        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(srcFile));
        File tempFile = File.createTempFile("demo-", ".apk");
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(tempFile));

        try {
            rewrite(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
            srcFile.delete();
            tempFile.renameTo(srcFile);
            // todo 签名
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void rewrite(ZipInputStream inputStream, ZipOutputStream outputStream) throws IOException {
        ZipEntry nextEntry;
        while ((nextEntry = inputStream.getNextEntry()) != null) {
            if (nextEntry.getName().endsWith(".dex")) {
                // todo 修改 dex

            } else {
                outputStream.putNextEntry((ZipEntry) nextEntry.clone());
                copy(inputStream, outputStream);
            }

            inputStream.closeEntry();
            outputStream.closeEntry();
        }
    }

    private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buf = new byte[10240];
        int count;
        while ((count = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, count);
        }
        outputStream.flush();
    }

}
