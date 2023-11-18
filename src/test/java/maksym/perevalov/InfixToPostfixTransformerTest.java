package maksym.perevalov;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class InfixToPostfixTransformerTest {

    @Test
    void shouldTransformBasicAdditionAndDivision() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("4+18/(9-3)"));
        assertEquals(expected("4 18 9 3 - / +"), transform);
    }

    @Test
    void shouldTransformAdditionAndMultiplication() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("3+5*2"));
        assertEquals(expected("3 5 2 * +"), transform);
    }

    @Test
    void shouldTransformExpressionWithParentheses() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("(7+3)*(5-2)"));
        assertEquals(expected("7 3 + 5 2 - *"), transform);
    }

    @Test
    void shouldTransformDivisionChain() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("8/2/2"));
        assertEquals(expected("8 2 / 2 /"), transform);
    }

    @Test
    void shouldTransformComplexExpression() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("((10+2)*6)/2"));
        assertEquals(expected("10 2 + 6 * 2 /"), transform);
    }

    @Test
    void shouldTransformExpressionWithMixedOperators() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("9+(3-1)*3+10/2"));
        assertEquals(expected("9 3 1 - 3 * + 10 2 / +"), transform);
    }

    @Test
    void shouldTransformExpressionWithTrigonometricFunctions() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("sin(30)+cos(60)"));
        assertEquals(expected("30 sin 60 cos +"), transform);
    }

    @Test
    void shouldTransformExpressionWithPowerFunction() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("pow(2,3)"));
        assertEquals(expected("2 3 pow"), transform);
    }

    @Test
    void shouldTransformComplexExpressionWithNestedFunctions() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("sqrt(pow(3, 2) + pow(4, 2))"));
        assertEquals(expected("3 2 pow 4 2 pow + sqrt"), transform);
    }

    @Test
    void shouldTransformExpressionWithNestedMinMaxFunctions() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("max(5+3, min(10, 7))"));
        assertEquals(expected("5 3 + 10 7 min max"), transform);
    }

    @Test
    void shouldSimplifyExpressionWithExcessiveParentheses() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("(((3+2)))"));
        assertEquals(expected("3 2 +"), transform);
    }

    @Test
    void shouldTransformExpressionWithUnaryMinus() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("-5"));
        assertEquals(expected("5 -"), transform);
    }

    @Test
    void shouldTransformExpressionWithUnaryMinusAndExponentiation() {
        var sut = new InfixToPostfixTransformer();

        var transform = sut.transform(expression("-2^3"));
        assertEquals(expected("2 3 ^ -"), transform);
    }


    List<String> expected(String expected) {
        return Arrays.stream(expected.split(" ")).collect(Collectors.toList());
    }

    List<Tokenizer.Token> expression(String expression) {
        return new Tokenizer().tokenize(expression);
    }
}
