package maksym.perevalov;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MathContext {
    private final List<String> functions;
    private final Map<String, Double> variables;

    public MathContext(List<String> functions, Map<String, Double> variables) {
        this.functions = functions;
        this.variables = variables;
    }

    public MathContext(List<String> functions) {
        this.functions = functions;
        this.variables = Collections.emptyMap();
    }

    public boolean isFunction(String name) {
        return functions.contains(name);
    }

    public Double readVariable(String variable) {
        if (variable.charAt(0) == '-') {
            var value = variable.substring(1);
            return -1 * variables.get(value);
        }
        return variables.get(variable);
    }
}
