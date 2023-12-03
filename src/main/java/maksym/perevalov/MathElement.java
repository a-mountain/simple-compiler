package maksym.perevalov;

public sealed interface MathElement {

    String value();

    record MNumber(String value) implements MathElement, Value {
        public static MNumber zero() {
            return new MNumber("0");
        }

        public static MNumber one() {
            return new MNumber("1");
        }

        @Override
        public String toString() {
            return value;
        }
    }

    record Varaible(String value) implements MathElement, Value {
        @Override
        public String toString() {
            return value;
        }
    }

    record Plus() implements Operator {
        @Override
        public String value() {
            return "+";
        }
    }

    record Minus() implements Operator {
        @Override
        public String value() {
            return "-";
        }
    }

    record Divide() implements Operator {
        @Override
        public String value() {
            return "/";
        }
    }

    record Multiply() implements Operator {
        @Override
        public String value() {
            return "*";
        }
    }

    record Function(String value) implements Operator {
    }

    sealed interface Value extends MathElement {
    }

    sealed interface Operator extends MathElement {

    }
}
