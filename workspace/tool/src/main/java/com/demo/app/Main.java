package com.demo.app;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.writer.io.MemoryDataStore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    public static void main(String[] args) {
        doMain(args[0]);
    }

    private static void doMain(String path) {
        if (path == null || !path.endsWith(".apk")) {
            System.err.println("not apk file!");
            return;
        }

        try {
            File srcFile = new File(path);
            ZipInputStream inputStream = new ZipInputStream(new FileInputStream(srcFile));
            File tempFile = new File(path.substring(0, path.length()-4) + "-new.apk");
            ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(tempFile));
            rewrite(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
            // 签名
            Process process = Runtime.getRuntime().exec(new String[]{
                    "bash", "-c",
                    "jarsigner", "-verbose", "-keystore", "~/.android/debug.keystore",
                    tempFile.getAbsolutePath(),
                    "androiddebugkey"
            });
            System.out.println(process);
            new RedirectThread(process.getInputStream(), System.out);
            new RedirectThread(process.getErrorStream(), System.out);
            System.out.println("return code: " + process.waitFor());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void rewrite(ZipInputStream inputStream, ZipOutputStream outputStream) throws IOException {
        ZipEntry nextEntry;
        while ((nextEntry = inputStream.getNextEntry()) != null) {
            ZipEntry newEntry = new ZipEntry(nextEntry.getName());
            if (nextEntry.getName().equals("")) {
                continue;
            }
            System.out.println("name -> " + nextEntry.getName());
            outputStream.putNextEntry(newEntry);
            if (nextEntry.getName().endsWith(".dex")) {
                // 修改 dex
                MemoryDataStore memoryDataStore = ReWriter.fromDexFile(ReWriter.transform(DexBackedDexFile.fromInputStream(Opcodes.getDefault(), new BufferedInputStream(inputStream))));
                copy(memoryDataStore.readAt(0), outputStream);
            } else {
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

    private static class RedirectThread extends Thread {
        private InputStream inputStream;
        private OutputStream outputStream;

        public RedirectThread(InputStream inputStream, OutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            try {
                copy(inputStream, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
