package org.eclipse.californium.core.observe;


import org.eclipse.californium.core.observe.Fuzzy;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.californium.core.observe.Server;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Message;
public class Event {
	
	//Class to act as a data structure to store general information about the observation.
	public static class Stats{
		//position 0 in this array indicates non harvester and 1 indicates harvester.
		static final int recieved[] = {0, 0};
		static int lost[] = {0, 0};
		public static int CON = 0;
		public static int NON = 0;
		public static int eliminations = 0;
		public static double loss_rate = 0;
	}
	
	
	//Array to store current observers.
	public static Set <InetAddress> observersArray = new HashSet <InetAddress>();
	private static int numberOfObservers = 0;
	//Attay to store all servers that did communicate with this client.
	public static List <Server> servers = new ArrayList <Server>();
	public static List <Server> removed = new ArrayList <Server>();
	
	//Timestamp to indicate the last system time that an elimination occurred.
	public static long lastEliminationTime = System.currentTimeMillis();
	//Timestamp to indicate the last time any relevant message was recieved
	public static long lastTimeRecieved;
	
	//Public variable to indicate if the program needs to eliminate one node.
	public static Boolean needToEliminate = false;
	
	

	
	//Function that returns the proportion of harvesters observing the event.
	public static double harvestingRate() {
		int harvestingNodes = 0;
		if(numberOfObservers > 0) {
			for(Server server : servers) 
				if(observersArray.contains(server.IP))
					if (server.isHarvesting == 1) 
						harvestingNodes++;
			//System.out.println("HARVESTING: " + harvestingNodes + " " + numberOfObservers);
			return (harvestingNodes/(double)numberOfObservers);
		}else 
			return 0;
	}
	
	

	
	
	public static void printStats() {
		System.out.println("IP\tRecebidas\tPerdidas\tObserving\tLast Message\n");
		for(int i = 0; i < servers.size(); i++) {
			//System.out.println("Mensagens recebidas por " +  Event.servers.get(i).IP + " : " + Event.servers.get(i).rec_msgs);
			//System.out.println("Duplicadas por : " + Event.servers.get(i).IP + " : " + Event.servers.get(i).duplicates);
			//System.out.println("Mensagens perdidas por: " + Event.servers.get(i).IP + " : " + Event.servers.get(i).lost_msgs);
			System.out.println(Event.servers.get(i).IP + "\t" + Event.servers.get(i).rec_msgs + "\t" + Event.servers.get(i).lost_msgs + "\t" + Event.servers.get(i).isObserving + "\t" + Event.servers.get(i).last_datetime);
		}
		
		System.out.println("Removed nodes: \n");
		for(int i = 0; i < removed.size(); i++) {
			System.out.println(removed.get(i).IP);
		}
				
		
		int total_messages = Stats.recieved[0]+Stats.recieved[1];
		int total_loss = Stats.lost[0] + Stats.lost[1];
		double loss_rate= 0;
		
		if((total_messages+total_loss) > 0) {
			loss_rate = total_loss*1.0/((total_messages+total_loss)*1.0);
		}
		
		Stats.loss_rate = loss_rate;
		
		System.out.print("\n\n------------------------\n");
		System.out.println("NORMAL\tHARVESTING");
		System.out.println(Stats.recieved[0]+"\t"+Stats.recieved[1]);
		System.out.println(Stats.lost[0] + "\t"+ Stats.lost[1]);
		
		
		System.out.println("CURRENT LOSS RATE: " + loss_rate);
		System.out.println("TOTAL MESSAGES: " + total_messages);
		System.out.println("CONFIRMABLE: " + Stats.CON);
		System.out.println("NON CONFIRMABLE: " + Stats.NON);
		System.out.println("Eliminations: " + Stats.eliminations);
		System.out.print("\n\n------------------------");
		
	}
	
	

	
	public static Server toEliminate() {
		
		Server eli = null;
		
		double currentLoss = -1;
		 InetAddress eliminate = null;
		 int indexEliminate = 0;
		for(int i = 0; i < servers.size(); i++) {
			if(servers.get(i).getLoss() > currentLoss && !removed.contains(servers.get(i))) {
				if(servers.get(i).isHarvesting == 0) {
					//eliminate = servers.get(i).IP;
					eli = servers.get(i);
					indexEliminate = i;
				}
			}
		}
		//servers.remove(indexEliminate);
		return eli;
	}
	
	private static boolean lateMessageHandler(int current_mid, Server server) {
		//Runs through the missing messages and checks if the recieved message matches one in the list
		for(int i = 0; i < server.lateMsgs.size(); i++) {
			//If matched, the system deletes this message from the list, decreases the lost counter and increases the recieved counter.
			if(current_mid == server.lateMsgs.get(i)) {
				System.out.println("LATE message recieved");
				server.lateMsgs.remove(i);
				
				server.lost_msgs--;
				Stats.lost[server.isHarvesting]--;
				
				server.rec_msgs++;
				Stats.recieved[server.isHarvesting]++;

				return true;
			}
			
			if(server.last_msgs.contains(current_mid)) {
				System.out.println("DUPLICATE");
				server.duplicates++;
				server.last_msgs.remove(0);
				
				if(server.last_msgs.size() >= 10)//10 is the size limit of this array. Any longer and the message is considered no longer wanted.
					server.last_msgs.remove(0);
				return true;
			}
		}
		return false;
	}
	

	private static boolean registerMessage (int current_mid,  Server server) {
		//If current MID is higher than last MID, the message is new and should be counted.
		
		
		server.last_datetime = System.currentTimeMillis();
		
		//If the last mid is 0, then this is the first message recieved. Thus, the last mid does not count.
		if(server.last_mid == 0) { 
			server.last_mid = current_mid;
			return true;
		}
		
		System.out.println("CURRENT MID = " + current_mid + "LAST MID = " + server.last_mid);
		
		
		
		//If the message has a lower mid than the last, then it is late or a duplicate.
		if(current_mid < server.last_mid) {
			lateMessageHandler(current_mid, server);
			return true;
		}
		//If the message is not lower than mid, then it can be accounted as one of the latest messages without problems.
		server.last_msgs.add(current_mid);
		
		if(current_mid ==  server.last_mid + 1) {
			System.out.println("RECIEVED");
			server.rec_msgs++;
			Stats.recieved[server.isHarvesting]++;
			server.last_mid = current_mid;
			
			
			return true;
		}
		
		//If current MID is not one step ahead of the last_mid, than a message was lost.
		else if(((server.last_mid +1) < current_mid)) {
			System.out.println("LOSS");
			//Defines how many messages were lost.
			int lap = current_mid - server.last_mid -1;
			System.out.println("LAP:" + lap);
			server.lost_msgs += lap;
			Stats.lost[server.isHarvesting]+= lap;
			
			//Adds the MID of lost messages in a waiting list in case they are LATE.
			//TO-DO ---> Implement dynamic list to exclude items as they get old.
			for(int i = 1; i < lap+1; i++) {
				server.lateMsgs.add(server.last_mid+i);
			}
			
			server.last_mid = current_mid;
			return true;
		}
		return false;
	}
	
	
	public static int getData(CoapResponse response) {
		int current_mid = response.advanced().getMID();
		InetAddress senderAddress = response.advanced().getSource();
		String payload = response.advanced().getPayloadString();
		
		
		if(payload.length() != 2)
			return 0;
		
		if(!response.advanced().isNotification())
			return 0;
		
		
		for(int i = 0; i < servers.size(); i++) {	
			if(senderAddress.equals(servers.get(i).IP)){
				servers.get(i).last_datetime = lastTimeRecieved;
				
				if (registerMessage(current_mid, servers.get(i))) {
					if(response.advanced().getType() == Type.CON)
						Stats.CON++;
					if(response.advanced().getType() == Type.NON)
						Stats.NON++;
				}

				int eventClass = Character.getNumericValue(payload.charAt(1));
				String output = Fuzzy.start(numberOfObservers/(double)servers.size(), servers.get(i).getLoss(), eventClass, harvestingRate());
				System.out.println("INPUT--> " + (numberOfObservers/(double)servers.size()) + " " + servers.get(i).getLoss() + " " + eventClass + " " + harvestingRate());
				System.out.println("OUTPUT--> " + output);
				updateStats();
				
				return 1;
			}
		}
		//Server object was not found and a new Object is created to represent it.
		
		for(Server server : removed) {
			if(server.IP.equals(senderAddress))
				return 0;
		}
		
		servers.add(new Server(senderAddress, lastTimeRecieved, Character.getNumericValue(payload.charAt(0))));
		updateStats();
		return 0;
	}
	
	
	private static Server lastEvent() {
		int index = 0;
		long higher = 0;
		for (int i = 0; i < servers.size(); i++) 
			if(servers.get(i).last_datetime > higher) { 
				higher = servers.get(i).last_datetime;
				index = i;
			}
		return servers.get(index);
	}
	
	
		
	private static void updateStats() {
		numberOfObservers = 0;
		observersArray.clear();
		if(servers.size() < 2){
			System.out.println(numberOfObservers + " mote is observing the event \n");
			return;
		}
		for(int i = 0; i < servers.size(); i++)
				if(Math.abs(lastEvent().last_datetime- servers.get(i).last_datetime) < 120000) {
					servers.get(i).isObserving =  true;
					numberOfObservers++;
					observersArray.add(servers.get(i).IP);
				}else {
					servers.get(i).isObserving = false;
				}
		System.out.println(numberOfObservers + " mote(s) observing the event \n");
		printStats();
	}
	
	public static String outputInstructions(Message message) {
		List<InetAddress> obs_list = new ArrayList<>(observersArray);
		int next = 0;
		
		if(message.getPayloadSize() != 2) 
			return null;
		
		
		for(int i = 0; i < obs_list.size(); i++){ 
			if (message.getSource().equals(obs_list.get((i)))) {
				if(message.isConfirmable()) 
					servers.get(i).last_con =  message.getMID();
				
				next = servers.get(i).last_con + numberOfObservers + i;
			
				int event_Class = Character.getNumericValue(message.getPayloadString().charAt(1));
				int isHarvester = Character.getNumericValue(message.getPayloadString().charAt(0));
				
				//System.out.println("ISHARVESTER: " + isHarvester);
				//System.out.println("EVENT_CLASS: " + event_Class);
				String output = Fuzzy.start(numberOfObservers/(double)servers.size(), Stats.loss_rate, event_Class, harvestingRate());
				//System.out.println("INPUT----> + " +numberOfObservers/(double)servers.size() +" " + servers.get(i).getLoss() +" " + event_Class +" " + harvestingRate());
				//System.out.println("OUTPUT_RECIEVED: " + output);				
				//System.out.println("Ultima CON foi: " + servers.get(i).last_con + " Proxima CON deveria ser: " + next);
			
				return output;
			}
				
		}
		return null;
	}
}
