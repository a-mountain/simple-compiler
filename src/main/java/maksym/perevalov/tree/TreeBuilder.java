package maksym.perevalov.tree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class TreeBuilder {

    public TreeNode buildTree(List<MathElement> postfix) {
        Deque<TreeNode> stack = new ArrayDeque<>();
        for (MathElement element : postfix) {
            if (element.value().equals("(")) {
                var node = stack.pop().withBrackets(true);
                stack.addFirst(node);
                continue;
            }
            if (element instanceof MathElement.Operator) {
                var right = stack.pop();
                var left = stack.pop();
                stack.push(new TreeNode(element, left, right));
            } else {
                stack.push(new TreeNode(element, null, null));
            }
        }
        return stack.pop();
    }
}
