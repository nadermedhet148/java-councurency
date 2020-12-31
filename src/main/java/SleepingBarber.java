import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SleepingBarber {

    public static void main (String a[]) throws InterruptedException {

        int noOfBarbers=2, customerId=1, noOfCustomers=100, noOfChairs=10;




        ExecutorService exec = Executors.newFixedThreadPool(12);
        Bshop shop = new Bshop(noOfBarbers, noOfChairs);

        Random r = new Random();

        System.out.println("\nBarber shop opened with "
                +noOfBarbers+" barber(s)\n");

        long startTime  = System.currentTimeMillis();

        for(int i=1; i<=noOfBarbers;i++) {

            Barber barber = new Barber(shop, i);
            Thread thbarber = new Thread(barber);
            exec.execute(thbarber);
        }

        for(int i=0;i<noOfCustomers;i++) {

            Customer customer = new Customer(shop);
            customer.setInTime(new Date());
            Thread thcustomer = new Thread(customer);
            customer.setcustomerId(customerId++);
            exec.execute(thcustomer);

            try {

                double val = r.nextGaussian() * 2000 + 2000;
                int millisDelay = Math.abs((int) Math.round(val));
                Thread.sleep(millisDelay);
            }
            catch(InterruptedException iex) {

                iex.printStackTrace();
            }

        }

        exec.shutdown();
        exec.awaitTermination(12, SECONDS);

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("\nBarber shop closed");
        System.out.println("\nTotal time elapsed in seconds"
                + " for serving "+noOfCustomers+" customers by "
                +noOfBarbers+" barbers with "+noOfChairs+
                " chairs in the waiting room is: "
                +TimeUnit.MILLISECONDS
                .toSeconds(elapsedTime));
        System.out.println("\nTotal customers: "+noOfCustomers+
                "\nTotal customers served: "+shop.getTotalHairCuts()
                +"\nTotal customers lost: "+shop.getCustomerLost());

    }
}

class Barber implements Runnable {

    Bshop shop;
    int barberId;

    public Barber(Bshop shop, int barberId) {

        this.shop = shop;
        this.barberId = barberId;
    }

    public void run() {

        while(true) {

            shop.cutHair(barberId);
        }
    }
}

class Customer implements Runnable {

    int customerId;
    Date inTime;

    Bshop shop;

    public Customer(Bshop shop) {

        this.shop = shop;
    }

    public int getCustomerId() {
        return customerId;
    }

    public Date getInTime() {
        return inTime;
    }

    public void setcustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }

    public void run() {

        goForHairCut();
    }
    private synchronized void goForHairCut() {

        shop.add(this);
    }
}

class Bshop {

    private final AtomicInteger totalHairCuts = new AtomicInteger(0);
    private final AtomicInteger customersLost = new AtomicInteger(0);
    int nchair, noOfBarbers, availableBarbers;
    List<Customer> listCustomer;

    Random r = new Random();

    public Bshop(int noOfBarbers, int noOfChairs){

        this.nchair = noOfChairs;
        listCustomer = new LinkedList<Customer>();
        this.noOfBarbers = noOfBarbers;
        availableBarbers = noOfBarbers;
    }

    public AtomicInteger getTotalHairCuts() {

        totalHairCuts.get();
        return totalHairCuts;
    }

    public AtomicInteger getCustomerLost() {

        customersLost.get();
        return customersLost;
    }

    public void cutHair(int barberId)
    {
        Customer customer;
        synchronized (listCustomer) {
            while(listCustomer.size()==0) {

                System.out.println("\nBarber "+barberId+" is waiting "
                        + "for the customer and sleeps in his chair");

                try {

                    listCustomer.wait();
                }
                catch(InterruptedException iex) {

                    iex.printStackTrace();
                }
            }

            customer = (Customer)((LinkedList<?>)listCustomer).poll();

            System.out.println("Customer "+customer.getCustomerId()+
                    " finds the barber asleep and wakes up "
                    + "the barber "+barberId);
        }

        int millisDelay=0;

        try {

            availableBarbers--;
            //cutting hair of the customer and the customer sleeps
            System.out.println("Barber "+barberId+" cutting hair of "+
                    customer.getCustomerId()+ " so customer sleeps");

            double val = r.nextGaussian() * 2000 + 4000;
            millisDelay = Math.abs((int) Math.round(val));
            Thread.sleep(millisDelay);

            System.out.println("\nCompleted Cutting hair of "+
                    customer.getCustomerId()+" by barber " +
                    barberId +" in "+millisDelay+ " milliseconds.");

            totalHairCuts.incrementAndGet();
            //exits through the door
            if(listCustomer.size()>0) {
                System.out.println("Barber "+barberId+
                        " wakes up a customer in the "
                        + "waiting room");
            }

            availableBarbers++;
        }
        catch(InterruptedException iex) {

            iex.printStackTrace();
        }

    }

    public void add(Customer customer) {

        System.out.println("\nCustomer "+customer.getCustomerId()+
                " enters through the entrance door in the the shop at "
                +customer.getInTime());

        synchronized (listCustomer) {

            if(listCustomer.size() == nchair) {

                System.out.println("\nNo chair available "
                        + "for customer "+customer.getCustomerId()+
                        " so customer leaves the shop");

                customersLost.incrementAndGet();

                return;
            }
            else if (availableBarbers > 0) {
                ((LinkedList<Customer>)listCustomer).offer(customer);
                listCustomer.notify();
            }
            else {
                ((LinkedList<Customer>)listCustomer).offer(customer);

                System.out.println("All barber(s) are busy so "+
                        customer.getCustomerId()+
                        " takes a chair in the waiting room");

                if(listCustomer.size()==1)
                    listCustomer.notify();
            }
        }
    }
}