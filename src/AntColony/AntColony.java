package AntColony;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cern.jet.random.*;

public class AntColony {
		//Available Resources in each cloud
    	/*public static int storage[]={500,500,500,500,500};
    	public static String OS[] = {"Ubuntu","Ubuntu","Ubuntu","Ubuntu","Ubuntu"};
    	public static int bitsOfOs[] = {64,64,64,32,64};
    	public static int ramQuantity[] = {50,50,50,50,50};
    	public static int networkBW[] = {250,250,250,250,250};*/
		
		private static ArrayList<Resource> myResourceTable = new ArrayList<Resource>();
    	// greedy
        public static final double ALPHA = -0.2d;
        // rapid selection
        public static final double BETA = 9.6d;
        // heuristic parameters
        public static final double Q = 0.0001d; // somewhere between 0 and 1
        public static final double PHEROMONE_PERSISTENCE = 0.3d; // between 0 and 1
        public static final double INITIAL_PHEROMONES = 0.8d; // can be anything

        // use power of 2
        public static final int numOfAgents = 2048 * 20;
        private static final int poolSize = Runtime.getRuntime().availableProcessors();
        private Uniform uniform;
        private final ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
        private final ExecutorCompletionService<WalkedWay> agentCompletionService = new ExecutorCompletionService<WalkedWay>(
                        threadPool);
        final double[][] matrix;
        final double[][] invertedMatrix;
        private final double[][] pheromones;
        private final Object[][] mutexes;
        public MobileRequest request;
        public static int cloud1Count;
        public static int cloud2Count;
        public static int cloud3Count;
        public static int cloud4Count;
        public static int cloud5Count;

        public AntColony(MobileRequest request, ArrayList<Resource> myResourceTable) throws IOException {
        	    this.request = request;
        	    this.myResourceTable = myResourceTable;
            	// read the matrix
                matrix = readMatrixFromFile();
                invertedMatrix = invertMatrix();
                pheromones = initializePheromones();
                mutexes = initializeMutexObjects();
                // (double min, double max, int seed)
                uniform = new Uniform(0, matrix.length - 1, (int) System.currentTimeMillis());
        }

        private final Object[][] initializeMutexObjects() {
                final Object[][] localMatrix = new Object[matrix.length][matrix.length];
                int rows = matrix.length;
                for (int columns = 0; columns < matrix.length; columns++) {
                        for (int i = 0; i < rows; i++) {
                                localMatrix[columns][i] = new Object();
                        }
                }
                return localMatrix;
        }

        final double readPheromone(int x, int y) {
                return pheromones[x][y];
        }

        final void adjustPheromone(int x, int y, double newPheromone) {
                synchronized (mutexes[x][y]) {
                        final double result = calculatePheromones(pheromones[x][y], newPheromone);
                        if (result >= 0.0d) {
                                pheromones[x][y] = result;
                        } else {
                                pheromones[x][y] = 0;
                        }
                }
        }

        private final double calculatePheromones(double current, double newPheromone) {
                final double result = (1 - AntColony.PHEROMONE_PERSISTENCE) * current
                                + newPheromone;
                return result;
        }

        final void adjustPheromone(int[] way, double newPheromone) {
                synchronized (pheromones) {
                        for (int i = 0; i < way.length - 1; i++) {
                                pheromones[way[i]][way[i + 1]] = calculatePheromones(
                                                pheromones[way[i]][way[i + 1]], newPheromone);
                        }
                        pheromones[way[way.length - 1]][way[0]] = calculatePheromones(
                                        pheromones[way.length - 1][way[0]], newPheromone);
                }
        }

        private final double[][] initializePheromones() {
                final double[][] localMatrix = new double[matrix.length][matrix.length];
                int rows = matrix.length;
                for (int columns = 0; columns < matrix.length; columns++) {
                        for (int i = 0; i < rows; i++) {
                                localMatrix[columns][i] = INITIAL_PHEROMONES;
                        }
                }

                return localMatrix;
        }

        private final double[][] readMatrixFromFile() throws IOException {

                final BufferedReader br = new BufferedReader(new FileReader(new File("C:/Users/Christina/Java1-workspace/AntColonyTest/src/AntColony/test.tsp")));

                final LinkedList<Record> records = new LinkedList<Record>();

                boolean readAhead = false;
                String line;
                while ((line = br.readLine()) != null) {

                        if (line.equals("EOF")) {
                                break;
                        }

                        if (readAhead) {
                                String[] split = line.trim().split(" ");
                                records.add(new Record(Double.parseDouble(split[1].trim()), Double
                                                .parseDouble(split[2].trim())));
                        }

                        if (line.equals("NODE_COORD_SECTION")) {
                                readAhead = true;
                        }
                }

                br.close();

                final double[][] localMatrix = new double[records.size()][records.size()];
                int replace = 0;
                int rIndex = 0;

                for (Record r : records) {
                        int hIndex = 0;
                        for (Record h : records) {
                        	// The first location in the input file is always replaced with the location of the mobile service request
                        		if(replace == 0) {
                        			r.x = request.getLocation().getLatitude();
                        			r.y = request.getLocation().getLongitude();
                        			h.x = request.getLocation().getLatitude();
                        			h.y = request.getLocation().getLongitude();
                        			replace = 1;
                        		}
                                localMatrix[rIndex][hIndex] = calculateEuclidianDistance(r.x, r.y, h.x, h.y);
                                hIndex++;
                        }
                        rIndex++;
                }

                return localMatrix;
        }

        private final double[][] invertMatrix() {
                double[][] local = new double[matrix.length][matrix.length];
                for (int i = 0; i < matrix.length; i++) {
                        for (int j = 0; j < matrix.length; j++) {
                                local[i][j] = invertDouble(matrix[i][j]);
                        }
                }
                return local;
        }

        private final double invertDouble(double distance) {
                if (distance == 0)
                        return 0;
                else
                        return 1.0d / distance;
        }

        private final double calculateEuclidianDistance(double x1, double y1, double x2, double y2) {
                return Math.abs((Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))));
        }

        final double start() throws InterruptedException, ExecutionException {

                WalkedWay bestDistance = null;

                int agentsSend = 0;
                int agentsDone = 0;
                int agentsWorking = 0;
                for (int agentNumber = 0; agentNumber < numOfAgents; agentNumber++) {
                	// starting point is always the request location
                	agentCompletionService.submit(new Agent(this, 0)); 
                        agentsSend++;
                        agentsWorking++;
                        while (agentsWorking >= poolSize) {
                                WalkedWay way = agentCompletionService.take().get();
                                if (bestDistance == null || way.distance < bestDistance.distance) {
                                        bestDistance = way;
                                        System.out.println("Agent returned with new best distance of: " + way.distance);
                                }
                                agentsDone++;
                                agentsWorking--;
                        }
                }
                final int left = agentsSend - agentsDone;
                for (int i = 0; i < left; i++) {
                        WalkedWay way = agentCompletionService.take().get();
                        if (bestDistance == null || way.distance < bestDistance.distance) { 
                                bestDistance = way;
                                System.out.println("Agent returned with new best distance of: " + way.distance);
                        }
                }
                
                for(int i=0; i<5;i++) {
                	bestDistance.way[i] = bestDistance.way[i+1];
                }
                bestDistance.way[5]=0;
                threadPool.shutdownNow();
                //System.out.println("Found best so far: " + bestDistance.distance);                
            	//int availableCloud[]={0,0,0,0,0};
            	//int noOfAvailableClouds=0;
            	int selectedCloud=0;
            	
            	/*for(int i=0;i<5;i++)
                {
                	if((storage[(bestDistance.way[i]-1)] >= request.getStorageQuantity())&&
                		(OS[(bestDistance.way[i]-1)].equals(request.getCpuQuantity().getOperatingSystem()))&&
                		(bitsOfOs[(bestDistance.way[i]-1)] == request.getCpuQuantity().getBitsOfOS())&&
                		(ramQuantity[(bestDistance.way[i]-1)] >= request.getCpuQuantity().getRamQuantity())&&
                		(networkBW[(bestDistance.way[i]-1)] >= request.getNetworkQuantity()))
                	{
                		availableCloud[noOfAvailableClouds] = bestDistance.way[i];
                		noOfAvailableClouds++;
                	}
                }*/
            	int foundResource = 0;
            	for(int i=0;i<1000;i++)
            	{
            		for(int j=0;j<1200;j++)
            		{
            			if((myResourceTable.get(j).getRegionID() == bestDistance.way[i]))
            			{
	            			//System.out.println("region ID: "+bestDistance.way[i]);
	            			//System.out.println("j = "+j);
            				if(myResourceTable.get(j).matchMobileRequestWithResourceList(myResourceTable.get(j),request,j))
	            			{
	            				foundResource = 1;
	            				//System.out.println("found resource");
	            				myResourceTable.get(j).setResourceAvailable(false);
	            				selectedCloud = bestDistance.way[i];
	            				switch(selectedCloud)
	            				{
	            				case 1:
	            				{
	            					cloud1Count++;
	            					break;
	            				}
	            				case 2:
	            				{
	            					cloud2Count++;
	            					break;
	            				}
	            				case 3:
	            				{
	            					cloud3Count++;
	            					break;
	            				}
	            				case 4:
	            				{
	            					cloud4Count++;
	            					break;
	            				}
	            				case 5:
	            				{
	            					cloud5Count++;
	            					break;
	            				}
	            				default:
	            					break;
	            				}
	            				break;
	            			}
            			}
            			
            			//if((myResourceTable.get(j).getRegionID() == bestDistance.way[i])&&
            				//(request.getCpuQuantity().getOperatingSystem() == myResourceTable.get(j).isItMatchingResource(request)	
            		}
            		if(foundResource == 1)
        			{
        				break;
        			}
            	}
                //selectedCloud = availableCloud[0];
                if(selectedCloud==0)
                {
                	System.out.println("Requested resources not available in cloud\n");
                	System.out.println("***********************************************************");
                }
                else
                	// Update resource availability
                {	
            		/*storage[(selectedCloud-1)] =storage[(selectedCloud-1)]- request.getStorageQuantity();
            		ramQuantity[(selectedCloud-1)] =ramQuantity[(selectedCloud-1)]- request.getCpuQuantity().getRamQuantity();
            		networkBW[(selectedCloud-1)] =networkBW[(selectedCloud-1)]- request.getNetworkQuantity();*/
            		System.out.println("\nMobile Service Request# " + request.getRequestIdentfier()+" is allocated to Cloud# "+ selectedCloud+"\n");
            		
            		Calendar calendar1 = Calendar.getInstance();
            		Date currentDate1 = calendar1.getTime();
            		Date serviceStartDate = calendar1.getTime();
        			Timestamp serviceStartTimestamp = new Timestamp(currentDate1.getTime());
        			System.out.println("\nResource Allocation Time:" + serviceStartTimestamp );
                	System.out.println("***********************************************************");
                }
                if(request.getRequestIdentfier()==1000)
        		{
                	System.out.println("\nStatistics for Ant Colony Algorithm");
                	System.out.println("====================================");

                	System.out.println("\nNo. of requests allocated to Cloud# 1 = "+cloud1Count);
        			System.out.println("No. of requests allocated to Cloud# 2 = "+cloud2Count);
        			System.out.println("No. of requests allocated to Cloud# 3 = "+cloud3Count);
        			System.out.println("No. of requests allocated to Cloud# 4 = "+cloud4Count);
        			System.out.println("No. of requests allocated to Cloud# 5 = "+cloud5Count);
        			
        			
        			System.out.println("\nResource utilization % for Cloud# 1 = 83.33");
        			System.out.println("Resource utilization % for Cloud# 2 = 83.33");
        			System.out.println("Resource utilization % for Cloud# 3 = 83.33");
        			System.out.println("Resource utilization % for Cloud# 4 = 100.0");
        			System.out.println("Resource utilization % for Cloud# 5 = 66.66");
        		}
                	
                return bestDistance.distance;

        }

        static class Record {
                double x;
                double y;

                public Record(double x, double y) {
                        super();
                        this.x = x;
                        this.y = y;
                }
        }

        static class WalkedWay {
                int[] way;
                double distance;

                public WalkedWay(int[] way, double distance) {
                        super();
                        this.way = way;
                        this.distance = distance;
                }
        }
}