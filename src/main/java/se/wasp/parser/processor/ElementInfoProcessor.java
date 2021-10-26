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
    private List<CtElement> allElements = new ArrayList<>();

    @Override
    public void process(CtElement element) {
        allElements.add(element);
    }

    public List<CtElement> getAllElements() {
        return allElements;
    }
}