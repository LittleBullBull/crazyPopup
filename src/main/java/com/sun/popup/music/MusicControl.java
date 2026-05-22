package com.sun.popup.music;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class MusicControl {

    public static void playBackgroundMusicFromResource(String fileName) {
        try {
            InputStream is = MusicControl.class.getResourceAsStream("/wav/" + fileName);
            if (is == null) {
                throw new RuntimeException("音频资源不存在: " + fileName);
            }

            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (Exception e) {
            throw new RuntimeException("播放失败", e);
        }
    }
}