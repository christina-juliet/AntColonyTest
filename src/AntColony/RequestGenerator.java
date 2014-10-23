package AntColony;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

//import randomRequestGenerator.MobileRequest;

public class RequestGenerator {
	
	//can be set to maximum number of requests needed multiples of 5 (like) 10 / 100 / 1000 
	static int maxNumberOfRequests = 10;
	
	static int loopCount=1;
	static int randomNumberCount = 1;
	
	//can be set to 1 / 10 / 24 corresponding to time frame hours
	//static int timeDelayFactor = 1;
	
	//static Timer timer = new Timer();
	static ArrayList<Integer> randomNumbersList = new ArrayList<Integer>();   
	
	//static ArrayList<MobileRequest> mobileRequestList = new ArrayList<MobileRequest>(); <christina>
	
    //static class Task extends TimerTask {
	static class Task {  
        public void run() {
        	
            //variable to store selected random number
            int selectedRandomNumber;
            

            int[]  Storage1, Network1;
            String[]  CPU1;
            CPU1 = new String[]{"Ubuntu,64,1", "Ubuntu,32,1", "Ubuntu,64,1", "Ubuntu,64,1", "Ubuntu, 64,1"};
            //Storage in MB
            Storage1 = new int[]{5,40,25,20,50};
            //NW is MBPS
            Network1 = new int[]{ 20,40,50,25,15};
            //Location in latitude and longitude <christina>
            String[] Location1;
            Location1 = new String[]{"33.0,84.0", "47.0,122.0", "39.0,119.0", "39.0,104.0", "42.0,71.0"};
                        
            
            //start the loop to generate 10 requests in random time intervals
                     
            //if (loopCount <= maxNumberOfRequests){
              while(loopCount <= maxNumberOfRequests){
            	
            	//generating random numbers without duplicates
            	if (randomNumberCount>5) { 
            		randomNumberCount = 1; 
            		randomNumbersList.clear();
            	}
            	
            	if (randomNumberCount==1) {
            		
            		for(int i = 1; i <= 5; i++)  {     
            			randomNumbersList.add(i);  
            		} 
            		
        			Collections.shuffle(randomNumbersList);
        	   	}
        		     
        		 
            	selectedRandomNumber = (int) randomNumbersList.get((randomNumberCount-1));
        		        		
        		//delay is calculated in milliseconds so multiplied by 1000
        		//int delay = ((selectedRandomNumber) * 1000)*timeDelayFactor;
        		
        		//printing request details, delay time and request generated time
                System.out.println("Request Number --> "+ loopCount +"----Random Number-->" +selectedRandomNumber );
                System.out.println("CPU: "+CPU1[selectedRandomNumber-1]+"\nStorage: "+Storage1[selectedRandomNumber-1]+"\nBandwidth: "+Network1[selectedRandomNumber-1]+"\nLocation: "+Location1[selectedRandomNumber-1]);
                //System.out.println("The time delay for next request is "+delay/1000 + " Seconds.");
                //timer.schedule(new Task(), delay);
                System.out.println("Request generated at " +new Date()+"\n");
                
                
                //forming the requestArrayList
                
                //Splitting the String;
                
                String CPUParameter = CPU1[selectedRandomNumber-1];
                //splitting to get OS type
                String OSType= CPUParameter.substring(0,CPUParameter.indexOf(','));
                CPUParameter=CPUParameter.substring(CPUParameter.indexOf(',')+1);
                //splitting to get OS bit size
                String OSBit= CPUParameter.substring(0,CPUParameter.indexOf(','));
                int OSBit1 = Integer.parseInt(OSBit.trim());
                //splitting to get Ram Size
                String OSRam=CPUParameter.substring(CPUParameter.indexOf(',')+1);
                int OSRam1 = Integer.parseInt(OSRam.trim());
                
                String LocationParameter = Location1[selectedRandomNumber-1];
                //splitting to get latitude
                String latitude = LocationParameter.substring(0,LocationParameter.indexOf(','));
                double latitude1 = Double.parseDouble(latitude.trim());
                //splitting to get longitude
                String longitude= LocationParameter.substring(LocationParameter.indexOf(',')+1);
                double longitude1 = Double.parseDouble(longitude.trim());                
                
                //forming the mobile request and adding the request to the mobileRequestList
                MobileRequest request = new MobileRequest(selectedRandomNumber, Storage1[selectedRandomNumber-1], OSType, OSBit1, OSRam1, Network1[selectedRandomNumber-1], latitude1, longitude1);
                
                /*request.setRequestIdentfier(selectedRandomNumber);
                request.setStorageQuantity(Storage1[selectedRandomNumber-1]);
                cpu.setOperatingSystem(OSType);
                cpu.setBitsOfOS(OSBit1);
                cpu.setRamQuantity(OSRam1);
                request.setNetworkQuantity(Network1[selectedRandomNumber-1]);
                loc.setLatitude(latitude1);
                loc.setLongitude(longitude1);
                // request has to be sent to MQ
                // Displaying the request details
                System.out.println("MobileRequest# "+selectedRandomNumber+":");
                //System.out.println("Storage: " +Storage1[selectedRandomNumber-1]+"\nOS Type: "+ OSType+"\nOS Bit: "+ OSBit1+"\nOS Ram: "+ OSRam1+"\nNetwork: "+ Network1[selectedRandomNumber-1]+"\nLatitude: "+ latitude1+"\nLongitude: "+ longitude1);
                System.out.println("Storage: " +request.getStorageQuantity()+"\nOS Type: "+request.getCpuQuantity().getOperatingSystem() +"\nOS Bit: "+ request.getCpuQuantity().getBitsOfOS()+"\nOS Ram: "+ request.getCpuQuantity().getRamQuantity()+"\nNetwork: "+ request.getNetworkQuantity()+"\nLatitude: "+ request.getLocation().getLatitude()+"\nLongitude: "+ request.getLocation().getLongitude());
                //incrementing the static counters*/
                //System.out.println("Storage: " +request.getStorageQuantity()+"\nOS Type: "+request.getCpuQuantity().getOperatingSystem() +"\nOS Bit: "+ request.getCpuQuantity().getBitsOfOS()+"\nOS Ram: "+ request.getCpuQuantity().getRamQuantity()+"\nNetwork: "+ request.getNetworkQuantity()+"\nLatitude: "+ request.getLocation().getLatitude()+"\nLongitude: "+ request.getLocation().getLongitude());
                try {
                long start = System.currentTimeMillis();
                AntColony AntColony = new AntColony(request);
                AntColony.start();
                }
                catch(IOException ioe)
                {
                	System.out.println("IOException: "+ioe);
                }
                catch(InterruptedException ie)
                {
                	System.out.println("Interrupted Exception: "+ie);
                }
                catch(ExecutionException ee)
                {
                	System.out.println("Execution Exception: "+ee);
                }
                loopCount++;
                randomNumberCount++;
                 
            }
            //else {
            	System.out.println("\n---All " + maxNumberOfRequests +" Requests has been created--- ");
            	//timer.cancel();
            //}    		            	            
        }
    }
    

    public static void main(String[] args) throws Exception {
    	
        new Task().run();
    	
    }
}