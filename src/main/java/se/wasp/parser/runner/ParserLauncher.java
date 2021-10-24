package se.wasp.parser.runner;

import se.wasp.parser.processor.ElementInfoProcessor;
import spoon.Launcher;
import spoon.reflect.CtModel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

public class ParserLauncher {

    private Launcher launcher = new Launcher();
    private CtModel model;

    private final static Logger LOGGER = Logger.getLogger(ParserLauncher.class.getName());

    ParserLauncher(String path) {
        LOGGER.info(String.format("Processing %s", path));
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.addInputResource(path);
        launcher.buildModel();
        model = launcher.getModel();
    }

    public void printModel(PrintStream ps) throws IOException {
        ElementInfoProcessor elementInfoProcessor = new ElementInfoProcessor();
        model.processWith(elementInfoProcessor);
        elementInfoProcessor.getElementsInfo().forEach(l -> ps.println(l));
    }
}