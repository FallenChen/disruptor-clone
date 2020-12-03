package org.garry.disruptor_clone.another;

/**
 * Synchronization between N > 2 processes
 * By Leslie Lamport
 *
 * When we enter the bakery, we are given a particular token (number) for instance in a bakery
 * We need to wait dor some time until the token number xxx is called out
 * So, we have a display over there which periodically would set a token number,and when the token number
 * of xxx is displayed then you are able to get your food from the bakery
 *
 * So, essentially when we look at this from synchronization aspect,we see that we are trying to synchronize the
 * usage of a particular counter
 * So all people who have such a token should wait until their number is called, then sequentially each person depending
 * on when the number is called goes into the counter, and is able to collect whatever he or she wants and for instance eat
 *
 * N processes involved and all these N processes access the same critical section
 */
public class Bakery extends Thread{

    // The id of the current thread
    public int thread_id;
    public static final int countToThis = 200;
    // variable for the threads
    public static final int numberOfThreads = 5;
    // A simple counter for the testing
    public static volatile int count = 0;

    // Global variables for the bakery's algorithm
    // Array that contains boolean values for each thread and it means that
    // thread i wants to get into the critical area or not
    private static volatile boolean[] choosing = new boolean[numberOfThreads];

    // The ticket is used to define the priority
    // global share
    // each thread has a particular index in this ticket array
    // thread1 -> 1 ; thread2 -> 2
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
     * @param id thread id 0 ~ numberOfThreads - 1
     *
     *  map to unsafe version
     *  thread1  thread2  thread3  thread4 thread5
     *     0        0        0        0       0
     *  assume thread3 begin,thread4 execute
     *    0         4         1        2       3  // thread1 is not execute
     *    0         4         0        2       3
     *    0         4         0        0       0
     *    0         4         0        0       0
     *  if ticket[id] = findMax() + 1; not atomic
     *    0         3          1        2       2
     */
    public void lock(int id){
        // That means that the current thread(with id = id), is interested in getting into the critical section
        choosing[id] = true;

        // find the max value and add 1 to get the next available ticket
        // make sure no two threads get the same number
        // unsafe version
        // This is at the doorway! Assume it is not atomic
        // no context switch !!!
        //
        // ticket[id] = findMax() + 1;
        ticket[id] = findMax() + 1;
        choosing[id] = false;

        System.out.println("Thread " + id + " got ticket in lock");

        for (int j = 0; j < numberOfThreads; j++) {

            // If the thread j is the current thread go the next thread
            if (j == id)
                continue;

            // Wait if thread j is choosing right now
            // choosing ensures that a thread is not at the doorway
            // i.e.,the thread is not 'choosing' a value for ticket
            while (choosing[j]){
                // nothing
            }

            // unsafe version
            // only have this
            // lowest number must execute first
            // This is at the doorway.It has to be atomic to ensure two processes do not get the same token
            // while (ticket[j] !=0 && ticket[j] < ticket[id])

            // thread safe
            // 0   3   1   2   2
            // 0   3   0   2   2
            // 0   3   0   0   2  // because
            while (ticket[j] != 0 &&
                    // (ticket[id], id) > (ticket[j], j)
                    // (a,b) > (c,d) is equivalent to : (a > c) or ((a==c) and (b > d))
                    // break the condition when two threads have the same num value
                    (ticket[id] > ticket[j] || (ticket[id] == ticket[j] && id > j))){
                // nothing
            }

        }
    }

    /**
     * Method that leaves the lock
     */
    private void unlock(int id){
        // todo at the start of execution, this value of ticket is all set to 0 why???
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
