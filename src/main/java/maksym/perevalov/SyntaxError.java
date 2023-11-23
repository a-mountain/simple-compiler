package maksym.perevalov;

public sealed interface SyntaxError {

    record TokenError(String value, int position) implements SyntaxError {
    }
}
