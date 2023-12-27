package maksym.perevalov.tree;

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

        boolean isNegative() {
            return value.startsWith("-");
        }
    }

    record Varaible(String value) implements MathElement, Value {
        @Override
        public String toString() {
            return value;
        }

        boolean isNegative() {
            return value.startsWith("-");
        }
    }

    record Plus() implements Operator {
        @Override
        public String value() {
            return "+";
        }

        @Override
        public String toString() {
            return value();
        }
    }

    record Minus() implements Operator {
        @Override
        public String value() {
            return "-";
        }
        @Override
        public String toString() {
            return value();
        }
    }

    record Divide() implements Operator {
        @Override
        public String value() {
            return "/";
        }
        @Override
        public String toString() {
            return value();
        }
    }

    record Multiply() implements Operator {
        @Override
        public String value() {
            return "*";
        }
        @Override
        public String toString() {
            return value();
        }
    }

    record Function(String value) implements Operator {
        @Override
        public String toString() {
            return value();
        }
    }

    sealed interface Value extends MathElement {
    }

    sealed interface Operator extends MathElement {

    }
}
