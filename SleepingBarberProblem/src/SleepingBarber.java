/*
 * Sleeping Barber Problem (2 barbers)
 * Avi Ginsberg (s0753107)
 * CS-438-50
 */
import java.util.concurrent.*;
import java.util.*;


public class SleepingBarber extends Thread {

  /* PREREQUISITES */


  /* we create the semaphores. First there are no customers and 
   the barber is asleep so we call the constructor with parameter
   0 thus creating semaphores with zero initial permits. 
   Semaphore(1) constructs a binary semaphore, as desired. */
  
    public static Semaphore customers = new Semaphore(0);
    public static Semaphore barber = new Semaphore(0);
    public static Semaphore accessSeats = new Semaphore(1);

  /* we denote that the number of chairs in this barbershop is 5. */

    public static final int CHAIRS = 5;
    public static final int NumOfBarbers = 2;

  /* we create the integer numberOfFreeSeats so that the customers
   can either sit on a free seat or leave the barbershop if there
   are no seats available */

   public static int numberOfFreeSeats = CHAIRS;
  // public static boolean justTookNewCust=false;
   public static int justTookNewCust=0;
   
   //public static Queue<Integer> WaitingRoomQueue = new LinkedList<Integer>();
   public static ArrayList<Integer> WaitingRoomList = new ArrayList<Integer>();
   
   
/* THE CUSTOMER THREAD */

class Customer extends Thread {
  
  /* we create the integer iD which is a unique ID number for every customer
     and a boolean notCut which is used in the Customer waiting loop */
  
  int iD;
  boolean notCut=true;
  

  /* Constructor for the Customer */
    
  public Customer(int i) {
    iD = i;
  }
  
  public String drawWaitingRoom(int numofcust){
	  String waitingroomseats = "";
	  switch (numofcust) {
	  case 0:  waitingroomseats = "[_][_][_][_][_]";
		  	   break;
	  case 1:  waitingroomseats = "[x][_][_][_][_]";
               break;
      case 2:  waitingroomseats = "[x][x][_][_][_]";
               break;
      case 3: waitingroomseats = "[x][x][x][_][_]";
               break;
      case 4:  waitingroomseats = "[x][x][x][x][_]";
               break;
      case 5: waitingroomseats = "[x][x][x][x][x]";
               break;
	  }
	  return waitingroomseats;
	 
  }

  public void run() {   
    while (notCut) {  // as long as the customer is not cut 
    	System.out.println("\nCustomer " + this.iD + " has entered the barber shop.");
   if(!barber.hasQueuedThreads()&&!(justTookNewCust>=NumOfBarbers)){
	   ///////
	   justTookNewCust++;
	   try {
	    	//System.out.println("The barber semaphore currently has " + barber.availablePermits()+ " available permits.\nhasQueuedThreads: "+barber.hasQueuedThreads());
		      accessSeats.acquire();  //tries to get access to the chairs
		      if (numberOfFreeSeats > 0) {  //if there are any free seats
		        System.out.println("A barber is free! Customer " + this.iD + " has sat down in the barber chair.");
		        numberOfFreeSeats--;  //sitting down on a chair
		        //System.out.println("~~~There are " + numberOfFreeSeats + " in the waiting room.");
		        customers.release();  //notify the barber that there is a customer
		        accessSeats.release();  // don't need to lock the chairs anymore  
		        try {
		        barber.acquire();  // now it's this customers turn but we have to wait if the barber is busy
		        notCut = false;  // this customer will now leave after the procedure
		        this.get_haircut();  //cutting...
		        } catch (InterruptedException ex) {}
		      }   
	      else  {  // there are no free seats
	        System.out.println("There are no free seats in the waiting room. Customer " + this.iD + " has left the barbershop.");
	        accessSeats.release();  //release the lock on the seats
	        notCut=false; // the customer will leave since there are no spots in the queue left.
	      }
	     }
	      catch (InterruptedException ex) {}
	   
	   
	   
	   //barber is busy. send the customer to the waiting room.
   }else{
    	try {
    	//System.out.println("The barber semaphore currently has " + barber.availablePermits()+ " available permits.\nhasQueuedThreads: "+barber.hasQueuedThreads());
	      accessSeats.acquire();  //tries to get access to the chairs
	      if (numberOfFreeSeats > 0) {  //if there are any free seats
	        System.out.println("There are no free barbers. Customer " + this.iD + " sat down in the waiting room.");
	        WaitingRoomList.add(this.iD);
	        numberOfFreeSeats--;  //sitting down on a chair
	        System.out.println("~~~There are " + numberOfFreeSeats + " free seats in the waiting room.\n~~~The waiting room looks like this: "+drawWaitingRoom(5-numberOfFreeSeats));
	        System.out.println("~~~The customers in the waiting room: "+WaitingRoomList);

	        customers.release();  //notify the barber that there is a customer
	        accessSeats.release();  // don't need to lock the chairs anymore  
	        try {
	        barber.acquire();  // now it's this customers turn but we have to wait if the barber is busy
	        notCut = false;  // this customer will now leave after the procedure
	        this.get_haircut();  //cutting...
	        } catch (InterruptedException ex) {}
	      }   
      else  {  // there are no free seats
        System.out.println("There are no free seats in the waiting room. Customer " + this.iD + " has left the barbershop.");
        accessSeats.release();  //release the lock on the seats
        notCut=false; // the customer will leave since there are no spots in the queue left.
      }
     }
      catch (InterruptedException ex) {}
    }
    }
  }

  /* this method will simulate getting a hair-cut */
  
  public void get_haircut(){
    System.out.println("Customer " + this.iD + " is getting his hair cut.");
    try {
    sleep(5050);
    //System.out.println("Customer" + this.iD + "'s haircut is finished. He has left the barber shop.");
    justTookNewCust--;
    } catch (InterruptedException ex) {}
  }

}

 
/* THE BARBER THREAD */


class Barber extends Thread {
  
	String bname;
	
  public Barber(String barbername) {
	 bname = barbername;
  }
  

  public void run() {
	System.out.println("Barber " + bname + " has arrived at work.");
    while(true) {  // runs in an infinite loop
      try {
      customers.acquire(); // tries to acquire a customer - if none is available he goes to sleep
      accessSeats.release(); // at this time he has been awaken -> want to modify the number of available seats
        numberOfFreeSeats++; // one chair gets free
        WaitingRoomList.remove(0);
        
    barber.release();  // the barber is ready to cut
    System.out.println("Barber "+this.bname+" is ready to cut hair");
      accessSeats.release(); // we don't need the lock on the chairs anymore
      this.cutHair();  //cutting...
    } catch (InterruptedException ex) {}
    }
  }

    /* this method will simulate cutting hair */
   
  public void cutHair(){
    System.out.println("Barber " +this.bname+ " is cutting hair.");
    try {
      sleep(6050);
      //barber.release();
     // System.out.println("Barber "+this.bname+" has finished cutting hair and is ready to cut a new customer.");
      
    } catch (InterruptedException ex){ }
  }
}       
  
  /* main method */

  public static void main(String args[]) {
    
    SleepingBarber barberShop = new SleepingBarber();  //Creates a new barbershop
   for(int i=0;i<NumOfBarbers;i++){
	   WaitingRoomList.add(0); //Add padding to the waiting room for each barber chair
   }
    
    barberShop.start();  // Let the simulation begin
  }

  public void run(){   
   Barber giovanni = new Barber("Giovanni");  //Giovanni is the best barber ever 
   Barber tim = new Barber("Tim");
   giovanni.start();  //Ready for another day of work
   tim.start();


   /* This method will create new customers for a while */
    
   for (int i=1; i<21; i++) {
     Customer aCustomer = new Customer(i);
     aCustomer.start();
     try {
       sleep(2000);
     } catch(InterruptedException ex) {};
   }
  } 
}