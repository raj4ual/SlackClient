package com.client.slack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 * 
 * @author 0600B9744
 *
 */
public class SlackClient {
	private final static Logger LOGGER =  
            Logger.getLogger(SlackClient.class.getSimpleName());

	
    private static final String PASSPHRASE="RAJ-SLACK";

    protected static Map<String,Integer> slackApiCallStatus=new HashMap<>();
    public static void main( String[] args ) throws Exception {
    
    	if(args.length >0)	{
    	try {
    		String ul =args[0].split("//")[1];
    		ul="https://"+EncryptorAesGcmPassword.decrypt(ul,PASSPHRASE );

    		URL url = new URL(ul);
    	
    		JSONObject json = new JSONObject();
    		String channel=null;
    		if (args[1] !=null && args[1].contains("@channel")) {
    			channel=args[1].split("=")[1];
    			
    			frameJsonRequest(json,channel,args[2]);
    			LOGGER.log(Level.INFO, "message to be sent to channel :"+ channel);

    			postMsgToSlack(json,channel,url);
			//json.put("channel",channel);
    		}else if (args[1] !=null && args[1].contains("@DM")) {
    			String ids=args[1].split("=")[1];
    			if (null !=ids && ids.length()>0){
    				String slackIdArr [] =ids.split(",");
    				if(null !=slackIdArr && slackIdArr.length>0) {
    					for(int i=0;i<slackIdArr.length;i++) {
    						channel=slackIdArr[i];
    						frameJsonRequest(json,channel,args[2]);
    		    			LOGGER.log(Level.INFO, "message to be sent to channel :"+ channel);
    						postMsgToSlack(json,channel,url);
    					}
    				}
    			}
    		}
    		LOGGER.log(Level.INFO, "messages sent status  :"+ slackApiCallStatus);
       }catch (MalformedURLException e) {
			e.printStackTrace();
		} 
    	}else {
   		LOGGER.log(Level.SEVERE,"No Arguments passed");
   		throw new Exception("No arguments present");
    	}
    }
	

	private static void frameJsonRequest(JSONObject json, String channel, String msgArg) {
		json.put("channel",channel);
		json.put("text",msgArg);
		json.put("username","SLACKMSG");
		LOGGER.log(Level.FINE,"json: "+json.toString());		
	}


	private static void postMsgToSlack(JSONObject json, String channel, URL url) {
		try {
			int retry=2;
			boolean connected =false;
			HttpURLConnection conn=null; 
			for(int i=0;i<=retry && !connected;i++) {
				conn = (HttpURLConnection) url.openConnection();
				if(i>0) {
					LOGGER.log(Level.INFO,"no. of attempt:"+i);
				}
				conn.setDoOutput(true);

				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
		
				OutputStream os = conn.getOutputStream();
				os.write(json.toString().getBytes());
				os.flush();

				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					connected=true;
					slackApiCallStatus.put(channel, conn.getResponseCode());
					BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

					String output;
					LOGGER.log(Level.FINE,"Output from Server .... \\n");
					while ((output = br.readLine()) != null) {
						LOGGER.log(Level.FINE,output);
					}

				}
				else if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					slackApiCallStatus.put(channel, conn.getResponseCode());
					LOGGER.log(Level.WARNING,"error_occured:"+conn.getResponseCode());
					conn.disconnect();
					continue;
			//throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
				}else if(conn.getResponseCode() != HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {
					slackApiCallStatus.put(channel, conn.getResponseCode());
					LOGGER.log(Level.WARNING,"error_occured:"+conn.getResponseCode());
					conn.disconnect();
					continue;
				}

			}
			conn.disconnect();

		} catch (IOException e) {
			  e.printStackTrace();
			}
		
	}

}
