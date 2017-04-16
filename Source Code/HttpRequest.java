package cns;

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpRequest {

    final static String CRLF = "\r\n";
    final static int HTTP_PORT = 80;
    String method;
    String URL;
    String version;
    String headers = "";
    private String host;
    private int port;

    //Created using clients output socket
    public HttpRequest(BufferedReader from) {
		String firstLine = "";
		try {
		    firstLine = from.readLine();
		} 
		catch (IOException e) {
		    System.out.println("Error reading request line: " + e);
		}

		String[] tmp = firstLine.split(" ");
		method = tmp[0];
		URL = tmp[1];
		version = tmp[2];
	
		System.out.println("URL is: " + URL);
	
		if (!method.equals("GET")) {
		    System.out.println("Error: Method not GET");
		}
		try {
		    String line = from.readLine();
		    while (line.length() != 0) {
			headers += line + CRLF;
			/* We need to find host header to know which server to
			 * contact in case the request URL is not complete. */
			if (line.startsWith("Host:")) {
			    tmp = line.split(" ");
			    if (tmp[1].indexOf(':') > 0) {
				String[] tmp2 = tmp[1].split(":");
				host = tmp2[0];
				port = Integer.parseInt(tmp2[1]);
			    } else {
				host = tmp[1];
				port = HTTP_PORT;
			    }
			}
			line = from.readLine();
		    }
		} 
		catch (IOException e) {
		    System.out.println("Error reading from socket: " + e);
		    return;
		}
		System.out.println("Host to contact is: " + host + " at port " + port);	
    }

    //Return Host
    public String getHost() {
	return host;
    }
    //Return port
    public int getPort() {
	return port;
    }
    //Return URL
    public String getURL() {
	return URL;
    }
    
    
    //Append Last-Modified to headers
    public void checkModified(String[] modified){
    	if(modified.length > 1)
    	{
    		headers += "If-None-Match: " + modified[1] + CRLF;
    	}
    	headers += "If-Modified-Since: " + modified[0] + CRLF;    	
    }

    //Convert Request into String
    public String toString() {
		String req = "";
		req = method + " " + URL + " " + version + CRLF;
		req += headers;
		req += "Connection: close" + CRLF;
		req += CRLF;
		return req;
    }
}

