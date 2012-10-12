package orbotix.drive;

/**
 * Created by IntelliJ IDEA. User: brandon Date: 7/28/11 Time: 11:04 AM To change this template use File | Settings |
 * File Templates.
 */
public class FifoAutoQueue {

    private int[] queue;
    private int first;
    private int last;
    private int size;

    public FifoAutoQueue(int size) {
        queue = new int[size];
        this.size = size;
        first = -1;
        last = -1;
    }

    public void add(int value) {
        if (last < 0) {
            queue[0] = value;
            last = first = 0;
        } else if (last == (size - 1)) {
            queue[0] = value;
            last = 0;
            adjustFirst();
        } else {
            last++;
            queue[last] = value;
            adjustFirst();
        }
    }

    private void adjustFirst() {
        if (first == last && first >= 0) {
            if (first == size - 1) {
                first = 0;
            } else {
                first++;
            }
        }
    }

    public int getFirst() {
        if (first >= 0) {
            return queue[first];
        } else {
            throw new IllegalStateException("Nothing in the queue to return.");
        }
    }

    public int getLast() {
        if (last >= 0) {
            return queue[last];
        } else {
            throw new IllegalStateException("Nothing in the queue to return.");
        }
    }

    public boolean full() {
        if (first > last) {
            if (first - last == 1) {
                return true;
            }
        } else {
            if (first == 0 && last == size - 1) {
                return true;
            }
        }

        return false;
    }
}
