package AntColony;
import java.util.ArrayList;

enum ResourceType 
{
	OS,
	S3Storage,
	DBService,
	UNKNOWN;
	
	
	public static ResourceType getResourceType(String type) {
		if(type.equalsIgnoreCase(OS.name()))
			return OS;
		else if(type.equalsIgnoreCase(S3Storage.name()))
			return S3Storage;
		else if(type.equalsIgnoreCase(DBService.name()))
			return DBService;
		else
			return UNKNOWN;
	}
}

enum OSType 
{
	SUSELINUX,
	REDHATLINUX,
	AMAZONLINUX,
	WINDOWS,
	UBUNTU,
	UNKNOWN;
	
	public static OSType getOSType(String type) {
		if(type.equalsIgnoreCase(SUSELINUX.name()))
			return SUSELINUX;
		else if(type.equalsIgnoreCase(WINDOWS.name()))
			return WINDOWS;
		else if(type.equalsIgnoreCase(AMAZONLINUX.name()))
			return AMAZONLINUX;
		else if(type.equalsIgnoreCase(UBUNTU.name()))
			return UBUNTU;
		else if(type.equalsIgnoreCase(REDHATLINUX.name()))
			return REDHATLINUX;
		else
			return UNKNOWN;			
	}
}

enum DBServiceType 
{
	MONGODB,
	CASSANDRA,
	COUCHDB,
	REDIS,
	AmazonS35MB,
	UNKNOWN;
	
	public static DBServiceType getDBServiceType(String type) {
		if( type.equalsIgnoreCase(MONGODB.name()))
			return MONGODB;
		else if( type.equalsIgnoreCase(CASSANDRA.name()))
			return CASSANDRA;
		else if( type.equalsIgnoreCase(COUCHDB.name()))
			return COUCHDB;
		else if( type.equalsIgnoreCase(REDIS.name()))
			return REDIS;
		else 
			return UNKNOWN;
	}
}



class Resource
{
	private int regionID;	
	private ArrayList<ResourcePair> resourceList;
	boolean isResourceAvailable;
	
	public static class ResourcePair
	{
		private ResourceType type;
		private String value;
		
		ResourcePair(String resourceType, String resourceValue)
		{
			type = ResourceType.getResourceType(resourceType);
			value = resourceValue;
		}
		
		public String toString()
		{
	    	String s = "\n\tResource.type:" + type;
	    	s += ", Resource.value:" + value;
	    	return s;
		}
	}
	
	Resource(String regionIDString, String resourceType, String resourceValue)
	{
		regionID = Integer.parseInt(regionIDString);
		resourceList = new ArrayList<ResourcePair>();
		resourceList.add(new ResourcePair(resourceType, resourceValue));
		isResourceAvailable = true;
	}
	
	Resource()
	{
		regionID = 0;
		resourceList = new ArrayList<ResourcePair>();
		isResourceAvailable = true;
	}
	
	Resource(String regionIDString)
	{
		regionID = Integer.parseInt(regionIDString);
		resourceList = new ArrayList<ResourcePair>();
		isResourceAvailable = true;
	}
	
	void setRegionID(String id)
	{
		regionID = Integer.parseInt(id);
	}
	
	int getRegionID()
	{
		return regionID;
	}
	
	ArrayList<ResourcePair> getResourceList()
	{
		return resourceList;
	}
	
	boolean isResourceAvailable()
	{
		return isResourceAvailable;
	}
	
	void setResourceAvailable(boolean availability){
		isResourceAvailable = availability;
	}
	
	void addResource(String resourceType, String resourceValue)
	{
		resourceList.add(new ResourcePair(resourceType, resourceValue));
	}
	
	void deleteResource()
	{
		resourceList.clear();
	}
	
	boolean isItMatchingResource(Resource requestingResource)
	{
		boolean isItMatch = false;
		int requestingResourceListSize = requestingResource.getResourceList().size();
		int myResourceListSize = resourceList.size();
		
		if(this.isResourceAvailable() == false || myResourceListSize < requestingResourceListSize)
			return isItMatch; //my resource is not avilable or adequate
			
		if( requestingResourceListSize > 0 )
			isItMatch = true;  
		//get all resources in requesting 
		for(int count=0; count <requestingResourceListSize && isItMatch == true; count++)
		{
			ResourcePair requestingResourcePair = requestingResource.getResourceList().get(count);
			
			for(int count1=0; count1 < myResourceListSize && isItMatch == true; count1++)
			{
				ResourcePair myResourcePair = resourceList.get(count1);
				if(myResourcePair.type == requestingResourcePair.type)
				{
					if(requestingResourcePair.type == ResourceType.OS)
					{
						
						OSType myOSType = OSType.getOSType(myResourcePair.value);
						OSType requestedOSType = OSType.getOSType(requestingResourcePair.value);
						if(myOSType == requestedOSType)
							isItMatch = true;
						else 
							isItMatch = false;
					}
					/*else if(requestingResourcePair.type == ResourceType.S3Storage)
					{
						int myFreeStroage = Integer.parseInt(myResourcePair.value);
						int requestedStroage = Integer.parseInt(requestingResourcePair.value);
						if(requestedStroage <= myFreeStroage)
							isItMatch = true;
						else 
							isItMatch = false;
					}
					else if(requestingResourcePair.type == ResourceType.DBService)
					{
						DBServiceType myDBType = DBServiceType.getDBServiceType(myResourcePair.value);
						DBServiceType requestedDBType = DBServiceType.getDBServiceType(requestingResourcePair.value);
						if(myDBType == requestedDBType)
							isItMatch = true;
						else 
							isItMatch = false;
					}*/
					else
					{
							isItMatch = false;
					}
				}
			}	
			
		}
		
		return isItMatch;
	}
	
	boolean matchMobileRequestWithResourceList(Resource resource,MobileRequest mobRequest,int count)
	{
		boolean retValue = false;
		 boolean isItMatch = false;
		
		if(!resource.isResourceAvailable())
		{
			return retValue;
		}
		int requestingResourceListSize = resource.getResourceList().size();

		//int numResourcePair = resourceList.size();
		
		
		
		for(int i = 0; i < requestingResourceListSize;i++)
		{
			ResourcePair requestingResourcePair = resource.getResourceList().get(i);
			ResourcePair myResourcePair = resourceList.get(i);
			//System.out.println("resource list: "+resourceList.get(i));
			
			if(myResourcePair.type == requestingResourcePair.type)
			{
				if(requestingResourcePair.type == ResourceType.OS)
				{
					OSType myOSType = OSType.getOSType(myResourcePair.value);
					//System.out.println("mob req OS: "+mobRequest.getCpuQuantity().getOperatingSystem());
					OSType requestedOSType = OSType.getOSType(mobRequest.getCpuQuantity().getOperatingSystem());
					if(myOSType == requestedOSType)
					{
						isItMatch = true;
						//System.out.println("match!!");
						break;
					}
					else 
					{
						isItMatch = false;
					//System.out.println("Mismatch!!");
					}
					//System.out.println("my OS type is "+myOSType+" and request type is " +requestedOSType);
				}
				/* TODO
				else if(requestingResourcePair.type == ResourceType.S3Storage)
				{
					int myFreeStroage = Integer.parseInt(myResourcePair.value);
					int requestedStroage = (mobRequest.getStorageQuantity());
					if(requestedStroage <= myFreeStroage)
						isItMatch = true;
					else 
						isItMatch = false;
				}
				
				else if(requestingResourcePair.type == ResourceType.DBService)
				{
					DBServiceType myDBType = DBServiceType.getDBServiceType(myResourcePair.value);
					DBServiceType requestedDBType = DBServiceType.getDBServiceType(requestingResourcePair.value);
					if(myDBType == requestedDBType)
						isItMatch = true;
					else 
						isItMatch = false;
				}
				*/
				else
				{
						isItMatch = false;
				}
		}
		
		
		}
		retValue = isItMatch;
		return retValue;
	}
	public String toString()
	{
	    String s = "";
	    //s += "==========================\n";
	    s += "\nResource.regionID:" + Integer.toString(regionID);
	    s += "\nResource.isAvailable:" + isResourceAvailable;
	    for(int count=0; count < resourceList.size(); count++)
	    {
	    	ResourcePair resource = resourceList.get(count);
	    	s += resource;
	    }
	    return s;
	}
}
	

