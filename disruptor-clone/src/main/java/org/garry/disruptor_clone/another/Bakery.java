package org.garry.disruptor_clone.another;

public class Bakery extends Thread{

    // variable for the threads
    public int thread_id; // The id of the current thread
    public static final int countToThis = 200;
    public static final int numberOfThreads = 5;
    // A simple counter for the testing
    public static volatile int count = 0;

    // Global variables for the bakery's algorithm
    // Array that contains boolean values for each thread and it means that
    // thread i wants to get into the critical area or not
    private static volatile boolean[] choosing = new boolean[numberOfThreads];

    // The ticket is used to define the priority
    private static volatile int[] ticket = new int[numberOfThreads];

    /**
     * Thread constructor
     */
    public Bakery(int id){
        thread_id = id;
    }

    // Simple test of a global counter

    @Override
    public void run() {
       int scale = 2;

        for (int i = 0; i < countToThis; i++) {

                lock(thread_id);
                // start of critical section
                count = count + 1;
                System.out.println("I am " + thread_id + " and count is: " + count);
                // wait, in order to cause a race condition among the threads
                try {
                    sleep((int) (Math.random() * scale));
                } catch (InterruptedException e) {
                    // nothing
                }
                // end of critical section
                unlock(thread_id);
        }
    }

    /**
     * Method that does the lock of the bakery's algorithm
     */
    public void lock(int id){
        // That means that the current thread(with id = id), is interested in getting into the critical section
        choosing[id] = true;

        // find the max value and add 1 to get the next available ticket
        ticket[id] = findMax() + 1;
        choosing[id] = false;

        System.out.println("Thread " + id + " got ticket in lock");

        for (int j = 0; j < numberOfThreads; j++) {

            // If the thread j is the current thread go the next thread
            if (j == id)
                continue;

            // Wait if thread j is choosing right now
            while (choosing[j]){
                // nothing
            }
            while (ticket[j] != 0 &&
                    (ticket[id] > ticket[j] || (ticket[id] == ticket[j] && id > j))){
                // nothing
            }

        }
    }

    /**
     * Method that leaves the lock
     */
    private void unlock(int id){
        ticket[id] = 0;
        System.out.println("Thread " + id + " unlock");
    }

    /**
     * Method that finds the max value inside the ticket array
     */
    private int findMax() {

        int m = ticket[0];

        for (int i = 1; i < ticket.length; i++) {
            if (ticket[i] > m)
                m = ticket[i];
        }
        return m;
    }

    public static void main(String[] args) {

        // Initialization of the global variables (it is not necessary at all)
        for (int i = 0; i < numberOfThreads; i++) {
            choosing[i] = false;
            ticket[i] = 0;
        }

        // Array of threads
        Bakery[] threads = new Bakery[numberOfThreads];

        long start = System.currentTimeMillis();
        // Initialize the threads
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Bakery(i);
            threads[i].start();
        }

        // Wait all threads to finish
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("\ntime used " + (end -start));
        System.out.println("\nCount is: " + count);
        System.out.println("\nExpected was: " + (countToThis * numberOfThreads));
    }
}
