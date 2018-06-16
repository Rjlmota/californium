package org.eclipse.californium.core.observe;


import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Fuzzy {
	
	
	
		private static String parseOutput(String output) {
			
			String[] separeted_output = output.split("\\s+");
			
			//System.out.println("SEPARETED_OUTPUT " + separeted_output[0] + " " +  separeted_output[1] +" "+ separeted_output[2]);
			
			//INTERVAL:
			float raw_interval = Float.parseFloat(separeted_output[0]);
			int interval = (int) raw_interval;
			//Paddding with zeros
			String formatted_interval = String.format("%02d", interval);
			
			//CONFIRMAVEIS:
			float confirmable_rate = Float.parseFloat(separeted_output[1]);
			int confirmable_interval = (int) (confirmable_rate * 10);
			//Paddding with zeros
			String formatted_confirmable = String.format("%02d", confirmable_interval);
			
			
			//ELMINATE:
			float eliminate = Float.parseFloat(separeted_output[2]);
			int toEliminate = (int) eliminate;
			
			String result = 'i' +formatted_interval + formatted_confirmable + toEliminate;
			
			return result;
			//return "";
	}
	
	
		public static String start(int node_number, float loss_rate, int event_class, double harvesting) {
		


			Fuzzy obj = new Fuzzy();

			String command = "./main " + node_number + " " + loss_rate + " " + event_class + " " + harvesting;
			String output = obj.executeCommand(command);
			//System.out.println("AQUI: " + output);
			output = output.replaceAll("\n", " ");
			
			
			
			//FUNCAO PRA CONSERTAR OS VALORES - TODO
			output = parseOutput(output);
			// System.out.println("OUTPUT " + output);
			return output;

		}
		private String executeCommand(String command) {

			StringBuffer output = new StringBuffer();

			Process p;
			try {
				p = Runtime.getRuntime().exec(command);
				p.waitFor();
				BufferedReader reader = 
	                            new BufferedReader(new InputStreamReader(p.getInputStream()));

	                        String line = "";			
				while ((line = reader.readLine())!= null) {
					output.append(line + "\n");
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				return output.toString();

			}
		

		
	}

