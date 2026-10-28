package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.cli.CommandProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Amusement park application entry point.
 */
public class App {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        new CommandProcessor().run(in, System.out);
    }
}
