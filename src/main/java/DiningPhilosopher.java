import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

class Monitor {
    private int numOfPhilosophers;
    final Lock lock;
    private enum States {hungry, thinking, eating};
    private States [] state;
    final Condition [] cond;
    Monitor(int numOfPhil){
        this.numOfPhilosophers = numOfPhil;
        lock = new ReentrantLock();
        state = new States[numOfPhilosophers];
        cond = new Condition[numOfPhilosophers];
        for(int i = 0; i < numOfPhilosophers; i++){
            state[i] = States.thinking;
            cond[i] = lock.newCondition();
        }
    }
    public void PickUp(int i){
        lock.lock();
        try{
            state[i] = States.hungry;
            if( ( state[(i-1+numOfPhilosophers)%numOfPhilosophers] != States.eating ) &&
                    (state[(i+1)%numOfPhilosophers] != States.eating) ){
                System.out.format("Philosopher %d picks up left chopstick\n", i+1);
                System.out.format("Philosopher %d picks up right chopstick\n", i+1);
                state[i] = States.eating;
            }
            else {
                try {
                    cond[i].await();
                    System.out.format("Philosopher %d picks up left chopstick\n", i+1);
                    System.out.format("Philosopher %d picks up right chopstick\n", i+1);
                    state[i] = States.eating;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        finally{
            lock.unlock();
        }
    }
    public void PutDown(int i){
        lock.lock();
        try{
            System.out.format("Philosopher %d puts down right chopstick\n", i+1);
            System.out.format("Philosopher %d puts down left chopstick\n", i+1);
            state[i] = States.thinking;
            int left = (i - 1 + numOfPhilosophers)%numOfPhilosophers;
            int left2 = (i - 2 + numOfPhilosophers)%numOfPhilosophers;
            if( (state[left] == States.hungry) &&
                    (state[left2] != States.eating) ){
                cond[left].signal();
            }
            if( (state[(i+1)%numOfPhilosophers] == States.hungry) &&
                    (state[(i+2)%numOfPhilosophers] != States.eating) ){
                cond[(i+1)%numOfPhilosophers].signal();
            }
        }
        finally {
            lock.unlock();
        }
    }
}

public class DiningPhilosopher implements Runnable {
    private int myId;
    private int timesToEat;
    private Monitor mon;
    private Thread t;
    private int sleepLength;
    DiningPhilosopher(int id, int numToEat, Monitor m){
        this.myId = id;
        this.timesToEat = numToEat;
        this.mon = m;
        sleepLength = 10;
        t = new Thread(this);
        t.start();
    }
    @Override
    public void run() {
        int count = 1;
        while(count <= timesToEat ){
            mon.PickUp(myId);
            eat(count);
            mon.PutDown(myId);
            ++count;
        }
    }
    void eat(int count){
        System.out.format("Philosopher %d eats (%d times)\n", myId+1, count);
        try {
            Thread.sleep(sleepLength);
        }
        catch (InterruptedException e) {}
    }

    public static void main(String[] args) {
        int numOfPhilosophers = 5;
        int timesToEat = 5;

        Monitor mon = new Monitor(numOfPhilosophers);
        DiningPhilosopher [] p = new DiningPhilosopher[numOfPhilosophers];

        System.out.println("Dinner is starting!");
        System.out.println("");

        for(int i = 0; i < numOfPhilosophers; i++)
            p[i] = new DiningPhilosopher(i, timesToEat, mon);

        for(int i = 0; i < numOfPhilosophers; i++)
            try {
                p[i].t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        System.out.println("");
        System.out.println("Dinner is over!");
    }
}
