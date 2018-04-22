/***************************
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
 *    Achim Kraus (Bosch Software Innovations GmbH) - add saving payload
 **************************/
package org.eclipse.californium.examples;

import org.eclipse.californium.core.observe.Event;

//import org.eclipse.californium.core.observe.Event;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;


//import org.eclipse.californium.core.observe.Event;








public class GETClient {

	/*
	 * Application entry point.
	 *
	 */
	
	public static void main(String args[]) {
		
		
		int obs_number = 5;
		//URI uri = null; // URI parameter of the request
		//URI uri2 = null;
		URI[] uri_arr = new URI[obs_number];
		CoapClient[] client_arr = new CoapClient[obs_number];
		CoapObserveRelation[] relation_arr = new CoapObserveRelation[obs_number];
		
		if (args.length > 0) {
			for(int i = 0; i < obs_number; i++) {
				// input URI from command line arguments
				try {
					//uri = new URI(args[0]);
					int z = i+2;
					uri_arr[i] = new URI(args[0].substring(0, 22) + z + args[0].substring(23));
				} catch (URISyntaxException e) {
					System.err.println("Invalid URI: " + e.getMessage());
					System.exit(-1);
				}
				
				System.out.println("URI: " + uri_arr[i]);
				//CoapClient client = new CoapClient(uri);
				client_arr[i] = new CoapClient(uri_arr[i]);
				
				System.out.println("===============\nCO01+06");
				System.out.println("---------------\nGET /obs with Observe");
				//CoapObserveRelation relation1 = client_arr[i].observe(
				relation_arr[i] = client_arr[i].observe(
				new CoapHandler() {
				@Override public void onLoad(CoapResponse response) {
				String content = response.getResponseText();
				System.out.println("-CO0----------");
				System.out.println(content);
				}
	
				@Override public void onError() {
				System.err.println("-Failed--------");
				}
				});
		
			
			}
			try { Thread.sleep(6*10000); } catch (InterruptedException e) { }
			System.out.println("----------"
					+ "-----\nCancel Observe");
			//relation1.reactiveCancel();
			
			//try { Thread.sleep(6*10000); } catch (InterruptedException e) { }
			
			//System.out.println("teste: " + relation_arr[0].teste);
			
			//System.out.println("aqui: " + relation_arr[0].last_Time);
			
			//Stats.event_observers(relation_arr);
			
			System.out.println("---------------\nCancel Observe 2");
			
			for (int i = 0; i < obs_number; i++) {
				System.out.println("Mensagens recebidas por " +  Event.IP.get(i) + " : " + Event.rec_msgs.get(i));
			}

			CoapResponse response = client_arr[0].get();
			
			if (response!=null) {
				
				System.out.println(response.getCode());
				System.out.println(response.getOptions());
				if (args.length > 1) {
					try (FileOutputStream out = new FileOutputStream(args[2])) {
						out.write(response.getPayload());
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println(response.getResponseText());
					
					System.out.println(System.lineSeparator() + "ADVANCED" + System.lineSeparator());
					// access advanced API with access to more details through
					// .advanced()
					System.out.println(Utils.prettyPrint(response));
				}
			} else {
				System.out.println("No response received.");
			}
			
		} else {
			// display help
			System.out.println("Californium (Cf) GET Client");
			System.out.println("(c) 2014, Institute for Pervasive Computing, ETH Zurich");
			System.out.println();
			System.out.println("Usage : " + GETClient.class.getSimpleName() + " URI [file]");
			System.out.println("  URI : The CoAP URI of the remote resource to GET");
			System.out.println("  file: optional filename to save the received payload");
		}
	}

}