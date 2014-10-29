package AntColony;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class RequestGenerator {	
	//Set the maximum number of requests  
	static int maxNumberOfRequests = 1000;
	static int loopCount=1;
	static int randomNumberCount = 1;
	static ArrayList<Integer> randomNumbersList = new ArrayList<Integer>();   
	static ArrayList<Resource> myResourceTable = new ArrayList<Resource>();
	
	static void populateResourceTable(String fileName) throws Exception
	{
		System.out.println("populating resource table\n");
		FileInputStream in = new FileInputStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		if(myResourceTable == null)
			myResourceTable = new ArrayList<Resource>();
		
		String strLine;
		while((strLine = br.readLine())!= null)
		{
			String tokens[] = strLine.split(",");
			
			//make sure there are 3 or entries, and total count is odd number, so we can construct resource pair
			int currentToken = 0;
			if( (tokens.length >= 3) && (tokens.length -1) % 2 == 0) 
			{
				Resource resource = new Resource(tokens[currentToken++]);
				while(currentToken < tokens.length)
					resource.addResource(tokens[currentToken++], tokens[currentToken++]);
				myResourceTable.add(resource);
			}
			else
			{
				//invalid entry, drop it...
				System.out.println("There are less than 3 entries, so dropping resource -->" + strLine);
			}
		}
		
		br.close();
	}
	
	static class Task {  
        public void run() {        	
            //variable to store selected random number
            int selectedRandomNumber;           
          //list of resources available
            String[] OS, S3Storage, DBService;
            OS = new String[]{"RedHatLinux", "Ubuntu", "SUSELinux", "Windows", "AmazonLinux"};
            S3Storage = new String[]{"1024", "2048", "4096", "1024", "8192"};
            DBService = new String[]{"AmazonS35MB", "AmazonS35MB", "AmazonS35MB", "AmazonS35MB", "AmazonS35MB"};
            //Location in latitude and longitude
            String[] Location1;
            Location1 = new String[]{"33.0,84.0", "47.0,122.0", "42.0,83.0", "39.0,104.0", "42.0,71.0"};
                                    
            //start the loop to generate 5 mobile service requests
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
            		
        			//Collections.shuffle(randomNumbersList);
        	   	}        		             		 
            	selectedRandomNumber = (int) randomNumbersList.get((randomNumberCount-1));    
            	
            	Calendar calendar = Calendar.getInstance();
        		System.out.println("Request Number --> "+ loopCount +" ---OS: "+OS[selectedRandomNumber-1] + "---DBService: "+DBService[selectedRandomNumber-1]);
            	
        		//printing request details, delay time and request generated time
                System.out.println("Mobile Service Request# "+ loopCount);
                System.out.println("OS: "+OS[selectedRandomNumber-1]+"\nStorage: "+S3Storage[selectedRandomNumber-1]+"\nDBService: "+DBService[selectedRandomNumber-1]+"\nLocation: "+Location1[selectedRandomNumber-1]);
                //System.out.println("Request generated at " +new Date()+"\n");  
                

                /*String CPUParameter = CPU1[selectedRandomNumber-1];
                //splitting to get OS type
                String OSType= CPUParameter.substring(0,CPUParameter.indexOf(','));
                CPUParameter=CPUParameter.substring(CPUParameter.indexOf(',')+1);
                //splitting to get OS bit size
                String OSBit= CPUParameter.substring(0,CPUParameter.indexOf(','));
                int OSBit1 = Integer.parseInt(OSBit.trim());
                //splitting to get Ram Size
                String OSRam=CPUParameter.substring(CPUParameter.indexOf(',')+1);
                int OSRam1 = Integer.parseInt(OSRam.trim());*/
                
                String LocationParameter = Location1[selectedRandomNumber-1];
                //splitting to get latitude
                String latitude = LocationParameter.substring(0,LocationParameter.indexOf(','));
                double latitude1 = Double.parseDouble(latitude.trim());
                //splitting to get longitude
                String longitude= LocationParameter.substring(LocationParameter.indexOf(',')+1);
                double longitude1 = Double.parseDouble(longitude.trim()); 
                MobileRequest request = null;
                
                //forming the mobile request
                request = new MobileRequest(loopCount, 35, OS[selectedRandomNumber-1], 64, 1, 250, latitude1, longitude1);
                
                // The mobile service request is sent to the Ant Colony Optimization algorithm for resource allocation
                Date currentDate = calendar.getTime();
    			Timestamp requestTimestamp = new Timestamp(currentDate.getTime());
    			System.out.println("\nRequest Generation Time:" + requestTimestamp);
    			
                System.out.println("\nUsing Ant Colony Optimization ...");
                try {
                long start = System.currentTimeMillis();
                AntColony AntColony = new AntColony(request,myResourceTable);
                AntColony.start();
                
    			/*Date currentDate = calendar.getTime();
    			Timestamp requestTimestamp = new Timestamp(currentDate.getTime());
    			System.out.println("Request Generation Time:" + requestTimestamp);*/
    			
    			/*Calendar calendar1 = Calendar.getInstance();
        		Date currentDate1 = calendar1.getTime();
        		Date serviceStartDate = calendar.getTime();
    			Timestamp serviceStartTimestamp = new Timestamp(currentDate1.getTime());
    			System.out.println("\nResource Allocation Time:" + serviceStartTimestamp );
            	System.out.println("***********************************************************");*/
            	
    			
    			
    			//DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    			//Date resDate = sdf.parse(serviceStartTimestamp.toString());
    			//Date reqDate = sdf.parse(requestTimestamp.toString());
    			
    			//responseTime = resDate.getTime()-reqDate.getTime();
    			
    			//System.out.println("Service Response Time-->"+responseTime+" ms");
    			//totalResponseTime+= responseTime;
                
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
        }
    }
    

    public static void main(String[] args) throws Exception {
    	
    	long startTime = System.currentTimeMillis();
    	String fileName = "C:/Users/Christina/Java1-workspace/AntColonyTest/src/AntColony/MyResourceTable_new_1200.txt";
		populateResourceTable(fileName);
    	new Task().run();
    	long endTime = System.currentTimeMillis();
    	System.out.println("\nTime taken to allocate 1000 requests = "+(endTime-startTime)+ "ms");
    	System.out.println("\nAverage Response Time per request = "+((endTime-startTime)/1000)+ "ms");
    	
    }
}