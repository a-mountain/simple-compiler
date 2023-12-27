package maksym.perevalov.model;

import java.util.List;

public interface Layer {
    default boolean transmit(Instruction instruction) {
        return false;
    }

    default void setInstructionLength(int length) {
    }

    void tick();

    void move();

    List<Layer> collectLayers();

    State state();
}
