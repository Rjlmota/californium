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
	
	
	
	public static void add_data(Timestamp last_Time, InetAddress inetAddress, int MID) {
		for(int i = 0; i < Servers.size(); i++) {
			if(inetAddress.equals(Servers.get(i).IP)){
				Servers.get(i).last_datetime = last_Time;
				
				if(MID > Servers.get(i).last_mid)
					Servers.get(i).rec_msgs++;
				else Servers.get(i).duplicates++;
				
				if(!((Servers.get(i).last_mid +1) == MID)) Servers.get(i).lost_msgs++;
				
				
				Servers.get(i).last_mid = MID;
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
