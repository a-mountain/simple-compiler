package maksym.perevalov;

import static maksym.perevalov.parser.SyntaxParser.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import maksym.perevalov.parser.BracketsContext;
import maksym.perevalov.parser.ErrorCollector;
import maksym.perevalov.parser.SyntaxParser;
import maksym.perevalov.parser.Tokenizer;

class InfixToPostfixTransformerTest {

    @Test
    void shouldTransformBasicAdditionAndDivision() {
        var transform = transform(expression("4+18/(9-3)"));
        assertEquals(expected("4 18 9 3 - / +"), transform);
    }

    @Test
    void shouldTransformAdditionAndMultiplication() {
        var transform = transform(expression("3+5*2"));
        assertEquals(expected("3 5 2 * +"), transform);
    }

    @Test
    void shouldTransformExpressionWithParentheses() {
        var transform = transform(expression("(7+3)*(5-2)"));
        assertEquals(expected("7 3 + 5 2 - *"), transform);
    }

    @Test
    void shouldTransformDivisionChain() {
        var transform = transform(expression("8/2/2"));
        assertEquals(expected("8 2 / 2 /"), transform);
    }

    @Test
    void shouldTransformComplexExpression() {
        var transform = transform(expression("((10+2)*6)/2"));
        assertEquals(expected("10 2 + 6 * 2 /"), transform);
    }

    @Test
    void shouldTransformExpressionWithMixedOperators() {
        var transform = transform(expression("9+(3-1)*3+10/2"));
        assertEquals(expected("9 3 1 - 3 * + 10 2 / +"), transform);
    }

    @Test
    void shouldTransformExpressionWithTrigonometricFunctions() {
        var transform = transform(expression("sin(30)+cos(60)"));
        assertEquals(expected("30 sin 60 cos +"), transform);
    }

    @Test
    void shouldTransformExpressionWithPowerFunction() {
        var transform = transform(expression("pow(2,3)"));
        assertEquals(expected("2 3 pow"), transform);
    }

    @Test
    void shouldTransformComplexExpressionWithNestedFunctions() {
        var transform = transform(expression("sqrt(pow(3, 2) + pow(4, 2))"));
        assertEquals(expected("3 2 pow 4 2 pow + sqrt"), transform);
    }

    @Test
    void shouldTransformExpressionWithNestedMinMaxFunctions() {
        var transform = transform(expression("max(5+3, min(10, 7))"));
        assertEquals(expected("5 3 + 10 7 min max"), transform);
    }

    @Test
    void shouldSimplifyExpressionWithExcessiveParentheses() {
        var transform = transform(expression("(((3+2)))"));
        assertEquals(expected("3 2 +"), transform);
    }

    @Test
    void shouldTransformExpressionWithUnaryMinus() {
        var transform = transform(expression("-5"));
        assertEquals(expected("-5"), transform);
    }

    @Test
    void shouldTransformExpressionWithUnaryMinusAndExponentiation() {
        var transform = transform(expression("-2^3"));
        assertEquals(expected("-2 3 ^"), transform);
    }

    List<String> transform(List<SyntaxToken> elements) {
        var transformer = new InfixToPostfixTransformer();
        return transformer.transform(elements).stream().map(MathElement::value).toList();
    }

    List<String> expected(String expected) {
        return Arrays.stream(expected.split(" ")).collect(Collectors.toList());
    }

    List<SyntaxToken> expression(String expression) {
        var context = new MathContext(List.of("sin", "cos", "pow", "sqrt", "min", "max"));
        var errorCollector = new ErrorCollector();
        var tokenizer = new Tokenizer(context, errorCollector);
        var tokens = tokenizer.tokenize(expression);
        var parser = new SyntaxParser(tokens, new BracketsContext(errorCollector), errorCollector);
        var syntaxTokens = parser.parse();
        if (errorCollector.hasErrors()) {
            System.out.println("---Errors---");
            errorCollector.report().forEach(System.out::println);
        } else {
            System.out.println("---Syntax tokens---");
            syntaxTokens.forEach(System.out::println);
        }
        return syntaxTokens;
    }
}
