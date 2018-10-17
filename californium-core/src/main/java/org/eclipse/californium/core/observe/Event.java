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
	public static long LastEliminationTime = System.currentTimeMillis();
	
	
	public static double harvestingRate() {
		int harvestingNodes = 0;
		if(numberOfObservers > 0) {
			for(Server server : servers) 
				if(observersArray.contains(server.IP))
					if (server.isHarvesting == 1) 
						harvestingNodes++;
			System.out.println("HARVESTING: " + harvestingNodes + " " + numberOfObservers);
			return (harvestingNodes/(double)numberOfObservers);
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
	
	
	public static Boolean needToEliminate = false;
	
	
	public static InetAddress toEliminate() {
		double currentLoss = -1;
		 InetAddress eliminate = null;
		 int indexEliminate = 0;
		for(int i = 0; i < servers.size(); i++) {
			if(servers.get(i).getLoss() > currentLoss) {
				if(servers.get(i).isHarvesting == 0) {
					eliminate = servers.get(i).IP;
					indexEliminate = i;
				}
			}
		}
		servers.remove(indexEliminate);
		return eliminate;
	}
	
	private static void checkMessage (int current_mid,  Server server) {
		//If current MID is higher than last MID, the message is new and should be counted.
		
		
		if(server.last_mid == 0) { 
			server.last_mid = current_mid;
			return;
		}
		
		System.out.println("-------LAST MSGS----------");
		System.out.println(server.last_msgs);
		System.out.println("-----------------");
		
		System.out.println("-------LOST MSGS----------");
		System.out.println(server.lateMsgs);
		System.out.println("-----------------");
		
		
		System.out.println("CURRENT MID = " + current_mid + "LAST MID = " + server.last_mid);
		
		

		/*
		if(server.lateMsgs.contains(current_mid)) {
			System.out.println("Late msg rec");
			server.lateMsgs.remove(current_mid);
			server.lost_msgs--;
		}
		*/
	
		for(int i = 0; i < server.lateMsgs.size(); i++) {
			if(current_mid == server.lateMsgs.get(i)) {
				System.out.println("Late msg rec");
				server.lateMsgs.remove(i);
				server.lost_msgs--;
				server.rec_msgs++;
				System.out.println("NEW LAST_MID = " + server.last_mid);
				return;
			}
		}
		
		if(server.last_msgs.size() >= 10) {
			for(int i = 0; i < server.last_msgs.size(); i++) 
				if(current_mid == server.last_msgs.get(i)) { 
					System.out.println("DUPLICATE");
					server.duplicates++;
					server.last_msgs.remove(0);
					System.out.println("NEW LAST_MID = " + server.last_mid);
					return;
				}
		}else 
			server.last_msgs.add(current_mid);
		
		
		if(current_mid ==  server.last_mid + 1) {
			System.out.println("RECIEVED");
			server.rec_msgs++;
			server.last_mid = current_mid;
			
		}
		//If current MID is not one step ahead of the last_mid, than a message was lost.
		else if(((server.last_mid +1) < current_mid)) {
			System.out.println("LOSS");
			//Defines how many messages were lost.
			System.out.println(server.last_mid + " - " + current_mid);
			int lap = current_mid - server.last_mid -1;
			System.out.println("LAP:" + lap);
			server.lost_msgs += lap;
			
			//Adds the MID of lost messages in a waiting list in case they are LATE.
			for(int i = 1; i < lap+1; i++) {
				server.lateMsgs.add(server.last_mid+i);
			}
			
			server.last_mid = current_mid;
		}
		
		System.out.println("NEW LAST_MID = " + server.last_mid);
		
	}
	
	
	public static void updateData(Timestamp last_Time, InetAddress Address, int current_mid, String payload) {
		for(int i = 0; i < servers.size(); i++) {	
			if(Address.equals(servers.get(i).IP)){
				servers.get(i).last_datetime = last_Time;
				
				checkMessage(current_mid, servers.get(i));	

				int eventClass = Character.getNumericValue(payload.charAt(1));
				String output = Fuzzy.start(numberOfObservers/(double)servers.size(), servers.get(i).getLoss(), eventClass, harvestingRate());
				System.out.println("INPUT--> " + (numberOfObservers/(double)servers.size()) + " " + servers.get(i).getLoss() + " " + eventClass + " " + harvestingRate());
				System.out.println("OUTPUT--> " + output);
				//servers.get(i).last_mid = current_mid;
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
	
	public static String outputInstructions(Message message) {
		List<InetAddress> obs_list = new ArrayList<>(observersArray);
		int next = 0;
		for(int i = 0; i < obs_list.size(); i++){ 
			if (message.getSource().equals(obs_list.get((i)))) {
				if(message.isConfirmable()) 
					servers.get(i).last_con =  message.getMID();
				
				next = servers.get(i).last_con + numberOfObservers + i;
			
				int event_Class = Character.getNumericValue(message.getPayloadString().charAt(1));
				int isHarvester = Character.getNumericValue(message.getPayloadString().charAt(0));
				
				System.out.println("ISHARVESTER: " + isHarvester);
				System.out.println("EVENT_CLASS: " + event_Class);
				String output = Fuzzy.start(numberOfObservers/(double)servers.size(), servers.get(i).getLoss(), event_Class, harvestingRate());
				System.out.println("INPUT----> + " +numberOfObservers/(double)servers.size() +" " + servers.get(i).getLoss() +" " + event_Class +" " + harvestingRate());
				System.out.println("OUTPUT_RECIEVED: " + output);				
				//System.out.println("Ultima CON foi: " + servers.get(i).last_con + " Proxima CON deveria ser: " + next);
			
				return output;
			}
				
		}
		return null;
	}
}
