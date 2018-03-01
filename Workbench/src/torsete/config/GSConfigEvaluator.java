package torsete.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Torsten on 01.03.2018.
 */
public class GSConfigEvaluator implements Function<Map<String, String>, Map<String, String>> {
    private Map<String, String> inputMap;
    private Map<String, String> outputMap;
    private Stack<String> leftSides;  // To detect crcular references

    private String arrayDelimiter = ",";
    private String arrayPrefix = "{";
    private String arraySuffix = "}";
    private String variablePrefix = "$";
    private String variableSuffix = "$";

    private boolean isEvaluationEnabled;


    public GSConfigEvaluator() {
        isEvaluationEnabled = true;
    }

    public GSConfigEvaluator enableEvaluation(boolean enabled) {
        isEvaluationEnabled = enabled;
        return this;
    }

    @Override
    public Map<String, String> apply(Map<String, String> map) {
        inputMap = map;
        arrayDelimiter = getValue(inputMap, "-arrayDelimiter", arrayDelimiter);
        arrayPrefix = getValue(inputMap, "-arrayPrefix", arrayPrefix);
        arraySuffix = getValue(inputMap, "-arraySuffix", arraySuffix);
        variablePrefix = getValue(inputMap, "-variablePrefix", variablePrefix);
        variableSuffix = getValue(inputMap, "-variableSuffix", variableSuffix);
        leftSides = new Stack<>();

        Predicate<Map.Entry<String, String>> evaluateValuePredicate = e -> isEvaluationEnabled && !e.getKey().startsWith("-");

        outputMap = map.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> evaluateValuePredicate.test(e) ? evaluate(e.getValue()) : e.getValue()));


        return outputMap;
    }

    protected String evaluate(String source) {
        if (source.startsWith(arrayPrefix)) {
            return evaluate(source.substring(arrayPrefix.length()));
        } else if (source.startsWith(arraySuffix)) {
            return "";
        } else if (source.startsWith(variablePrefix)) {
            int posEnd = source.substring(variablePrefix.length()).indexOf(variableSuffix);
            if (posEnd < 0) {
                return evaluate(source.substring(variablePrefix.length()));
            } else {
                String leftSide = source.substring(variablePrefix.length(), posEnd + variablePrefix.length());
                String rigthValue = inputMap.get(leftSide);
                if (rigthValue == null || leftSides.search(leftSide) >= 0) {
                    return source;
                }
                leftSides.push(leftSide);
                String result = evaluate(rigthValue) + evaluate(source.substring(posEnd + variableSuffix.length() + variablePrefix.length()));
                leftSides.pop();
                return result;
            }
        } else {
            return source.length() == 0 ? "" : source.substring(0, 1) + evaluate(source.substring(1));
        }
    }

    protected String getValue(Map<String, String> map, String key, String defaultValue) {
        String rightSide = map.get(key);
        if (rightSide == null) {
            return defaultValue;
        }
        return rightSide;
    }

    public String getValue(String key) {
        return getValue(outputMap, key, null);
    }

    public List<String> getValues(String key) {
        String rightSide = getValue(key);
        if (rightSide == null) {
            return null;
        }
        List<String> values = new ArrayList<>();
        int end = 0;
        int start = 0;
        while (end < rightSide.length()) {
            if (rightSide.substring(end).startsWith(arrayDelimiter)) {
                values.add(rightSide.substring(start, end));
                end += arrayDelimiter.length();
                start = end;
            } else {
                end++;
            }
        }
        values.add(rightSide.substring(start));
        return values;
    }


}
