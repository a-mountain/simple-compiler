package maksym.perevalov;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import maksym.perevalov.parser.ErrorCollector;
import maksym.perevalov.parser.SyntaxParser;
import maksym.perevalov.parser.BracketsContext;
import maksym.perevalov.parser.Tokenizer;
import maksym.perevalov.tree.MathContext;

class SyntaxAnalyzerTest {

    ErrorCollector errorCollector;

    @Nested
    class SyntaxErrors {
        @Test
        void shouldDetectInvalidStartWithClosedBracket() {
            var syntaxAnalyzer = analyzer(")1 + 2");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectInvalidVariableName() {
            var syntaxAnalyzer = analyzer("a$ + 2");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectInvalidEndsWithOperator() {
            var syntaxAnalyzer = analyzer("1 + 2 *");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectDoubleOperators() {
            var syntaxAnalyzer = analyzer("1 ++ 2");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectMissingOperatorInsideBrackets() {
            var syntaxAnalyzer = analyzer("(1 2)");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectInvalidOperatorsAfterOpenBracket() {
            var syntaxAnalyzer = analyzer("(+ 1 2)");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectMismatchedParenthesesMissing() {
            var syntaxAnalyzer = analyzer("(1 + 2");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectMismatchedParenthesesExtra() {
            var syntaxAnalyzer = analyzer("(1 + 2))");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectInvalidCommaUsage() {
            var syntaxAnalyzer = analyzer("1, + 2");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectNestedFunctionsMissingArguments() {
            var syntaxAnalyzer = analyzer("sin(1 + 1, cos(2,))");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectInvalidCommaPlacement() {
            var syntaxAnalyzer = analyzer("1 +, 2");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectNestedFunctionsMissingComma() {
            var syntaxAnalyzer = analyzer("max(1 2 3)");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDetectCommaOutsideFunction() {
            var syntaxAnalyzer = analyzer("1 + (1,2)");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldDeleteEmptyBrackets() {
            var syntaxAnalyzer = analyzer("()");
            analyze(syntaxAnalyzer);
            assertTrue(hasErrors());
        }

        @Test
        void shouldAllowEmptyParamFunction() {
            var syntaxAnalyzer = analyzer("sin()");
            analyze(syntaxAnalyzer);
            assertFalse(hasErrors());
        }

        @Test
        void shouldDetectUnaryMinus() {
            var syntaxAnalyzer = analyzer("-(5 + 1)");
            analyze(syntaxAnalyzer);
            assertFalse(hasErrors());
        }

        @Test
        void shouldDetectMinusOperator() {
            var syntaxAnalyzer = analyzer("(x+7)-(0-i)");
            analyze(syntaxAnalyzer);
            assertFalse(hasErrors());
        }
    }

    private boolean hasErrors() {
        if  (errorCollector.hasErrors()) {
            var report = errorCollector.report();
            report.forEach(System.out::println);
        }
        return errorCollector.hasErrors();
    }

    void analyze(SyntaxParser syntaxAnalyzer) {
        syntaxAnalyzer.parse();
    }

    SyntaxParser analyzer(String expression) {
        errorCollector = new ErrorCollector();
        return new SyntaxParser(expression(expression), new BracketsContext(errorCollector), errorCollector);
    }

    List<Tokenizer.RowToken> expression(String expression) {
        return new Tokenizer(new MathContext(List.of("sin", "cos", "max")), new ErrorCollector()).tokenize(expression);
    }
}
