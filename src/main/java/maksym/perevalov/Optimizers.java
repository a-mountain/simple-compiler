package maksym.perevalov;

import java.util.List;

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

/**
 * комутативний
 * дистрибутивний
 * "a*(b+c-1)*d"
 */
public class Optimizers {
    public static void main(String[] args) {
        var context = new MathContext(List.of());
        var tokens = expression("a*(b+c-1)*d", context);
        tokens.forEach(System.out::println);
        var tree = tree(tokens, true);
        var first = tree.toExpressionString();
        Display.displayTree(tree);
        traverse(tree);
        tree = optimize(tree);
        Display.displayTree(tree);
        System.out.println(first);
        System.out.println(tree.toExpressionString());
    }

    public static void traverse(TreeNode node) {
        if (node.value() instanceof MathElement.Multiply) {

            if (node.right().isBrackets()) {
                node.setRight(multiply(node.left(), node.right()));
            }

        }
    }

    public static TreeNode multiply(TreeNode left, TreeNode right) {
        if (left.value() instanceof MathElement.Value) {

            if (right.value() instanceof MathElement.Value) {
                return new TreeNode(new MathElement.Multiply(), left, right);
            }

            if (right.value() instanceof MathElement.Plus || right.value() instanceof MathElement.Minus) {
                return new TreeNode(right.value(), multiply(left, right.left()), multiply(left, right.right()));
            }

        }

        if (left.value() instanceof MathElement.Multiply) {

            return multiply(left.left(), right);

        }

        return null;
    }

    private static TreeNode tree(List<SyntaxParser.SyntaxToken> tokens, boolean optimize) {
        var transformer = new InfixToPostfixTransformer();
        var postfix = transformer.transform(tokens);
        System.out.println("postfix = " + postfix);
        var tree = new TreeBuilder().buildTree(postfix);
        if (optimize) {
            return optimize(tree);
        } else {
            return tree;
        }
    }

    public static List<SyntaxParser.SyntaxToken> expression(String input, MathContext context) {
        var errorCollector = new ErrorCollector();
        var tokenizer = new Tokenizer(context, errorCollector);
        var tokens = tokenizer.tokenize(input);
        var parser = new SyntaxParser(tokens, new BracketsContext(errorCollector), errorCollector);
        return parser.parse();
    }

    public static TreeNode optimize(TreeNode node) {
        return new TreeOptimizer(node).optimize();
    }
}
