package org.example;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;


import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

    @State(Scope.Benchmark)
    public static class GraphHopperBenchmark {
        private static Process graphHopperApp;

        // upon each iteration, warmup and measurement, the setup method is called
        @Setup(Level.Iteration)
        public void setup() throws Exception {
            ProcessBuilder graphHopperServer = new ProcessBuilder("java", "-jar", "graphhopper-web-9.1.jar", "server", "config-example.yml");
            graphHopperServer.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            graphHopperServer.redirectError(ProcessBuilder.Redirect.INHERIT);
            graphHopperApp = graphHopperServer.start();
            Thread.sleep(4000);
        }

        @TearDown(Level.Iteration)
        public void tearDown() throws IOException, InterruptedException {
            graphHopperApp.destroy();
        }

        @Benchmark
        @BenchmarkMode(Mode.All)
        public void graphhopper(Blackhole blackhole) throws Exception {
            SSLParameters sslParameters = new SSLParameters();
            sslParameters.setProtocols(new String[] {"TLSv1.2"});
            sslParameters.setNeedClientAuth(false);
            HttpClient client = HttpClient.newBuilder().sslParameters(sslParameters).build();

            List<String> bodyCandidates = GetCoordinates.coordinates;

            for (String body: bodyCandidates) {
                HttpRequest request =
                        HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(body)).header("Content-Type", "application/json").uri(URI.create("http://localhost:8989/route?key=")).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assert response.statusCode() == 200;
                blackhole.consume(response.body());
            }
        }
    }

    @State(Scope.Benchmark)
    public static class GetCoordinates {
        public static List<String> coordinates = new ArrayList<>();
        static {
            coordinates.add("{\"points\":[[13.4,52.55],[13.5,52.5]],\"profile\":\"car\",\"elevation\":true,\"instructions\":true,\"locale\":\"en_US\",\"points_encoded\":true,\"points_encoded_multiplier\":1000000,\"snap_preventions\":[\"ferry\"],\"details\":[\"road_class\",\"road_environment\",\"max_speed\",\"average_speed\"]}");
            coordinates.add("{\"points\":[[13.326646050770488,52.48491117425755],[13.481828,52.460861]],\"profile\":\"car\",\"elevation\":true,\"instructions\":true,\"locale\":\"en_US\",\"points_encoded\":true,\"points_encoded_multiplier\":1000000,\"snap_preventions\":[\"ferry\"],\"details\":[\"road_class\",\"road_environment\",\"max_speed\",\"average_speed\"],\"alternative_route.max_paths\":3,\"algorithm\":\"alternative_route\"}");
            coordinates.add("{\"points\":[[13.353690257807115,52.501884220064625],[13.297218713378905,52.488980766058546],[13.391549,52.447277]],\"profile\":\"car\",\"elevation\":true,\"instructions\":true,\"locale\":\"en_US\",\"points_encoded\":true,\"points_encoded_multiplier\":1000000,\"snap_preventions\":[\"ferry\"],\"details\":[\"road_class\",\"road_environment\",\"max_speed\",\"average_speed\"]}");
            coordinates.add("{\"points\":[[13.506318735769048,52.63548860447159],[13.563469118164067,52.53310157640536],[13.156106,52.601161]],\"profile\":\"car\",\"elevation\":true,\"instructions\":true,\"locale\":\"en_US\",\"points_encoded\":true,\"points_encoded_multiplier\":1000000,\"snap_preventions\":[\"ferry\"],\"details\":[\"road_class\",\"road_environment\",\"max_speed\",\"average_speed\"]}");
            coordinates.add("{\"points\":[[13.547676271484377,52.58903617318725],[13.323811679687495,52.62386860156977],[13.543767124023432,52.43408663412353],[13.338917880859373,52.44064997400247],[13.156106,52.601161]],\"profile\":\"car\",\"elevation\":true,\"instructions\":true,\"locale\":\"en_US\",\"points_encoded\":true,\"points_encoded_multiplier\":1000000,\"snap_preventions\":[\"ferry\"],\"details\":[\"road_class\",\"road_environment\",\"max_speed\",\"average_speed\"]}");
        }
    }
}