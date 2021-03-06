package org.eclipse.californium.core.observe;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Server{
	
	 public InetAddress IP;
	 public long last_datetime;
	 public int last_con;
	 public int rec_msgs = 1;
	 public int last_mid = 0;
	 public int duplicates = 0;
	 public int lost_msgs = 0;
	 public int isHarvesting = 0;
	 
	 public boolean isObserving = false;
	 
	 public List <Integer> last_msgs =new ArrayList <Integer>();
	 public List <Integer> lateMsgs = new ArrayList <Integer>();
	 //Constructor for creating a new Instance of this Class.
	 public Server(InetAddress IP_AD, long last, int harvesting) {
		 IP = IP_AD;
		 last_datetime = last;
		 isHarvesting = harvesting;
	 }
	 
	 
	 //Calculate the current loss rate of Node.
	 public double getLoss() {
		 if(rec_msgs > 0 && lost_msgs > 0) {
			 return lost_msgs/((double)(rec_msgs + lost_msgs));
		 }
		 else {
			 return 0;
		 }
	 }
	 

	
}
	
