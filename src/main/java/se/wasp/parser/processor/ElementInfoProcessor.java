package se.wasp.parser.processor;

import spoon.processing.AbstractProcessor;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.ModifierKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElementInfoProcessor extends AbstractProcessor<CtElement> {
    private static final String ELEMENT_TYPE = "{element-type}";
    private static final String LOCATION = "{location}";
    private static final String FILE_PATH = "{filepath}";
    private static final String EXTRA_INFO = "{extra-info}";
    private static final String PARENT_LOCATION = "{parent-location}";
    private static final String PARENT_ELEMENT_TYPE = "{parent-element-type}";
    private static final String ELEM_STR_TEMPLATE =
            String.format("%s%s%s%s%s%s",
                    ELEMENT_TYPE, LOCATION, PARENT_ELEMENT_TYPE, PARENT_LOCATION, FILE_PATH, EXTRA_INFO);
    private static final String INFO_SEPARATOR = ";";

    private List<String> elementsInfo;

    public ElementInfoProcessor(){
        elementsInfo = new ArrayList<>();
        elementsInfo.add(populateElemStrTemplate("ELEMENT_TYPE", "LOCATION", "EXTRA_INFO",
                "PARENT_TYPE", "PARENT_LOCATION", "FILEPATH"));
    }

    @Override
    public void process(CtElement element) {
        elementsInfo.add(getElemStr(element));
    }

    private String getElemStr(CtElement element) {
        CtElement parent = element.getParent();
        String parentType = parent == null ? "null" : parent.getClass().getSimpleName(),
                parentLocation = getElemLocation(parent),
                filePath = element.getPosition().isValidPosition() ? element.getPosition().getFile().getAbsolutePath() : "null";

        String elemInfo = populateElemStrTemplate
                (
                        element.getClass().getSimpleName(),
                        getElemLocation(element),
                        getExtraInfo(element),
                        parentType,
                        parentLocation,
                        filePath);

        return elemInfo;
    }

    private String populateElemStrTemplate
            (
                    String elementType,
                    String location,
                    String extraInfo,
                    String parentType,
                    String parentLocation,
                    String filePath
            ) {
        return ELEM_STR_TEMPLATE
                    .replace(ELEMENT_TYPE, String.format("%-30s", elementType))
                    .replace(LOCATION, String.format("%-30s", location))
                    .replace(EXTRA_INFO, String.format("%-30s", extraInfo))
                    .replace(PARENT_ELEMENT_TYPE, String.format("%-30s", parentType))
                    .replace(PARENT_LOCATION, String.format("%-30s", parentLocation))
                    .replace(FILE_PATH, String.format("%-100s", filePath));
    }

    private String getElemLocation(CtElement element) {
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

    public List<String> getElementsInfo() {
        return elementsInfo;
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
}