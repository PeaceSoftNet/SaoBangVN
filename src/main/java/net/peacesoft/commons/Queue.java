package net.peacesoft.commons;

import java.util.Vector;

public class Queue {

    protected Vector queue;

    public Queue() {
        queue = new Vector();
    }

    /**
     * This method is used by a consummer. If you attempt to remove an object
     * from an queue is empty queue, you will be blocked (suspended) until an
     * object becomes available to remove. A blocked thread will thus wake up.
     *
     * @return the first object (the one is removed).
     */
    public Object remove() {
        synchronized (queue) {
            while (queue.isEmpty()) { //Threads are blocked
                try { //if the queue is empty.

                    queue.wait(); //wait until other thread call notify().
                } catch (InterruptedException ex) {
                    System.out.println("InterruptedException:" + ex.toString());
                }
            }
            Object item = queue.firstElement();
            queue.removeElement(item);
            return item;
        }
    }

    public void add(Object item) {
        synchronized (queue) {
            queue.addElement(item);
            queue.notify();
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int queueSize() {
        return queue.size();
    }

    public void setVector(Vector v) {
        this.queue = v;
    }

    public Vector getVector() {
        return this.queue;
    }
}
