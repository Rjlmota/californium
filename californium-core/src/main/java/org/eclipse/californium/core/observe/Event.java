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
	
	
	
	
	public static void print_stats() {
		for(int i = 0; i < Servers.size(); i++) {
			System.out.println("Mensagens recebidas por " +  Event.Servers.get(i).IP + " : " + Event.Servers.get(i).rec_msgs);
			System.out.println("Duplicadas por : " + Event.Servers.get(i).IP + " : " + Event.Servers.get(i).duplicates);
			System.out.println("Mensagens perdidas por: " + Event.Servers.get(i).IP + " : " + Event.Servers.get(i).lost_msgs);
		}
	}
	
	
	private static void validade (int current_mid,  Server server) {
		//If current MID is higher than last MID, the message is new and should be counted.
		if(current_mid > server.last_mid)
			server.rec_msgs++;
		
		//If current MID is equal to the last MID, the message is duplicated;
		else if (current_mid == server.last_mid) 
			server.duplicates++;		
		
		
		//If current MID is not one step ahead of the last_mid, than a message was lost.
		else if(!((server.last_mid +1) == current_mid)) {
			//Defines how many messages were lost.
			int lap = current_mid - server.last_mid +1;
			server.lost_msgs += lap;
		}
		
	}
	
	
	public static void add_data(Timestamp last_Time, InetAddress Address, int current_mid) {
		
		for(int i = 0; i < Servers.size(); i++) {	
			if(Address.equals(Servers.get(i).IP)){
				Servers.get(i).last_datetime = last_Time;
				
				
				validade(current_mid, Servers.get(i));	
				
				Servers.get(i).last_mid = current_mid;
				event_data();
				return;
			}
		}
		//Server object was not found and a new Object is created to represent it.
		Servers.add(new Server(Address, last_Time));
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
