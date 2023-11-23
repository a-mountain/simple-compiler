package maksym.perevalov;

import java.util.List;

public class MathContext {
    private final List<String> functions;

    public MathContext(List<String> functions) {
        this.functions = functions;
    }

    public boolean isFunction(String name) {
        return functions.contains(name);
    }
}
