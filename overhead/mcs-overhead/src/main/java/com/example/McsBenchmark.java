package com.example;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import it.mulders.mcs.App;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class McsBenchmark {

    @Benchmark
    public void mcs_search_no_agent() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException  {
        runSearch();
    }

    @Benchmark
    @Fork(jvmArgsPrepend = {
        "-javaagent:../../../classport-instr-agent/target/classport-instr-agent-0.1.0-SNAPSHOT.jar=mcs_overhead,../output"
    })
    public void mcs_search_with_agent() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        runSearch();
    }

    private void runSearch() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // This simulates running: `mcs search search`
        Method doMain = Class.forName("it.mulders.mcs.App")
            .getDeclaredMethod("doMain", String[].class);
        doMain.setAccessible(true);
        int exitCode = (int) doMain.invoke(null, (Object) new String[] { "search", "search" } );

        if (exitCode != 0) {
            throw new RuntimeException("MCS search command failed with exit code: " + exitCode);
        }
        
    }
}
