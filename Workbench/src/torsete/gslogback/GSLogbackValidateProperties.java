package torsete.gslogback;

import java.util.function.BiFunction;
import java.util.function.Function;

public class GSLogbackValidateProperties implements BiFunction<GSLogbackProperties, String, Boolean> {

    @Override
    public Boolean apply(GSLogbackProperties property, String value) {
        Function<String, Boolean> validator = v -> true;
        switch (property) {
            case GSLOG_CONSOLE:
            case GSLOG_FILE:
                validator = v -> v.equals("true") || v.equals("false");
                break;
            default:
                validator = v -> !v.startsWith("my");

                break;
        }
        return validator.apply(value);
    }
}
