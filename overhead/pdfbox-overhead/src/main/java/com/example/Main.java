package com.example;

import org.apache.pdfbox.tools.PDFBox;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

    @Benchmark
    @BenchmarkMode(Mode.All)
    public void pdfbox() throws IOException {
        Path tempDir = Files.createTempDirectory("pdfbox");
        for (Path pdf : PDFFiles.pdfs) {
            String basenameWithoutExtension = pdf.getFileName().toString().replace(".pdf", "");
            PDFBox.main(new String[] { "encrypt", "-O", "123", "-U", "123", "--input", pdf.toAbsolutePath().toString(), "--output", tempDir.resolve(String.format("%s-locked.pdf", basenameWithoutExtension)).toString()});
            PDFBox.main(new String[] { "decrypt", "-password", "123", "--input", tempDir.resolve(String.format("%s-locked.pdf", basenameWithoutExtension)).toString(), "--output", tempDir.resolve(String.format("%s-unlocked.pdf", basenameWithoutExtension)).toString()});
            PDFBox.main(new String[] { "export:text", "--input", tempDir.resolve(String.format("%s-unlocked.pdf", basenameWithoutExtension)).toString(), "--output", tempDir.resolve(String.format("%s-text.txt", basenameWithoutExtension)).toString()});
            PDFBox.main(new String[] { "export:images", "--input", tempDir.resolve(String.format("%s-unlocked.pdf", basenameWithoutExtension)).toString()});
            PDFBox.main(new String[] { "render", "--input", tempDir.resolve(String.format("%s-unlocked.pdf", basenameWithoutExtension)).toString()});
            PDFBox.main(new String[] { "fromtext", "--input", tempDir.resolve(String.format("%s-text.txt", basenameWithoutExtension)).toString(), "--output", tempDir.resolve(String.format("%s-from-text.pdf", basenameWithoutExtension)).toString(), "-standardFont", "Times-Roman"});
            PDFBox.main(new String[] { "split", "--input", tempDir.resolve(String.format("%s-unlocked.pdf", basenameWithoutExtension)).toString(), "-split", "3", "-outputPrefix", tempDir.resolve(String.format("split-%s", basenameWithoutExtension)).toString()});
            PDFBox.main(new String[] { "merge", "--input", tempDir.resolve(String.format("split-%s-1.pdf", basenameWithoutExtension)).toString(), "--output", tempDir.resolve(String.format("merged-%s.pdf", basenameWithoutExtension)).toString()});
            PDFBox.main(new String[] { "decode", tempDir.resolve(String.format("%s-unlocked.pdf", basenameWithoutExtension)).toString(), tempDir.resolve(String.format("%s-decoded.pdf", basenameWithoutExtension)).toString()});
            PDFBox.main(new String[] { "overlay", "-default", pdf.toAbsolutePath().toString(), "--input", tempDir.resolve(String.format("%s-unlocked.pdf", basenameWithoutExtension)).toString(), "--output", tempDir.resolve(String.format("%s-overlay.pdf", basenameWithoutExtension)).toString()});
        }
        System.out.println(tempDir.toString());
    }

    @State(Scope.Benchmark)
    public static class PDFFiles {
        public static List<Path> pdfs;
        static {
            Path resources = Path.of("src/main/resources");
            pdfs = Arrays.stream(Objects.requireNonNull(resources.toFile().listFiles())).filter(file -> file.getName().endsWith(".pdf")).map(File::toPath).toList();
        }

    }
}