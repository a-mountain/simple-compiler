package maksym.perevalov;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import maksym.perevalov.parser.BracketsContext;
import maksym.perevalov.parser.ErrorCollector;
import maksym.perevalov.parser.SyntaxParser;
import maksym.perevalov.parser.Tokenizer;
import maksym.perevalov.tree.InfixToPostfixTransformer;
import maksym.perevalov.tree.MathContext;
import maksym.perevalov.tree.TreeBuilder;
import maksym.perevalov.tree.TreeNode;
import maksym.perevalov.tree.TreeOptimizer;

class TreeOptimizerTest {

    @Test
    void test1() {
        String expression = "1+2+3+4+5+6+7+8";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(4, optimizedHeight(expression));
    }

    @Test
    void test2() {
        String expression = "1+2+3+4+5+6+7+8+(8+9+10+11+12+13+14+15)/(16+17+18+19+20+21+22+23)";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(8, optimizedHeight(expression));
    }

    @Test
    void test3() {
        String expression = "1-2-3-4-5-6-7-8";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(4, optimizedHeight(expression));
    }

    @Test
    void test4() {
        String expression = "1+(2+3+4+(5+6)+7)+8";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(4, optimizedHeight(expression));
    }

    @Test
    void test5() {
        String expression = "1-((2-3-4)-(5-6)-7)-8";
        assertEquals(result(expression), optimizedResult(expression));
        assertEquals(4, optimizedHeight(expression));
    }

    @Test
    void test6() {
        String expression = "1/2/3/4/5/6/7/8";
        assertEquals(result(expression), optimizedResult(expression), 0.00001);
        assertEquals(5, optimizedHeight(expression));
    }

    @Test
    void test7() {
        String expression = "5040/8/7/6/5/4/3/2";
        assertEquals(0.125, optimizedResult(expression), 0.00001);
        assertEquals(5, optimizedHeight(expression));
    }

    @Test
    void test8() {
        String expression = "10-9-8-7-6-5-4-3-2-1";
        assertEquals(-35.0, optimizedResult(expression), 0.00001);
        assertEquals(5, optimizedHeight(expression));
    }

    @Test
    void test9() {
        String expression = "64-(32-16)-8-(4-2-1)";
        assertEquals(39.0, optimizedResult(expression), 0.00001);
        assertEquals(4, optimizedHeight(expression));
    }

    double optimizedResult(String expression) {
        var mathContext = new MathContext(List.of());
        var optimizer = new TreeOptimizer(buildTree(expression));
        var optimized = optimizer.optimize();
        return optimized.compute(mathContext);
    }

    double result(String expression) {
        var mathContext = new MathContext(List.of());
        return buildTree(expression).compute(mathContext);
    }

    int optimizedHeight(String input) {
        var optimizer = new TreeOptimizer(buildTree(input));
        var optimized = optimizer.optimize();
        return optimized.height();
    }

    TreeNode buildTree(String input) {
        var mathContext = new MathContext(Collections.emptyList());
        var errorCollector = new ErrorCollector();
        var tokenizer = new Tokenizer(mathContext, errorCollector);
        var tokens = tokenizer.tokenize(input);
        var parser = new SyntaxParser(tokens, new BracketsContext(errorCollector), errorCollector);
        var syntaxTokens = parser.parse();
        var transformer = new InfixToPostfixTransformer();
        var postfix = transformer.transform(syntaxTokens);
        var treeBuilder = new TreeBuilder();
        return treeBuilder.buildTree(postfix);
    }
}
