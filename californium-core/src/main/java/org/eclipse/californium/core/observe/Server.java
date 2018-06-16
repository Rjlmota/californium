package org.eclipse.californium.core.observe;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Server{
	
	 public InetAddress IP;
	 public Timestamp last_datetime;
	 public int last_con;
	 public int rec_msgs = 0;
	 public int last_mid = 0;
	 public int duplicates = 0;
	 public int lost_msgs = 0;
	 public int isHarvesting = 0;
	 
	 public List <Integer> last_msgs =new ArrayList <Integer>();
	 
	 public Server(InetAddress IP_AD, Timestamp last, int harvesting) {
		 IP = IP_AD;
		 last_datetime = last;
		 isHarvesting = harvesting;
	 }
	 
	 public float getLoss() {
		 if(rec_msgs > 0 && lost_msgs > 0) {
			 return lost_msgs/rec_msgs + lost_msgs;
		 }
		 else {
			 return 0;
		 }
	 }
	 

	
}
	
