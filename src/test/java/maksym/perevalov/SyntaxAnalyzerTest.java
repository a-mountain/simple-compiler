package maksym.perevalov;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import maksym.perevalov.SyntaxValidator.Result;

class SyntaxAnalyzerTest {

    @Nested
    class SyntaxErrors {
        @Test
        void shouldDetectInvalidStartWithClosedBracket() {
            var syntaxAnalyzer = analyzer(")1 + 2");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }

        @Test
        void shouldDetectInvalidVariableName() {
            var syntaxAnalyzer = analyzer("a$ + 2");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }

        @Test
        void shouldDetectInvalidEndsWithOperator() {
            var syntaxAnalyzer = analyzer("1 + 2 *");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }

        @Test
        void shouldDetectDoubleOperators() {
            var syntaxAnalyzer = analyzer("1 ++ 2");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }

        @Test
        void shouldDetectMissingOperatorInsideBrackets() {
            var syntaxAnalyzer = analyzer("(1 2)");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }

        @Test
        void shouldDetectInvalidOperatorsAfterOpenBracket() {
            var syntaxAnalyzer = analyzer("(+ 1 2)");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }

        @Test
        void shouldDetectMismatchedParenthesesMissing() {
            var syntaxAnalyzer = analyzer("(1 + 2");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }

        @Test
        void shouldDetectMismatchedParenthesesExtra() {
            var syntaxAnalyzer = analyzer("(1 + 2))");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }

        @Test
        void shouldDetectInvalidCommaUsage() {
            var syntaxAnalyzer = analyzer("1, + 2");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }

        @Test
        void shouldDetectNestedFunctionsMissingArguments() {
            var syntaxAnalyzer = analyzer("sin(1 + 1, cos(2,))");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }

        @Test
        void shouldDetectInvalidCommaPlacement() {
            var syntaxAnalyzer = analyzer("1 +, 2");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }

        @Test
        void shouldDetectNestedFunctionsMissingComma() {
            var syntaxAnalyzer = analyzer("max(1 2 3)");
            var result = analyze(syntaxAnalyzer);
            assertTrue(result.isError());
        }
    }

    Result analyze(SyntaxValidator syntaxAnalyzer) {
        var analyze = syntaxAnalyzer.analyze();
        var errors = analyze.errors().stream().map(SyntaxError::message).toList();
        errors.forEach(System.out::println);
        return analyze;
    }

    SyntaxValidator analyzer(String expression) {
        return new SyntaxValidator(expression(expression), new BracketsContext());
    }

    List<Tokenizer.RowToken> expression(String expression) {
        return new Tokenizer(new MathContext(List.of("sin", "cos", "max"))).tokenize(expression);
    }
}
