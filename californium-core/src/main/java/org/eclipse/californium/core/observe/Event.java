package org.eclipse.californium.core.observe;


import org.eclipse.californium.core.observe.Fuzzy;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.californium.core.observe.Server;

import org.eclipse.californium.core.coap.Message;
public class Event {
	public static Set <InetAddress> observersArray = new HashSet <InetAddress>();
	private static int numberOfObservers = 0;
	public static List <Server> servers = new ArrayList <Server>();

	public static double harvestingRate() {
		int harvestingNodes = 0;
		if(numberOfObservers > 0) {
			for(Server server : servers) 
				if(observersArray.contains(server.IP))
					if (server.isHarvesting == 1) 
						harvestingNodes++;
			System.out.println(harvestingNodes + " " + numberOfObservers);
			return (harvestingNodes/numberOfObservers);
		}else 
			return 0;
	}
	
	public static void printStats() {
		for(int i = 0; i < servers.size(); i++) {
			System.out.println("Mensagens recebidas por " +  Event.servers.get(i).IP + " : " + Event.servers.get(i).rec_msgs);
			System.out.println("Duplicadas por : " + Event.servers.get(i).IP + " : " + Event.servers.get(i).duplicates);
			System.out.println("Mensagens perdidas por: " + Event.servers.get(i).IP + " : " + Event.servers.get(i).lost_msgs);
		}
	}
	
	
	private static void checkMessage (int current_mid,  Server server) {
		//If current MID is higher than last MID, the message is new and should be counted.
		if(server.last_msgs.size() >= 10) {
			for(int i = 0; i < server.last_msgs.size(); i++) 
				if(current_mid == server.last_msgs.get(i)) 
					server.duplicates++;
					server.last_msgs.remove(0);
		}else 
			server.last_msgs.add(current_mid);
		
		if(current_mid > server.last_mid)
			server.rec_msgs++;
	
		//If current MID is not one step ahead of the last_mid, than a message was lost.
		else if(((server.last_mid +1) < current_mid)) {
			//Defines how many messages were lost.
			System.out.println(server.last_mid + " - " + current_mid);
			int lap = current_mid - server.last_mid +1;
			System.out.println("LAP:" + lap);
			server.lost_msgs += lap;
		}
		
	}
	
	
	public static void updateData(Timestamp last_Time, InetAddress Address, int current_mid, String payload) {
		for(int i = 0; i < servers.size(); i++) {	
			if(Address.equals(servers.get(i).IP)){
				servers.get(i).last_datetime = last_Time;
				
				checkMessage(current_mid, servers.get(i));	

				int eventClass = Character.getNumericValue(payload.charAt(1));
				String output = Fuzzy.start(numberOfObservers, servers.get(i).getLoss(), eventClass, harvestingRate());
				System.out.println("INPUT--> " + numberOfObservers + " " + servers.get(i).getLoss() + " " + eventClass + " " + harvestingRate());
				System.out.println("OUTPUT--> " + output);
				servers.get(i).last_mid = current_mid;
				currentEventStats();
				return;
			}
		}
		//Server object was not found and a new Object is created to represent it.
		servers.add(new Server(Address, last_Time, Character.getNumericValue(payload.charAt(0))));
		currentEventStats();
	}
	
	
	private static int lastEvent() {
		int index = 0;
		long higher = 0;
		for (int i = 0; i < servers.size(); i++) 
			if(servers.get(i).last_datetime.getTime() > higher) { 
				higher = servers.get(i).last_datetime.getTime();
				index = i;
			}
		return index;
	}
		
	private static void currentEventStats() {
		numberOfObservers = 0;
		observersArray.clear();
		if(servers.size() < 2){
			System.out.println(numberOfObservers + " mote is observing the event \n");
			return;
		}
		for(int i = 0; i < servers.size(); i++)
				if(Math.abs(servers.get(lastEvent()).last_datetime.getTime() - servers.get(i).last_datetime.getTime()) < 100000) {
					numberOfObservers++;
					observersArray.add(servers.get(i).IP);
				}
		System.out.println(numberOfObservers + " mote(s) observing the event \n");
		printStats();
	}
	
	public static String next_con(Message message) {
		List<InetAddress> obs_list = new ArrayList<>(observersArray);
		int next = 0;
		for(int i = 0; i < obs_list.size(); i++){ 
			if (message.getSource().equals(obs_list.get((i)))) {
				if(message.isConfirmable()) 
					servers.get(i).last_con =  message.getMID();
				
				next = servers.get(i).last_con + numberOfObservers + i;
			
				int event_Class = Character.getNumericValue(message.getPayloadString().charAt(0));
				
				String output = Fuzzy.start(numberOfObservers, servers.get(i).getLoss(), event_Class, harvestingRate());
				System.out.println("INPUT----> + " + numberOfObservers +" " + servers.get(i).getLoss() +" " + event_Class +" " + harvestingRate());
				System.out.println("OUTPUT_RECIEVED: " + output);				
				System.out.println("Ultima CON foi: " + servers.get(i).last_con + " Proxima CON deveria ser: " + next);
			
				return output;
			}
				
		}
		return null;
	}
}
