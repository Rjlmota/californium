/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and re-implementation
 *    Dominique Im Obersteg - parsers and initial implementation
 *    Daniel Pauli - parsers and initial implementation
 *    Kai Hudalla - logging
 ******************************************************************************/
package org.eclipse.californium.core.coap;

import java.net.InetAddress;
import java.util.Arrays;

import org.eclipse.californium.core.observe.Event;
import org.eclipse.californium.core.observe.Server;
import org.eclipse.californium.core.coap.CoAP.Type;

/**
 * EmptyMessage represents an empty CoAP message. An empty message has either
 * the message {@link Type} ACK or RST.
 */
public class EmptyMessage extends Message {

	/**
	 * Instantiates a new empty message.
	 *
	 * @param type the message type (ACK or RST)
	 */
	public EmptyMessage(Type type) {
		super(type);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String appendix = "";
		// crude way to check nothing extra is set in an empty message
		if (!hasEmptyToken()
				|| getOptions().asSortedList().size()>0
				|| getPayloadSize()>0) {
			String payload = getPayloadString();
			if (payload == null) {
				payload = "no payload";
			} else {
				int len = payload.length();
				if (payload.indexOf("\n")!=-1) payload = payload.substring(0, payload.indexOf("\n"));
				if (payload.length() > 24) payload = payload.substring(0,20);
				payload = "\""+payload+"\"";
				if (payload.length() != len+2) payload += ".. " + payload.length() + " bytes";
			}
			appendix = " NON-EMPTY: Token="+Arrays.toString(getToken())+", "+getOptions()+", "+payload;
		}
		return String.format("%s        MID=%5d%s", getType(), getMID(), appendix);
	}

	@Override
	public int getRawCode() {
		return 0;
	}

	/**
	 * Create a new acknowledgment for the specified message.
	 *
	 * @param message the message to acknowledge
	 * @return the acknowledgment
	 */
	public static EmptyMessage newACK(Message message) {
		System.out.println("Sending ACK");
		EmptyMessage ack = new EmptyMessage(Type.ACK);
		ack.setDestination(message.getSource());
		ack.setDestinationPort(message.getSourcePort());
		ack.setMID(message.getMID());
		//ack.setPayload(Event.next_con(Message))
		
		Server eli = Event.toEliminate();
		
		
		System.out.println("NeedToELiminate: " + Event.needToEliminate + " " + eli.IP);
		
		
		for(Server server : Event.removed)
			if(message.getSource().equals(server.IP)){
				System.out.println("Already Eliminated!\n");
				ack.setPayload("k");
				return ack;
			}
		
		if(Event.needToEliminate && eli != null && message.getSource().equals(eli.IP)) {
			System.out.println("Got into deletion " + eli.toString());
			System.out.println("This node will be removed: " + message.getSource());
			System.out.println("SENT KILL!!!\n!!!!!!!\n!!!!!!!!\n");
			ack.setPayload("k");
			Event.Stats.eliminations++;
			
			Event.needToEliminate = false;
			//Event.servers.remove(eli);
			System.out.println("TAMANHODOARRAYDESERVERS: " + Event.servers.size());
			Event.removed.add(eli);
			Event.lastEliminationTime = System.currentTimeMillis();
			
		}else {
			System.out.println("Setting payload: " + Event.outputInstructions(message));
			ack.setPayload(Event.outputInstructions(message));
		}
		System.out.println("ACK CONTENT: " + ack.getPayloadString());
		return ack;
	}
	
	/**
	 * Create a new reset message for the specified message.
	 *
	 * @param message the message to reject
	 * @return the reset
	 */
	public static EmptyMessage newRST(Message message) {
		EmptyMessage rst = new EmptyMessage(Type.RST);
		rst.setDestination(message.getSource());
		rst.setDestinationPort(message.getSourcePort());
		rst.setMID(message.getMID());
		return rst;
	}
	
}
