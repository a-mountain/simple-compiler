package maksym.perevalov.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Memory {
    private final Queue<Instruction> storage;
    private final int initialSize;
    private int writes = 0;

    public Memory(List<Instruction> storage) {
        this.storage = new LinkedList<>(storage);
        this.initialSize = storage.size();
    }

    public Instruction read() {
        return storage.poll();
    }

    public void write() {
        writes++;
    }

    public boolean isEmpty() {
        return writes == initialSize;
    }

}
