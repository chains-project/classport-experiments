package com.example;

import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.openjdk.jmh.annotations.*;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class BatikBenchmark {

    @Param({"src/main/resources"})
    public String resourceDir;

    private File[] svgFiles;

    @Setup(Level.Iteration)
    public void setUp() {
        File dir = new File(resourceDir);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Invalid input directory: " + resourceDir);
        }

        svgFiles = dir.listFiles((d, name) -> name.endsWith(".svg"));
        if (svgFiles == null || svgFiles.length == 0) {
            throw new IllegalStateException("No SVG files found in: " + resourceDir);
        }
    }

    @Benchmark
    public void batik_no_agent() throws Exception {
        runTranscoding();
    }

    @Benchmark
    @Fork(jvmArgsPrepend = {
        "-javaagent:../../../classport-instr-agent/target/classport-instr-agent-0.1.0-SNAPSHOT.jar=batik_overhead,../output,dependency"
    })
    public void batik_with_agent() throws Exception {
        runTranscoding();
    }

    private void runTranscoding() throws Exception {
        for (File svgFile : svgFiles) {
            try (InputStream in = new FileInputStream(svgFile);
                 OutputStream out = new ByteArrayOutputStream()) {

                TranscoderInput input = new TranscoderInput(in);
                TranscoderOutput output = new TranscoderOutput(out);

                PNGTranscoder transcoder = new PNGTranscoder();
                transcoder.transcode(input, output);
            }
        }
    }
}
