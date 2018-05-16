package org.eclipse.californium.core.observe;

import java.net.InetAddress;
import java.sql.Timestamp;

public class Server{
	 public InetAddress IP;
	 public Timestamp last_datetime;
	 public int last_con;
	 public int rec_msgs = 0;
	 public int last_mid = 0;
	 public int duplicates = 0;
	
}
