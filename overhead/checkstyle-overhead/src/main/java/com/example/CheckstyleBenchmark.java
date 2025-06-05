package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.xml.sax.InputSource;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.Configuration;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class CheckstyleBenchmark {

    @Param({"../batik-overhead/src/main/java/com/example/BatikBenchmark.java"})
    public String filePath;

    private Checker checker;
    private List<File> filesToProcess;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        // Load Checkstyle configuration
        File configFile = new File("sun_checks.xml");

        Properties properties = new Properties();
        properties.setProperty("checkstyle.cache.file", "checkstyle-cachefile"); // or another valid path

        InputSource inputSource = new InputSource(new FileInputStream(configFile));
        Configuration configuration = ConfigurationLoader.loadConfiguration(
            inputSource,
            new PropertiesExpander(new Properties()),
            ConfigurationLoader.IgnoredModulesOptions.OMIT
            );

            checker = new Checker();
            checker.setModuleClassLoader(Checker.class.getClassLoader());
            checker.configure(configuration);
        

        // Prepare files to process
        filesToProcess = Collections.singletonList(new File(filePath));
    }

    @Benchmark
    public int checkstyle_no_agent() throws Exception {
        return checker.process(filesToProcess);
    }

    @Benchmark
    @Fork(jvmArgsPrepend = {
        "-javaagent:../../../classport-instr-agent/target/classport-instr-agent-0.1.0-SNAPSHOT.jar=checkstyle_overhead,../output"
    })
    public int checkstyle_with_agent() throws Exception {
        return checker.process(filesToProcess);
    }
}
