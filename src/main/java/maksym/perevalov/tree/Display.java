package maksym.perevalov.tree;

import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;
import hu.webarticum.treeprinter.text.AnsiFormat;
import hu.webarticum.treeprinter.text.ConsoleText;

public class Display {

    public static void displayTree(TreeNode node) {
        var viewableNode = toViewableTreeNode(node);
        new TraditionalTreePrinter().print(viewableNode);
    }

    private static SimpleTreeNode toViewableTreeNode(TreeNode node) {
        var value = node.value() == null ? "nil" : node.value().value();
        var viewableNode = new SimpleTreeNode(ConsoleText.of(value).format(AnsiFormat.GREEN.compose(AnsiFormat.BOLD)));
        if (node.left() != null)
            viewableNode.addChild(toViewableTreeNode(node.left()));
        if (node.right() != null)
            viewableNode.addChild(toViewableTreeNode(node.right()));
        return viewableNode;
    }
}
