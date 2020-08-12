package com.demo.app;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;

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
    public static void main(String[] args) throws Exception {
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
            // 签名
            Process process = Runtime.getRuntime().exec(new String[]{
                    "bash", "-c",
                    "jarsigner", "-verbose", "-keystore", "~/.android/debug.keystore",
                    tempFile.getAbsolutePath(),
                    "androiddebugkey"
            });
            new RedirectThread(process.getInputStream(), System.out);
            new RedirectThread(process.getErrorStream(), System.out);
            System.out.println("return code: " + process.waitFor());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void rewrite(ZipInputStream inputStream, ZipOutputStream outputStream) throws IOException {
        ZipEntry nextEntry;
        while ((nextEntry = inputStream.getNextEntry()) != null) {
            outputStream.putNextEntry((ZipEntry) nextEntry.clone());
            if (nextEntry.getName().endsWith(".dex")) {
                // 修改 dex
                copy(ReWriter.fromDexFile(ReWriter.transform(DexBackedOdexFile.fromInputStream(Opcodes.getDefault(), inputStream))), outputStream);
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
