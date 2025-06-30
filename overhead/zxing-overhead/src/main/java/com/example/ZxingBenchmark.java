package com.example;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import org.openjdk.jmh.annotations.*;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class ZxingBenchmark {
    @Param({"src/main/resources"})
    public String resourceDir;

    private File[] pngFiles;
    private MultiFormatReader reader;

    @Setup(Level.Trial)
    public void setup() {
        File dir = new File(resourceDir);
        pngFiles = Objects.requireNonNull(dir.listFiles((d, n) -> n.endsWith(".png")));
        reader = new MultiFormatReader();
    }

    @Benchmark
    public void decodeAllQRCodes_no_agent() throws IOException {
        for (int i = 0; i < 10; i++) {
            for (File f : pngFiles) {
                BufferedImage img = ImageIO.read(f);
                if (img == null) continue;
                LuminanceSource source = new BufferedImageLuminanceSource(img);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    Result result = reader.decode(bitmap);
                    // you can log or use the result
                    result.getText();
                } catch (NotFoundException e) {
                    // skip
                }
            }
        }
    }

    @Benchmark
    @Fork(jvmArgsPrepend = {
        "-javaagent:../../../classport-instr-agent/target/classport-instr-agent-0.1.0-SNAPSHOT.jar=zxing-overhead,../output,dependency"
    })
    public void decodeAllQRCodes_with_agent() throws IOException {
        for (int i = 0; i < 10; i++) {
            for (File f : pngFiles) {
                BufferedImage img = ImageIO.read(f);
                if (img == null) continue;
                LuminanceSource source = new BufferedImageLuminanceSource(img);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    Result result = reader.decode(bitmap);
                    // you can log or use the result
                    result.getText();
                } catch (NotFoundException e) {
                    // skip
                }
            }
        }
    }
}
