package maksym.perevalov.model;

import java.util.List;
import java.util.stream.Stream;

class ComputationLayer implements Layer {
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
                if (isTransmitted) instruction = null;
                yield isTransmitted ? State.Empty : State.Holding;
            }
            case Computing -> {
                currentProgress++;
                var nextState = currentProgress >= instructionLength ? State.Holding : State.Computing;
                if (nextState == State.Holding) currentProgress = 0;
                yield nextState;
            }
            default -> throw new RuntimeException("not possible");
        };
    }

    @Override
    public void move() {
        if (state == State.Holding || state == State.Empty) {
            tick();
        }
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
            default -> throw new RuntimeException("not possible");
        };
    }

    @Override
    public State state() {
        return state;
    }
}
