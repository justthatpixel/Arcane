package javaff.VAL;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

public class CommandOutput {
    public static void main(String[] args) throws IOException, InterruptedException {
        String directory = "C:\\Arcane\\javaff\\src\\javaff\\VAL";
        String command = "C:/Arcane/javaff/src/javaff/VAL/validate C:\\Arcane\\javaff\\problems\\depots\\domain.pddl";

        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.directory(new File(directory));
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with error code : " + exitCode);
    }

}