package maksym.perevalov;

public class Test2 {


    private static double evaluatePostfix(String postfixExpression) {

    }

    public static void main(String[] args) {
        String postfixExpression = "0.5 sin 4 -1 * +"; // (0.5 + (sin(4) * -1))
        double result = evaluatePostfix(postfixExpression);

        if (result != 0.0) {
            System.out.println("Result: " + result);
        } else {
            System.out.println("Evaluation failed.");
        }
    }
}
