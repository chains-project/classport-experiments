package com.example;

import nl.altindag.crip.App;
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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.NotLinkException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class CertRipperBenchmark {

    @Benchmark
    public void ripper_no_agent() {
        runCrip();
    }

    @Benchmark
    @Fork(jvmArgsPrepend = {
        "-javaagent:../../../classport-instr-agent/target/classport-instr-agent-0.1.0-SNAPSHOT.jar=cert_ripper_overhead,../output"
    })
    public void ripper_with_agent() {
        runCrip();
    }

    private void runCrip() {
        // Silence stdout to avoid measurement noise
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        try {
            App.main(new String[]{
                "print", "--url=https://chains.proj.kth.se/"
            });
        } finally {
            System.setOut(originalOut);
        }
    }
}

