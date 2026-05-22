package com.sun.popup;

import com.sun.popup.disk.DiskScanner;
import com.sun.popup.music.MusicControl;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class CrazyAIPopup {

    private static int INIT_CREATED_COUNT = 2;
    private static final int INIT_COUNT = 10;
    private static final int SPEED = 10;

    private static final AtomicInteger popupCount = new AtomicInteger(0);
    private static final Random random = new Random();

    // 后台线程池
    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private static final BlockingQueue<String> logQueue =
            new LinkedBlockingQueue<>();

    // =========================
    // 启动入口
    // =========================
    public static void main(String[] args) {
        System.out.println("程序启动");
        // 后台任务
        EXECUTOR.submit(() -> {
            DiskScanner.scanAllToQueue(logQueue);
        });

        // Swing UI
        SwingUtilities.invokeLater(() -> {

            installGlobalHotKey();

            // 初始弹窗
            for (int i = 0; i < INIT_COUNT; i++) {
                sleep(SPEED);
                createPopup();
            }
        });
        MusicControl.playBackgroundMusicFromResource("0522.WAV");

        keepAlive();
    }

    // =========================
    // 保活线程
    // =========================
    private static void keepAlive() {
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {}
    }

    // =========================
    // 全局快捷键
    // =========================
    private static void installGlobalHotKey() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {

                    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_SPACE) {
                        System.exit(0);
                    }
                    return false;
                });
    }

    // =========================
    // 创建弹窗
    // =========================
    private static void createPopup() {
        System.out.println("创建窗口");
        //        if (popupCount.incrementAndGet() > MAX_POPUPS) {
//            popupCount.decrementAndGet();
//            return;
//        }

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int x = random.nextInt(Math.max(1, screen.width - 320));
        int y = random.nextInt(Math.max(1, screen.height - 180));

        JDialog dialog = buildDialog(x, y);

        JTextArea terminal = buildTerminal();
        JScrollPane scrollPane = new JScrollPane(terminal);
        scrollPane.setBorder(null);

        JButton closeBtn = buildButton();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(10, 10, 10));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(closeBtn, BorderLayout.SOUTH);

        dialog.setContentPane(panel);

        startTypingEffect(terminal);

        bindEvents(dialog, closeBtn);

        dialog.setVisible(true);
    }

    // =========================
    // UI构建
    // =========================
    private static JDialog buildDialog(int x, int y) {
        JDialog dialog = new JDialog();
        dialog.setUndecorated(false);
        dialog.setAlwaysOnTop(true);
        dialog.setSize(500, 200);
        dialog.setLocation(x, y);
        dialog.setTitle("AI_TERMINAL");
        return dialog;
    }

    private static JTextArea buildTerminal() {
        JTextArea terminal = new JTextArea();
        terminal.setEditable(false);
        terminal.setBackground(new Color(10, 10, 10));
        terminal.setForeground(new Color(0, 255, 120));
        terminal.setFont(new Font("Consolas", Font.PLAIN, 13));
        terminal.setLineWrap(true);
        terminal.setWrapStyleWord(true);
        terminal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return terminal;
    }

    private static JButton buildButton() {
        JButton btn = new JButton("der b");
        btn.setBackground(new Color(10, 10, 10));
        btn.setForeground(new Color(0, 255, 120));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 120)));
        return btn;
    }

    // =========================
    // 打字效果（修复EDT阻塞）
    // =========================
    private static void startTypingEffect(JTextArea terminal) {

        String script =
                "AI CORE ACTIVE\n" +
                        "Available Memory: " + availableMemoryGet() + "\n" +
                        "Use Memory: " + useMemoryGet() + "\n\n" +
                        "Deleting...\n";

        List<String> lines = List.of(script.split("\n"));

        Timer timer = new Timer(100, null);

        final int[] index = {0};

        timer.addActionListener(e -> {

            if (index[0] < lines.size()) {
                terminal.append(lines.get(index[0]++) + "\n");
            } else {
                String log = logQueue.poll();
                if (log != null) {
                    terminal.append(log + "\n");
                    terminal.setCaretPosition(terminal.getDocument().getLength());
                }
            }
        });

        timer.start();
    }

    // =========================
    // 事件绑定
    // =========================
    private static void bindEvents(JDialog dialog, JButton closeBtn) {
        //点击按钮
        closeBtn.addActionListener(e -> {

            dialog.dispose();
            popupCount.decrementAndGet();

            for (int i = 0; i < INIT_CREATED_COUNT; i++) {
                EXECUTOR.submit(CrazyAIPopup::createPopup);
            }

            INIT_CREATED_COUNT *= 2;
        });

        dialog.addWindowListener(new WindowAdapter() {
            //关闭窗口
            @Override
            public void windowClosing(WindowEvent e) {

                dialog.dispose();
                popupCount.decrementAndGet();

                for (int i = 0; i < 1000; i++) {
                    EXECUTOR.submit(() -> {
                        sleep(SPEED);
                        SwingUtilities.invokeLater(CrazyAIPopup::createPopup);
                    });
                }
            }
        });
    }

    // =========================
    // 内存信息
    // =========================
    public static String useMemoryGet() {
        SystemInfo si = new SystemInfo();
        GlobalMemory memory = si.getHardware().getMemory();

        long used = memory.getTotal() - memory.getAvailable();
        return used / 1024 / 1024 + " MB";
    }

    public static String availableMemoryGet() {
        SystemInfo si = new SystemInfo();
        GlobalMemory memory = si.getHardware().getMemory();

        return memory.getAvailable() / 1024 / 1024 + " MB";
    }

    // =========================
    // sleep工具
    // =========================
    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}