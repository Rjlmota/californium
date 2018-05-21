package org.eclipse.californium.core.observe;


//import org.eclipse.californium.core.CoapObserveRelation;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.californium.core.observe.Server;

import org.eclipse.californium.core.coap.Message;

//import org.eclipse.californium.core.CoapClient;
//import org.eclipse.californium.core.CoapResponse;
 



public class Event {
	
	public static Set <InetAddress> obs_arr = new HashSet <InetAddress>();
	private static int observers = 0;
	public static List <Server> Servers = new ArrayList <Server>();
	
	
	private static boolean appendMID(int MID, Server server) {
		
		//If input MID is already in array, it counts as duplicate.
		for(int i = 0; i < 10; i++) {
			if(server.MIDs[i] == MID) {
				server.duplicates++;
				return false;
			}
		}
		
		//Else, if verifies if mid_index is greater than array size and adjusts it.
		if(server.mid_index > 9) server.mid_index = 0;
		
		
		//Updates the last MID list.
		server.MIDs[server.mid_index] = MID; 
		server.mid_index++;
		
		return true;
		
	}
	
	
	public static void print_stats() {
		//Simply prints out the Recieved and Duplicate stats of each Server.
		
		for(int i = 0; i < Servers.size(); i++) {
			System.out.println("Mensagens recebidas por " +  Event.Servers.get(i).IP + " : " + Event.Servers.get(i).rec_msgs);
			System.out.println("Duplicadas por : " + Event.Servers.get(i).IP + " : " + Event.Servers.get(i).duplicates);
		}
	}
	
	
	
	public static void add_data(Timestamp last_Time, InetAddress inetAddress, int MID) {
		for(int i = 0; i < Servers.size(); i++) {
			if(inetAddress.equals(Servers.get(i).IP)){
				Servers.get(i).last_datetime = last_Time;
				//if(MID > Servers.get(i).last_mid)
					//Servers.get(i).rec_msgs++;
				//else Servers.get(i).duplicates++;
				
				//Servers.get(i).last_mid = MID;
				
				
				
				if(appendMID(MID, Servers.get(i))) Servers.get(i).rec_msgs++;
				
				event_data();
				return;
			}	
		}
		Servers.add(new Server());
		Servers.get(Servers.size()-1).IP = inetAddress;
		Servers.get(Servers.size()-1).last_datetime = last_Time;
		event_data();
	}
	
	
	private static int most_recent_event() {
		int index = 0;
		long higher = 0;
		for (int i = 0; i < Servers.size(); i++) 
			if(Servers.get(i).last_datetime.getTime() > higher) { 
				higher = Servers.get(i).last_datetime.getTime();
				index = i;
			}
		return index;
	}
		
	private static void event_data() {
		observers = 0;
		obs_arr.clear();
		if(Servers.size() < 2){
			System.out.println(observers + " mote is observing the event \n");
			return;
		}
		for(int i = 0; i < Servers.size(); i++)
				if(Math.abs(Servers.get(most_recent_event()).last_datetime.getTime() - Servers.get(i).last_datetime.getTime()) < 100000) {
					observers++;
					obs_arr.add(Servers.get(i).IP);
				}
		System.out.println(observers + " mote(s) observing the event \n");
		
		print_stats();
	}
	
	
	
	
	public static String next_con(Message message) {
		List<InetAddress> obs_list = new ArrayList<>(obs_arr);
		int next = 0;
		for(int i = 0; i < obs_list.size(); i++){ 
			if (message.getSource().equals(obs_list.get((i)))) {
				
				if(message.isConfirmable()) {
					Servers.get(i).last_con =  message.getMID();
				}
				
				next = Servers.get(i).last_con + observers + i;
			
		
				System.out.println("Ultima CON foi: " + Servers.get(i).last_con + " Proxima CON deveria ser: " + next);
			
				return Integer.toString(next);
				
				
			}
				
		}
	return "fail";
	}
}
