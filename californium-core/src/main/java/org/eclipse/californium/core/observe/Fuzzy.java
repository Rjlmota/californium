package org.eclipse.californium.core.observe;


import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Fuzzy {
		//Function to process raw output data and format to the model set on Contiki;
		private static String formattedFloat(String value) {
			//Converting number to Float
			float raw_input = Float.parseFloat(value);
			//Converting Float to Int
			int input = (int) raw_input;
			//Formatting
			String output = String.format("%02d", input);
			
			return output;
		}
		
		
		private static String parseEli(String value) {
			float raw_input = Float.parseFloat(value);
			int input = (int) (raw_input);
			System.out.println("INPUT ELIMINATION: " + input);
			String output = String.format("%d", input);
			return output;
		}
		//Formating raw output.
		private static String parseOutput(String output) {	
			//Splits out String into an array of Strings, used to conver the values to Non-String values. 
			String[] separeted_output = output.split("\\s+");
			
			//INTERVAL:
			String formatted_interval = formattedFloat(separeted_output[0]);
			
			//CONFIRMAVEIS:
			String eliminate = parseEli(separeted_output[1]);
			
			//ELMINATE:
			int formatted_confirmable = (int)(100*Float.parseFloat(separeted_output[2]));
			
			int toEliminate = Integer.parseInt(eliminate);

			if(toEliminate == 1) {
				//Event.needToEliminate = true;
				if(System.currentTimeMillis() - Event.lastEliminationTime > 800000) {
					Event.needToEliminate = true;
					toEliminate = 1;
					System.out.println("Eliminating");
					Event.lastEliminationTime = System.currentTimeMillis();
				}else {
					toEliminate = 0;
				}

			}
			
			String result = 'i' +formatted_interval + formatted_confirmable + toEliminate;
			
			return result;	
			}
	
	
		public static String start(double d, double loss_rate, int event_class, double harvesting) {
			//Creating command and executing it on Shell, in order to call Fuzzy module
			Fuzzy obj = new Fuzzy();
			String command = "./main " + d + " " + loss_rate + " " + event_class + " " + harvesting;
			//Get the output generated by Shell command.
			String output = obj.executeCommand(command);

			//Formatting output.
			output = output.replaceAll("\n", " ");
			
			System.out.println(command + ": " + output);
			output = parseOutput(output);
			System.out.println("AQUI OLHA: " + output);
			return output + event_class;

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

