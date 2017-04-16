package cns;

import java.net.*;
import java.io.*;
import java.util.*;

public class ProxyCache {

    private static int port;
    private static ServerSocket socket;
    private static HashMap<String, HttpResponse> cache;

    public static void init(int p) {
		port = p;
		cache = new HashMap<String, HttpResponse>();
		try {
		    socket = new ServerSocket(p);
		} 
		catch (IOException e) {
		    System.out.println("Error creating socket: " + e);
		    System.exit(-1);
		}
    }

    public static void handle(Socket client) {
		Socket server = null;
		HttpRequest request = null;
		HttpResponse response = null;
	
		// Read request
		try {
		    BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
		    request = new HttpRequest(fromClient);
		} catch (IOException e) {
		    System.out.println("Error reading request from client: " + e);
		    return;
		}
		
		//Check if request is stored in cache
		String requestLine = request.toString();
		if(cache.containsKey(requestLine))	//Check if GET request is in cache
		{
			//Send GET request with last-modified header
			System.out.println("IN CACHE");
			response = cache.get(requestLine);	//Get response from cache
			request.checkModified(response.getModified());	//append modified to header
			try {
			    server = new Socket(request.getHost(), 80);
			    DataOutputStream toServer = new DataOutputStream(server.getOutputStream());
			    toServer.writeBytes(request.toString());
			    toServer.flush();
			} catch (UnknownHostException e) {
			    System.out.println("Unknown host: " + request.getHost());
			    System.out.println(e);
			    return;
			} catch (IOException e) {
			    System.out.println("Error writing request to server: " + e);
			    return;
			}
		}
		else{
			//Send Normal GET request
			System.out.println("Not in Cache"); 
			try {
			    server = new Socket(request.getHost(), 80);
			    DataOutputStream toServer = new DataOutputStream(server.getOutputStream());
			    toServer.writeBytes(request.toString());
			    toServer.flush();
			} catch (UnknownHostException e) {
			    System.out.println("Unknown host: " + request.getHost());
			    System.out.println(e);
			    return;
			} catch (IOException e) {
			    System.out.println("Error writing request to server: " + e);
			    return;
			}
		}
			
		//Get Response from server
		try {
		    DataInputStream fromServer = new DataInputStream(server.getInputStream());
		    System.out.println("Getting Response from Server...");
		    response = new HttpResponse(fromServer);
		    server.close();
		} catch (IOException e) {
		    System.out.println("Error writing response to client: " + e);
		}
			
		//Check response to see if unmodified
		if(response.checkModified()){
			System.out.println("Retreiving data from Cache");
			response = cache.get(requestLine);					//Get Data from cache
		}
		else{
			System.out.println("Retreiving data from Host");
			cache.put(request.toString(), response);			//Get Data from response
		}
	
	
		//Write data to client	
		try {
		    DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
		    toClient.writeBytes(response.toString());
		    //for(int i = 0; i < response.bytesRead; i++){
			    //toClient.write(response.body, 0, response.bytesRead-1); //OFFSET set to 6 because getting some random bytes
		        toClient.flush();
		    	toClient.write(response.body);
		    //}
		    toClient.flush();
		    client.close();
		} catch (IOException e) {
		    System.out.println("Error writing response to client: " + e);
		}
		System.out.println();
	}
   
/* -------------------------------------------------- */


    /** Read command line arguments and start proxy */
    public static void main(String args[]) {
	int myPort = 0;
	
	try {
	    myPort = Integer.parseInt(args[0]);
	    //myPort = 7300;
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("Need port number as argument");
	    System.exit(-1);
	} catch (NumberFormatException e) {
	    System.out.println("Please give port number as integer.");
	    System.exit(-1);
	}
	
	init(myPort);

	/** Main loop. Listen for incoming connections and spawn a new
	 * thread for handling them */
	Socket client = null;
	
	while (true) {
	    try {
	    	System.out.println("Waiting..");
			client = socket.accept();
			handle(client);
	    } catch (IOException e) {
			System.out.println("Error reading request from client: " + e);
			continue;
	    }
	}

    }
}
