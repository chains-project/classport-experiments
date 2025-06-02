package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.tools.PDFBox;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class PDFBoxBenchmark {

    @Benchmark
    public void pdfbox_no_agent(PDFFiles files) throws IOException {
        runPdfBox(files);
    }

    @Benchmark
    @Fork(jvmArgsPrepend = {
        "-javaagent:../../../classport-instr-agent/target/classport-instr-agent-0.1.0-SNAPSHOT.jar=pdfbox_overhead,../output"
    })
    public void pdfbox_with_agent(PDFFiles files) throws IOException {
        runPdfBox(files);
    }

    private void runPdfBox(PDFFiles files) throws IOException {
        Path tempDir = Files.createTempDirectory("pdfbox");
        for (Path pdf : files.pdfs) {
            String basename = pdf.getFileName().toString().replace(".pdf", "");
            PDFBox.main(new String[] {
                "export:text",
                "--input", pdf.toAbsolutePath().toString(),
                "--output", tempDir.resolve(basename + "-text.txt").toString()
            });
        }
    }

    @State(Scope.Benchmark)
    public static class PDFFiles {
        public List<Path> pdfs;

        @Setup(Level.Trial)
        public void setup() {
            Path resources = Path.of("src/main/resources");
            pdfs = Arrays.stream(Objects.requireNonNull(resources.toFile().listFiles()))
                .filter(file -> file.getName().endsWith(".pdf"))
                .map(File::toPath)
                .toList();
        }
    }
}
