package torsete.gslogback;

import java.util.function.Function;

public class GSLogbackDefaultProperties implements Function<GSLogbackProperties, String> {

    @Override
    public String apply(GSLogbackProperties property) {
        Function<GSLogbackProperties, String> defaultValue = p -> "";
        switch (property) {
            case GSLOG_DATABASE:
                defaultValue = p -> "Generel";
                break;
            default:
                defaultValue = p -> "false";
                break;
        }
        return defaultValue.apply(property);
    }
}
