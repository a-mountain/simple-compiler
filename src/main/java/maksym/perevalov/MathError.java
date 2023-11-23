package maksym.perevalov;

public record MathError(String message) implements SyntaxError {
    public static MathError formatted(String message, Object... args) {
        return new MathError(message.formatted(args));
    }
}
