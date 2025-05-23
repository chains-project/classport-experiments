package com.experiment;

import com.fasterxml.jackson.core.*;
import java.io.*;


public class Main {
    public static void main(String[] args) throws Exception {
        String inputPath = args.length > 0 ? args[0] : "input.json";
        String outputPath = args.length > 1 ? args[1] : "output.json";
        String transformedPath = "transformed.json";

        generateSampleJson(outputPath);
        readJson(outputPath);
        transformJson(outputPath, transformedPath);
        validateJson(outputPath);
        runBenchmark(outputPath);
    }
    
    public static void generateSampleJson(String path) throws Exception {
        JsonFactory factory = new JsonFactory();
        try (JsonGenerator generator = factory.createGenerator(new FileOutputStream(path))) {
            generator.useDefaultPrettyPrinter();
            generator.writeStartObject();
            generator.writeStringField("name", "Alice");
            generator.writeNumberField("age", 30);
            generator.writeFieldName("languages");
            generator.writeStartArray();
            generator.writeString("Java");
            generator.writeString("Python");
            generator.writeString("Go");
            generator.writeEndArray();
            generator.writeEndObject();
        }
        System.out.println("Generated JSON: " + path);
    }

    public static void readJson(String path) throws Exception {
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(new FileInputStream(path))) {
            System.out.println("Reading JSON:");
            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();
                if (token != null)
                    System.out.println(token + ": " + parser.getText());
            }
        }
    }

    public static void transformJson(String inputPath, String outputPath) throws Exception {
        JsonFactory factory = new JsonFactory();
        try (
            JsonParser parser = factory.createParser(new File(inputPath));
            JsonGenerator generator = factory.createGenerator(new File(outputPath), JsonEncoding.UTF8)
        ) {
            generator.useDefaultPrettyPrinter();
            while (parser.nextToken() != null) {
                JsonToken token = parser.currentToken();
                switch (token) {
                    case START_OBJECT -> generator.writeStartObject();
                    case END_OBJECT -> generator.writeEndObject();
                    case START_ARRAY -> generator.writeStartArray();
                    case END_ARRAY -> generator.writeEndArray();
                    case FIELD_NAME -> generator.writeFieldName(parser.getCurrentName());
                    case VALUE_STRING -> generator.writeString(parser.getValueAsString().toUpperCase());
                    case VALUE_NUMBER_INT -> generator.writeNumber(parser.getIntValue() * 2);
                    default -> {}
                }
            }
        }
        System.out.println("Transformed JSON saved to: " + outputPath);
    }

    public static void validateJson(String path) {
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(new File(path))) {
            while (parser.nextToken() != null) {}
            System.out.println("✅ JSON is valid: " + path);
        } catch (Exception e) {
            System.err.println("❌ Invalid JSON: " + path);
            e.printStackTrace();
        }
    }

    public static void runBenchmark(String path) throws Exception {
        long start = System.nanoTime();
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(new File(path))) {
            while (parser.nextToken() != null) {}
        }
        long duration = System.nanoTime() - start;
        System.out.printf("⏱ Parsing time: %.3f ms%n", duration / 1_000_000.0);
    }
}