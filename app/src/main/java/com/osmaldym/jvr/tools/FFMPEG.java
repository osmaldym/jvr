/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.osmaldym.jvr.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author Osmaldy Maldonado
 */
public class FFMPEG {
    public enum CaptureType {
        DIRECT_SHOW,
        GDI
    }
    
    public enum AudioCapture {
        INTERNAL,
        MICROPHONE,
        MUTED,
    }
    
    public enum VideoCapture {
        SCREEN,
        WINDOW,
        REGION,
    }
    
    public enum OutputFormat {
        MP4,
        MOV,
        MKV,
    }
    
    private static String format = "";
    private static Map<String, String> cmds = new HashMap<String, String>();
    private static ArrayList<String> cmd = new ArrayList<>();
    private static ArrayList<HashMap<String, String>> data = new ArrayList<>();
    private static Process recordingProcess = null;
    
    
    public FFMPEG() {
        cmd.add(System.getProperty("user.dir") + "\\src\\main\\java\\com\\osmaldym\\jvr\\libs\\ffmpeg\\bin\\" + "ffmpeg");
        this.setCaptureType()
            .setOutputFormat()
            .setFramerate()
            .setAudioCapture()
            .setRecordByAllScreens();
    }
    
    /**
     * Stops the recording video
     * @return this instance
     */
    public FFMPEG stop() throws IOException{
        // This cause `recordingProcess.destroy();` doesn't work

        if (recordingProcess.isAlive()){
            OutputStream os = recordingProcess.getOutputStream();
            os.write("q".getBytes()); //'\n' to simulate enter key
            os.flush();
        }
        
        return this;
    }
    
    public BufferedReader getOutput() throws IOException{
//        BufferedReader reader = new BufferedReader(new InputStreamReader(recordingProcess.getInputStream()));
        return new BufferedReader(new InputStreamReader(recordingProcess.getErrorStream()));
    }
    
    /**
     * Record with all default and/or custom configurations.
     * @return this instance
     */
    public FFMPEG record() throws IOException { return record(null); }
    
    /**
     * Record with all default and/or custom configurations.
     * @return this instance
     */
    public FFMPEG record(String name) throws IOException {
        if (name == null) {
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter dateF = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
            name = date.format(dateF);
        }
        
        for (Map.Entry<String, String> cmdData : cmds.entrySet()) {
            cmd.add("-" + cmdData.getKey());
            cmd.add(cmdData.getValue());
        }
        
        File videosFilePath = new File(System.getProperty("user.home") + "\\videos\\jvr\\");
        
        if (!videosFilePath.exists()) videosFilePath.mkdir();
        
        cmd.addLast(videosFilePath.getAbsolutePath() + "\\" + name + format);
        
        ProcessBuilder recordingPB = new ProcessBuilder(cmd);
        recordingProcess = recordingPB.start();
      
        return this;
    }
    
    // Setter for format
    
    /**
     * The format of output file like "filename.mp4".
     * @param format
     * @return this instance
     */
    public FFMPEG setOutputFormat(){ return setOutputFormat(null); }
    
    /**
     * The format of output file like "filename.mp4".
     * @param format
     * @return this instance
     */
    public FFMPEG setOutputFormat(OutputFormat format){
        if (format == null) format = OutputFormat.MP4;
        FFMPEG.format = "." + format.toString().toLowerCase();
        return this;
    }
    
    // Setters for framerate
    
    public FFMPEG setFramerate(){ return setFramerate(25); }
    
    public FFMPEG setFramerate(double fps){
        cmds.put("framerate", Double.toString(fps));
        return this;
    }
    
    // Setters and methods for video capture
    
    private void cleanVideoCapture() {
        cmds.remove("i");
        cmds.remove("offset_x");
        cmds.remove("offset_y");
        cmds.remove("video_size");
        cmds.remove("show_region");
    }
    
    /**
     * Record a video from screen region. <br/><br/>
     * NOTE: For now only works with GDI capture type, remove or update if exist {@code setCaptureType(FFMPEG.CaptureType.DIRECT_SHOW) } before.
     * @return 
     */
    public FFMPEG setRecordByRegion(int offsetX, int offsetY, int width, int height){
        cleanVideoCapture();
        if (cmds.getOrDefault("f", "").equals("gdigrab")) {
            cmds.put("offset_x", Integer.toString(offsetX));
            cmds.put("offset_y", Integer.toString(offsetY));
            cmds.put("video_size", Integer.toString(width) + "x" + Integer.toString(height));
            cmds.put("show_region", "1");
            cmds.put("i", "desktop");
        }
        return this;
    }
    
    /**
     * Record video from window by his title name. <br/><br/>
     * NOTE: For now only works with GDI capture type, remove or update if exist {@code setCaptureType(FFMPEG.CaptureType.DIRECT_SHOW) } before.
     * @return 
     */
    public FFMPEG setRecordByWindow(String windowTitle){
        cleanVideoCapture();
        if (cmds.getOrDefault("f", "").equals("gdigrab")) cmds.put("i", "title=" + windowTitle);
        return this;
    }
    
    /**
     * Record video from screen. <br/><br/>
     * NOTE: For now only works with GDI capture type, remove or update if exist {@code setCaptureType(FFMPEG.CaptureType.DIRECT_SHOW) } before.
     * @return 
     */
    public FFMPEG setRecordByAllScreens(){
        cleanVideoCapture();
        if (cmds.getOrDefault("f", "").equals("gdigrab")) cmds.put("i", "desktop");
        return this;
    }
    
    // Setters for audio capture
    
    /**
     * The audio configuration for record setting by default gets audio by microphone. <br/><br/>
     * NOTE: For now only works with direct show capture type, use {@code setCaptureType(FFMPEG.CaptureType.DIRECT_SHOW) } before.
     * @return 
     */
    public FFMPEG setAudioCapture(){ return setAudioCapture(null); }
    
    /**
     * The audio configuration for record. <br/><br/>
     * NOTE: For now only works with direct show capture type, use {@code setCaptureType(FFMPEG.CaptureType.DIRECT_SHOW) } before.
     * @param audio
     * @return 
     */
    public FFMPEG setAudioCapture(AudioCapture audio){
        switch (cmds.getOrDefault("f", "")){
            case "dshow" -> {
                String audioSetted = cmds.getOrDefault("i", "");
                String videoAndAudio = "\"UScreenCapture\"";
                String trimmedAudio = ":audio=";
                
                if (audioSetted.contains(":audio=")) {
                    String[] splittedAudio = audioSetted.split("=");
                    trimmedAudio = splittedAudio[0] + "=";
                }
                
                if (audio == null) audio = AudioCapture.MICROPHONE;
                
                switch (audio){
                    case AudioCapture.INTERNAL -> {
                        videoAndAudio = trimmedAudio + "\"Audio mix\"";
                    }
                    case AudioCapture.MICROPHONE -> {
                        videoAndAudio = trimmedAudio + "\"Microphone\"";
                    }
                    case AudioCapture.MUTED -> {
                        videoAndAudio = "\"screen-capture-recorder\""; 
                    }
                }

                cmds.put("i" , "video=" + videoAndAudio);
            }
            case "gdigrab" -> {
                // For now, nothing
            }
        }
        return this;
    }
    
    // Setter for hardware acceleration
    
    /**
     * Speed up encoding and reduce the load on your CPU. Uses h264_nvenc
     * @param accelerate
     * @return this instance
     */
    public FFMPEG setHardwareAcceleration(boolean accelerate){
        if (accelerate){
            cmds.put("c:v", "h264_nvenc");
            cmds.put("qp", "0");
        }
        
        return this;
    }
    
    // Setters for capture type
    
    /**
     * Set tool to record, can be GDI or direct show. By default is GDI for more compatibility.
     * @return this instance
     */
    public FFMPEG setCaptureType() { return setCaptureType(null); }
    
    /**
     * Set tool to record, can be GDI or direct show. By default is GDI for more compatibility.
     * @return this instance
     */
    public FFMPEG setCaptureType(CaptureType type){
        if (type == null) type = CaptureType.GDI;

        switch (type) {
            case CaptureType.GDI -> cmds.put("f", "gdigrab");
            case CaptureType.DIRECT_SHOW -> cmds.put("f", "dshow");
        }
        
        return this;
    }
}