
package com.example.zxing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class ZxingWorkload {
    public static void main(String[] args) throws Exception {
        // resource dir is from arg 
        if (args.length < 1) {
            System.err.println("Usage: java ZxingWorkload <resource-dir>");
            System.exit(1);
        }
        String resourceDir = args[0];
        File dir = new File(resourceDir);
        File[] files = Objects.requireNonNull(dir.listFiles((d, n) -> n.endsWith(".png")));

        MultiFormatReader reader = new MultiFormatReader();

        for (int i = 0; i < 10; i++) {
            for (File f : files) {
                BufferedImage img = ImageIO.read(f);
                if (img == null) continue;
                LuminanceSource source = new BufferedImageLuminanceSource(img);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    Result result = reader.decode(bitmap);
                    System.out.println("Decoded: " + result.getText());
                } catch (NotFoundException e) {
                    System.out.println("Could not decode: " + f.getName());
                }
            }
        }
    }
}
