package maksym.perevalov.model;

import java.util.ArrayList;
import java.util.List;

public class Instruction {
    public int id;
    public int complexity;
    public String value;

    public Instruction(int id, int complexity, String value) {
        this.id = id;
        this.complexity = complexity;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
