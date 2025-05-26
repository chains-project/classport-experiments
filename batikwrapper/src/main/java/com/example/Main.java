package com.example;

import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

import java.io.*;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        // Pass currrent dir as argument
        String resDir = "";
        if (args.length > 0) {
            System.out.println("Current resources directory: " + args[0]);
            resDir = args[0];
        } else {
            System.out.println("No current resources directory provided.");
        }
        System.out.println("Current resources directory: " + resDir);
        String inputDirPath = resDir;
        File inputDir = new File(inputDirPath);
        File[] svgFiles = inputDir.listFiles((dir, name) -> name.endsWith(".svg"));
        String outputDirPath = resDir + "/output";
        File outputDir = new File(outputDirPath);

        if (!inputDir.exists() || !inputDir.isDirectory()) {
            System.err.println("The input directory does not exist or is not a directory.");
            return;
        }

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.err.println("Failed to create output directory: " + outputDirPath);
            return;
        }

        if (svgFiles == null || svgFiles.length == 0) {
            System.err.println("No SVG files found in /resources");
            return;
        }

        for (File svgFile : svgFiles) {
            try (InputStream in = new FileInputStream(svgFile);
                 OutputStream out = new FileOutputStream(new File(outputDir, "output-" + svgFile.getName() + ".png"))) {

                TranscoderInput input = new TranscoderInput(in);
                TranscoderOutput output = new TranscoderOutput(out);

                PNGTranscoder transcoder = new PNGTranscoder();
                transcoder.transcode(input, output);

                System.out.println("Converted: " + svgFile.getName());
            }
            catch (Exception e) {
                System.err.println("Error processing file: " + svgFile.getName());
                e.printStackTrace();
            }
        }
    }
}
