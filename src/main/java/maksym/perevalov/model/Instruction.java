package maksym.perevalov.model;

public class Instruction {
    public int id;
    public int complexity;

    public Instruction(int id, int complexity) {
        this.id = id;
        this.complexity = complexity;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
