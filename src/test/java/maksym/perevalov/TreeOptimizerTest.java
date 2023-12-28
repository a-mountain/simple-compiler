package maksym.perevalov;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import maksym.perevalov.parser.BracketsContext;
import maksym.perevalov.parser.ErrorCollector;
import maksym.perevalov.parser.SyntaxParser;
import maksym.perevalov.parser.Tokenizer;
import maksym.perevalov.tree.Display;
import maksym.perevalov.tree.InfixToPostfixTransformer;
import maksym.perevalov.tree.MathContext;
import maksym.perevalov.tree.MathElement;
import maksym.perevalov.tree.TreeBuilder;
import maksym.perevalov.tree.TreeNode;
import maksym.perevalov.tree.TreeOptimizer;

class TreeOptimizerTest {

    static MathContext MATH_CONTEXT = new MathContext(Collections.emptyList(), Map.ofEntries(
          Map.entry("a", 9.0),
          Map.entry("b", 7.0),
          Map.entry("c", 3.0),
          Map.entry("d", 4.0),
          Map.entry("e", 7.0),
          Map.entry("f", 6.0),
          Map.entry("g", 7.0),
          Map.entry("h", 54.0),
          Map.entry("i", 9.0),
          Map.entry("k", 10.0),
          Map.entry("j", 11.0)
    ));

    @Test
    void test1() {
        String expression = "a+b+c+d+e+f+g+d";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(4, optimizedHeight(expression));
    }

    @Test
    void test2() {
        String expression = "a+b+c+d+e+f+g+d+(a+b+c+d+e+f+g+d)/(a+b+c+d+e+f+g+d)";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(8, optimizedHeight(expression));
    }

    @Test
    void test3() {
        String expression = "a-b-c-d-e-f-g-d";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(4, optimizedHeight(expression));
    }

    @Test
    void test4() {
        String expression = "a+(b+c+d+(e+f)+g)+e";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(4, optimizedHeight(expression));
    }

    @Test
    void test5() {
        String expression = "a-((b-c-d)-(e-f)-g)-s";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(4, optimizedHeight(expression));
    }

    @Test
    void test6() {
        String expression = "a/b/c/d/e/f/g/h";
        assertEquals(result(expression), optimizedResult(expression), 0.00001);
        assertEquals(5, optimizedHeight(expression));
    }

    @Test
    void test7() {
        String expression = "5040/8/7/6/5/4/3/2";
        assertEquals(0.125, optimizedResult(expression), 0.00001);
        assertEquals(1, optimizedHeight(expression));
    }

    @Test
    void test8() {
        String expression = "10-9-8-7-6-5-4-3-2-1";
        assertEquals(-35.0, optimizedResult(expression), 0.00001);
        assertEquals(1, optimizedHeight(expression));
    }

    @Test
    void test9() {
        String expression = "64-(32-16)-8-(4-2-1)";
        assertEquals(39.0, optimizedResult(expression), 0.00001);
        assertEquals(1, optimizedHeight(expression));
    }
    @Test
    void test10() {
        String expression = "a*b - b*c - c*d - a*c*(b-d/e/f/g) - (g - h) - (i-j)";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(8, optimizedHeight(expression));
    }

    @Test
    void test11() {
        String expression = "a*2/0 + b/(b+b*0-1*b) - 1/(c*2*4.76*(1-2+1))";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(5, optimizedHeight(expression));
    }

    double optimizedResult(String expression) {
        var optimizer = new TreeOptimizer(buildTree(expression));
        var optimized = optimizer.optimize();
        Display.displayTree(optimized, "Optimized");
        var compute = optimized.compute(MATH_CONTEXT);
        System.out.println("Optimised result - " + compute);
        return compute;
    }

    double result(String expression) {
        var treeNode = buildTree(expression);
        Display.displayTree(treeNode, "Original");
        return treeNode.compute(MATH_CONTEXT);
    }

    int optimizedHeight(String input) {
        var optimizer = new TreeOptimizer(buildTree(input));
        var optimized = optimizer.optimize();
        return optimized.height();
    }

    TreeNode buildTree(String input) {
        var errorCollector = new ErrorCollector();
        var tokenizer = new Tokenizer(MATH_CONTEXT, errorCollector);
        var tokens = tokenizer.tokenize(input);
        var parser = new SyntaxParser(tokens, new BracketsContext(errorCollector), errorCollector);
        var syntaxTokens = parser.parse();
        var transformer = new InfixToPostfixTransformer();
        var postfix = transformer.transform(syntaxTokens);
        var treeBuilder = new TreeBuilder();
        return treeBuilder.buildTree(postfix);
    }
}
