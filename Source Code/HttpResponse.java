package cns;

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpResponse {
    final static String CRLF = "\r\n";
    final static int BUF_SIZE = 8192;
    /** Maximum size of objects that this proxy can handle. For the
     * moment set to 100 KB. You can adjust this as needed. */
    final static int MAX_OBJECT_SIZE = 1000000;
    String version;
    public int bytesRead;
    int status;
    String statusLine = "";
    String headers = "";
    byte[] body = new byte[MAX_OBJECT_SIZE];

    /** Read response from server. */
    @SuppressWarnings("deprecation")
	public HttpResponse(DataInputStream fromServer) {
		/* Length of the object */
		int length = -1;
		boolean gotStatusLine = false;
	
		/* First read status line and response headers */
		try {
		    String line = fromServer.readLine();
		    while (line.length() != 0) {
				if (!gotStatusLine) {
				    statusLine = line;
				    gotStatusLine = true;
				} else {
				    headers += line + CRLF;
				}
				if (line.startsWith("Content-length") ||
				    line.startsWith("Content-Length")) {
				    String[] tmp = line.split(" ");
				    length = Integer.parseInt(tmp[1]);
				}
				line = fromServer.readLine();
		    }
		    System.out.println(statusLine);
		} catch (IOException e) {
		    System.out.println("Error reading headers from server: " + e);
		    return;
		}
	
		try {
		    bytesRead = 0;
		    byte buf[] = new byte[BUF_SIZE];
		    boolean loop = false;
	
		    if (length == -1) {
			loop = true;
		    }

		    while (bytesRead < length || loop) {
			// Read it in as binary data
				int res = fromServer.read(buf, 0, BUF_SIZE);
				if (res == -1) {
				    break;
				}
				for (int i = 0; i < res && (i + bytesRead) < MAX_OBJECT_SIZE; i++) {
				    body[bytesRead+i] = buf[i];
				}
				bytesRead += res;
		    }
	 	} 
		catch (IOException e) {
	 	    System.out.println("Error reading response body: " + e);
	 	    return;
	 	}
	}

    //Checks if response status indicates modified
    public Boolean checkModified(){
    	String[] tmp = statusLine.split(" ");
    	if(tmp[1].equals("304")){
    		return true;
    	}
    	return false;
    }
    
    //Return an array of headers used to check if modified
    public String[] getModified(){
    	String[] modified;
    	if(getEtag().equals("NULL")){
    		modified = new String[]{
        			getLastModified()
        	};
    	}
    	else{
    		modified = new String[]{
        			getLastModified(),
        			getEtag()
        	};
    	}
    	return modified;
    }
    
    //Gets the header Last-Modified
    public String getLastModified(){
    	String[] indHeaders = headers.split(CRLF);
    	for(int i = 0; i < indHeaders.length; i++){
    		String[] tmp = indHeaders[i].split(" ");
    		if(tmp[0].equals("Last-Modified:")){
    			String lastModified = "";
    			for(int j = 1; j < tmp.length; j++){
    				if(tmp[j].equals("GMT")){
    					lastModified += tmp[j];
    				}
    				else{
    					lastModified += tmp[j] + " ";
    				}
    				
    			}
    			return lastModified;	
    		}
    	}
    	return "'Last-Modified' not found.";
    }
    
    //Gets the header ETag
    public String getEtag(){
    	String[] indHeaders = headers.split(CRLF);
    	for(int i = 0; i < indHeaders.length; i++){
    		String[] tmp = indHeaders[i].split(" ");
    		if(tmp[0].equals("ETag:")){
    			String lastModified = tmp[1];
    			return lastModified;	
    		}
    	}
    	return "NULL";
    }
    
    
   
    public String toString() {
	String res = "";
	
	res = statusLine + CRLF;
	res += headers;
	res += CRLF;
	
	return res;
    }
}

