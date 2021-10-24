package se.wasp;

import picocli.CommandLine;
import se.wasp.parser.runner.ParserCommand;

public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new ParserCommand()).execute(args);
        System.exit(exitCode);
    }
}
