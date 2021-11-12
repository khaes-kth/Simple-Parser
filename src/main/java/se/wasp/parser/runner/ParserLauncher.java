package se.wasp.parser.runner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import se.wasp.parser.processor.ElementInfoProcessor;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.ModifierKind;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParserLauncher {
    private static final String ELEMENT_TYPE = "{element-type}";
    private static final String LOCATION = "{location}";
    private static final String FILE_PATH = "{filepath}";
    private static final String EXTRA_INFO = "{extra-info}";
    private static final String VALUE = "{value}";
    private static final String PARENT_LOCATION = "{parent-location}";
    private static final String PARENT_ELEMENT_TYPE = "{parent-element-type}";
    private static final String ELEM_STR_TEMPLATE =
            String.format("%s%s%s%s%s%s%s",
                    ELEMENT_TYPE, LOCATION, PARENT_ELEMENT_TYPE, PARENT_LOCATION, VALUE, FILE_PATH, EXTRA_INFO);
    private static final String INFO_SEPARATOR = ";";
    private static final String[] OUTPUT_HEADERS = { "ELEMENT_TYPE", "LOCATION",
            "PARENT_TYPE", "PARENT_LOCATION", "VALUE", "FILEPATH", "EXTRA_INFO" };

    private Launcher launcher = new Launcher();
    private CtModel model;
    private String sourcePath;

    private final static Logger LOGGER = Logger.getLogger(ParserLauncher.class.getName());

    ParserLauncher(String path) {
        LOGGER.info(String.format("Processing %s", path));
        this.sourcePath = path;
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.addInputResource(path);
        launcher.buildModel();
        model = launcher.getModel();
    }

    public void printModel(PrintStream ps, OutputType outputType) throws IOException {
        ElementInfoProcessor elementInfoProcessor = new ElementInfoProcessor();
        model.processWith(elementInfoProcessor);

        switch (outputType){
            case csv:
                printOutputAsCSV(ps, elementInfoProcessor);
                break;
            case csv2:
                printOutputAsCommonCSV(ps, elementInfoProcessor);
                break;
            case table:
            default:
                printOutputAsTable(ps, elementInfoProcessor);
                break;
        }
    }

    private void printOutputAsCSV(PrintStream ps, ElementInfoProcessor elementInfoProcessor) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(ps, CSVFormat.DEFAULT
                .withHeader(OUTPUT_HEADERS))) {
            for (CtElement element : elementInfoProcessor.getAllElements()) {
                CtElement parent = element.getParent();
                String parentType = parent == null ? "null" : parent.getClass().getSimpleName(),
                        parentLocation = element.getParent() == null ? "null" : getShortLocation(element.getParent()),
                        filePath = element.getPosition().isValidPosition()
                                ? element.getPosition().getFile().getAbsolutePath() : "null";

                printer.printRecord(element.getClass().getSimpleName(), getShortLocation(element), parentType,
                        parentLocation, getElemValue(element), filePath, getExtraInfo(element));
            }
        }
    }

    private void printOutputAsCommonCSV(PrintStream ps, ElementInfoProcessor elementInfoProcessor) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(ps, CSVFormat.DEFAULT
                .withHeader("NAME", "LINE_START", "LINE_END", "COLUMN_START", "COLUMN_END", "REL_PATH", "VALUE",
                        "PARENT_LINE_START", "PARENT_LINE_END", "PARENT_COLUMN_START", "PARENT_COLUMN_END", "PARENT_NAME", "VISIBILITY"))) {
            for (CtElement element : elementInfoProcessor.getAllElements()) {
                CtElement parent = element.getParent();
                String parentType = parent == null ? "null" : parent.getClass().getSimpleName(),
                        parentLocation = element.getParent() == null ? "null" : getShortLocation(element.getParent()),
                        filePath = element.getPosition().isValidPosition()
                                ? element.getPosition().getFile().getAbsolutePath() : "null";

                String relPath = filePath.equals("null") ? "null" : Path.of(sourcePath).toAbsolutePath().relativize(Path.of(filePath)).toString();

                printer.printRecord(
                        element.getClass().getSimpleName(),
                        element.getPosition().isValidPosition() ? element.getPosition().getLine() + "" : "null",
                        element.getPosition().isValidPosition() ? element.getPosition().getEndLine() + "" : "null",
                        element.getPosition().isValidPosition() ? element.getPosition().getColumn() + "" : "null",
                        element.getPosition().isValidPosition() ? element.getPosition().getEndColumn() + "" : "null",
                        relPath,
                        getElemValue(element),
                        parent == null || parent.getPosition().isValidPosition() ? parent.getPosition().getLine() + "" : "null",
                        parent == null || parent.getPosition().isValidPosition() ? parent.getPosition().getEndLine() + "" : "null",
                        parent == null || parent.getPosition().isValidPosition() ? parent.getPosition().getColumn() + "" : "null",
                        parent == null || parent.getPosition().isValidPosition() ? parent.getPosition().getEndColumn() + "" : "null",
                        parentType,
                        getVisibility(element) + ""
                );
            }
        }
    }

    private void printOutputAsTable(PrintStream ps, ElementInfoProcessor elementInfoProcessor) {
        ps.println(populateElemStrTemplate(OUTPUT_HEADERS[0], OUTPUT_HEADERS[1], OUTPUT_HEADERS[6], OUTPUT_HEADERS[2],
                OUTPUT_HEADERS[3], OUTPUT_HEADERS[5], OUTPUT_HEADERS[4]));
        elementInfoProcessor.getAllElements().forEach(element -> ps.println(getElemStr(element)));
    }

    private String getElemStr(CtElement element) {
        CtElement parent = element.getParent();
        String parentType = parent == null ? "null" : parent.getClass().getSimpleName(),
                parentLocation = getFormattedLocation(parent),
                filePath = element.getPosition().isValidPosition() ? element.getPosition().getFile().getAbsolutePath() : "null";

        String elemInfo = populateElemStrTemplate
                (
                        element.getClass().getSimpleName(),
                        getFormattedLocation(element),
                        getExtraInfo(element),
                        parentType,
                        parentLocation,
                        filePath,
                        getElemValue(element));

        return elemInfo;
    }

    private String populateElemStrTemplate
            (
                    String elementType,
                    String location,
                    String extraInfo,
                    String parentType,
                    String parentLocation,
                    String filePath,
                    String value
            ) {
        return ELEM_STR_TEMPLATE
                .replace(ELEMENT_TYPE, String.format("%-30s", elementType))
                .replace(LOCATION, String.format("%-30s", location))
                .replace(EXTRA_INFO, String.format("%-30s", extraInfo))
                .replace(PARENT_ELEMENT_TYPE, String.format("%-30s", parentType))
                .replace(PARENT_LOCATION, String.format("%-30s", parentLocation))
                .replace(FILE_PATH, String.format("%-100s", filePath))
                .replace(VALUE, String.format("%-100s", value));
    }

    private String getFormattedLocation(CtElement element) {
        String elemLocation = "null";
        if (element.getPosition().isValidPosition()) {
            SourcePosition elemPos = element.getPosition();
            elemLocation = Stream.of(
                    String.format("%-5s", elemPos.getLine()),
                    String.format("%-5s", elemPos.getColumn()),
                    String.format("%-5s", elemPos.getEndLine()),
                    String.format("%-5s", elemPos.getEndColumn()))
                    .map(Object::toString).collect(Collectors.joining(""));
        }
        return elemLocation;
    }

    private String getShortLocation(CtElement element) {
        String elementLocation = "null";
        if (element.getPosition().isValidPosition()) {
            SourcePosition elemPos = element.getPosition();
            elementLocation = Stream.of(
                    String.valueOf(elemPos.getLine()),
                    String.valueOf(elemPos.getColumn()),
                    String.valueOf(elemPos.getEndLine()),
                    String.valueOf(elemPos.getEndColumn())).map(Object::toString)
                    .collect(Collectors.joining(","));
        }
        return elementLocation;
    }

    private String getExtraInfo(CtElement element) {
        List<String> extraInfoLst = new ArrayList<>();

        if (element instanceof CtModifiable) {
            if (!element.getPath().toString().contains("subPackage[name=java]")) {
                CtModifiable modifiable = (CtModifiable) element;
                Set<ModifierKind> modifiers = modifiable.getModifiers();
                if (modifiers.toString().contains("public"))
                    extraInfoLst.add("visibility=public");
                else if (modifiers.toString().contains("private"))
                    extraInfoLst.add("visibility=private");
                else if (modifiers.toString().contains("protected"))
                    extraInfoLst.add("visibility=protected");
                extraInfoLst.add(element.getPath().toString());
            }
        }
        return extraInfoLst.stream().collect(Collectors.joining(INFO_SEPARATOR));
    }

    private String getVisibility(CtElement element){
        if (element instanceof CtModifiable) {
            if (!element.getPath().toString().contains("subPackage[name=java]")) {
                CtModifiable modifiable = (CtModifiable) element;
                Set<ModifierKind> modifiers = modifiable.getModifiers();
                if (modifiers.toString().contains("public"))
                    return "public";
                else if (modifiers.toString().contains("private"))
                    return "private";
                else if (modifiers.toString().contains("protected"))
                    return "protected";
            }
        }
        return null;
    }

    private String getElemValue(CtElement element){
        return element.toString() == null ? "null" :
                (element.toString().contains(System.lineSeparator()) ? "multi-line" : element.toString());
    }

    public enum OutputType{
        csv,
        csv2,
        table
    }
}