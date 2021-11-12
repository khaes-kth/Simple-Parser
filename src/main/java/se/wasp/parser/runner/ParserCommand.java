package se.wasp.parser.runner;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

@Command(name = "parser", mixinStandardHelpOptions = true, version = "1.0",
        description = "Parse Java source file(s)")
public class ParserCommand implements Callable<Integer> {
    @Option(names = {"-s", "--src-dir"}, description = "The path to the source directory to parse")
    private String srcPath;

    @Option(names = {"-f", "--output-format"}, defaultValue = "table",
            description = "The format of the output: table (default)/csv")
    private String outputFormat;

    private final static Logger LOGGER = Logger.getLogger(ParserCommand.class.getName());

    @Override
    public Integer call() throws Exception {
        ParserLauncher parserLauncher = new ParserLauncher(srcPath);
        parserLauncher.printModel(System.out, ParserLauncher.OutputType.valueOf(outputFormat));
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ParserCommand()).execute(args);
        System.exit(exitCode);
    }
}
