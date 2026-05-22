package com.sun.popup.disk;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public class DiskScanner {

    public static void scanAllToQueue(BlockingQueue<String> queue) {
        File[] roots = File.listRoots();
        for (File root : roots) {
            queue.offer("SCAN ROOT: " + root);

            scanDir(root, "│  ", 0, 3, queue);
        }
    }

    private static void scanDir(File dir, String prefix,
                                int depth, int maxDepth,
                                BlockingQueue<String> queue) {

        if (dir == null || !dir.exists() || depth > maxDepth) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {

            File f = files[i];
            boolean last = (i == files.length - 1);
            String branch = last ? "└─ " : "├─ ";
            String line = prefix + branch + (f.isDirectory() ? "[D] " : "[F] ") + f.getName();
            queue.offer(line);
            if (f.isDirectory()) {
                String newPrefix = prefix + (last ? "   " : "│  ");
                scanDir(f, newPrefix, depth + 1, maxDepth, queue);
            }
        }
    }
}