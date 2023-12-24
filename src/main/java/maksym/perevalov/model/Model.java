package maksym.perevalov.model;

import java.util.List;
import java.util.stream.Stream;

public class Model {

    public static void main(String[] args) {
        var memory = new Memory(List.of(new Instruction(1, 1), new Instruction(2, 1)));
        var inputLayer = new InputLayer(memory, new ComputationLayer(new ComputationLayer(new OutputLayer(memory))));
        var layers = inputLayer.collectLayers();

        int tick = 0;
        printHeaders();
        while (tick <= 10) {
            var states = layers.stream()
                  .map(Layer::toString)
                  .toList();
            print(tick, states);
            for (Layer layer : layers) {
                layer.tick();
            }
            tick++;
        }
    }

    public interface Layer {
        default boolean transmit(Instruction instruction) {
            return false;
        }

        default void setInstructionLength(int length) {
        }

        void tick();

        List<Layer> collectLayers();
    }


    public static class InputLayer implements Layer {
        private final Memory memory;
        private final Layer nextLayer;
        private State state = State.Reading;
        private Instruction instruction;
        private int instructionLength;

        public InputLayer(Memory memory, Layer nextLayer) {
            this.memory = memory;
            this.nextLayer = nextLayer;
        }

        @Override
        public void tick() {
            state = switch (state) {
                case Empty -> State.Empty;
                case Reading -> {
                    instruction = memory.read();
                    yield instruction == null ? State.Empty : State.Holding;
                }
                case Holding -> {
                    instructionLength = Math.max(instructionLength, instruction.complexity);
                    var isTransmitted = nextLayer.transmit(instruction);
                    if (isTransmitted) nextLayer.setInstructionLength(instructionLength);
                    yield isTransmitted ? State.Reading : State.Holding;
                }
            };
        }

        @Override
        public List<Layer> collectLayers() {
            return Stream.concat(Stream.of(this), nextLayer.collectLayers().stream()).toList();
        }

        @Override
        public String toString() {
            return switch (state) {
                case Reading -> "R";
                case Empty -> "E";
                case Holding -> "H(%s)".formatted(instruction.toString());
            };
        }

        private enum State {
            Reading, Holding, Empty
        }

    }

    static class ComputationLayer implements Layer {
        private final Layer nextLayer;
        private State state = State.Empty;
        private Instruction instruction;
        private int currentProgress;
        private int instructionLength;

        public ComputationLayer(Layer nextLayer) {
            this.nextLayer = nextLayer;
        }

        @Override
        public void setInstructionLength(int length) {
            instructionLength = length;
            nextLayer.setInstructionLength(length);
        }

        @Override
        public boolean transmit(Instruction instruction) {
            var isEmpty = state == State.Empty;
            if (isEmpty) this.instruction = instruction;
            return isEmpty;
        }

        @Override
        public void tick() {
            state = switch (state) {
                case Empty -> instruction == null ? State.Empty : State.Computing;
                case Holding -> {
                    var isTransmitted = nextLayer.transmit(instruction);
                    yield isTransmitted ? State.Empty : State.Holding;
                }
                case Computing -> {
                    currentProgress++;
                    var nextState = currentProgress >= instructionLength ? State.Holding : State.Computing;
                    if (nextState == State.Holding) currentProgress = 0;
                    yield nextState;
                }
            };
        }

        @Override
        public List<Layer> collectLayers() {
            return Stream.concat(Stream.of(this), nextLayer.collectLayers().stream()).toList();
        }

        @Override
        public String toString() {
            return switch (state) {
                case Empty -> "E";
                case Computing -> "C(%s)".formatted(instruction.toString());
                case Holding -> "H(%s)".formatted(instruction.toString());
            };
        }

        private enum State {
            Empty, Holding, Computing,
        }
    }

    static class OutputLayer implements Layer {
        private final Memory memory;
        private State state = State.Empty;
        private Instruction instruction;

        public OutputLayer(Memory memory) {
            this.memory = memory;
        }

        @Override
        public boolean transmit(Instruction instruction) {
            var isEmpty = state == State.Empty;
            if (isEmpty) this.instruction = instruction;
            return isEmpty;
        }

        @Override
        public void tick() {
            state = switch (state) {
                case Empty -> instruction == null ? State.Empty : State.Writing;
                case Writing -> {
                    memory.write();
                    instruction = null;
                    yield State.Empty;
                }
            };
        }

        @Override
        public List<Layer> collectLayers() {
            return List.of(this);
        }

        @Override
        public String toString() {
            return switch (state) {
                case Empty -> "E";
                case Writing -> "W(%s)".formatted(instruction.toString());
            };
        }

        private enum State {
            Empty,
            Writing
        }
    }


    private static void printHeaders() {
        System.out.println("E - Empty, H - holding, W - writing, R - reading, C - computing");
        System.out.printf("%-5s %-4s %-4s %-4s %-4s%n", "Tick", "R", "L1", "L2", "W");
        System.out.println("----------------------");
    }

    private static void print(int tick, List<?> layer) {
        System.out.printf("%-5s %-4s %-4s %-4s %-4s%n", tick, layer.get(0), layer.get(1), layer.get(2), layer.get(3));
    }
}
