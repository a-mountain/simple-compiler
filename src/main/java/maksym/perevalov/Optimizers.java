package maksym.perevalov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.DuplicateFormatFlagsException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Stream;

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
    private static final Deque<TreeNode> stack = new LinkedList<>();

    public static void main(String[] args) {

        var context = new MathContext(Collections.emptyList());
        var tokens = expression(" A-B*c-J*(d*t*j-u*t+c*r-1+w-k/q+m*(n-k*s+z*(y+u*p-y/r-5)+x+t/2))/r+P", context);
        var tree = tree(tokens, true);
        var result = tree.compute(context);
        var newNodes = traverse(tree);
        var list = newNodes.stream().map(TreeNode::toExpressionString).toList();
        list.forEach(System.out::println);
        System.out.println("Size - " + list.size());
    }

    public static List<TreeNode> traverse(TreeNode node) {
        if (node == null) return List.of();
        if (node.value() instanceof MathElement.Value) return List.of(node);

        List<TreeNode> current = new ArrayList<>(List.of(node));

        if (node.value() instanceof MathElement.Operator op && op.isCommutative()) {
            current.add(swap(node));
        }

        var treeNodes = new ArrayList<TreeNode>();
        for (TreeNode curr : current) {
            var newLefts = traverse(curr.left());
            var newRights = traverse(curr.right());
            for (TreeNode newLeft : newLefts) {
                for (TreeNode newRight : newRights) {
                    treeNodes.add(curr.withRight(newRight).withLeft(newLeft));
                }
            }
        }
        return treeNodes;
    }

    private static TreeNode swap(TreeNode node) {
        var left = node.left();
        return node
              .withLeft(node.right())
              .withRight(left);
    }

//    public TreeNode multiply(MathElement.Value value, TreeNode node) {
//        return switch (node.value()) {
//            case MathElement.Value v -> new TreeNode(new MathElement.Multiply(), new TreeNode(value), node);
//            case MathElement.Minus _, MathElement.Plus _ ->
//                  new TreeNode(node.value(), multiply(value, node.left()), multiply(value, node.right()));
//            case MathElement.Divide _, MathElement.Multiply _ ->
//                  new TreeNode(new MathElement.Multiply(), multiply(value, node.left()), node.right());
//            case MathElement.Function _ -> new TreeNode(new MathElement.Multiply(), new TreeNode(value), node);
//        };
//    }

    public static boolean hasBrackets(TreeNode node) {
        if (node == null) return false;
        return node.isBrackets() || hasBrackets(node.left()) || hasBrackets(node.right());
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
