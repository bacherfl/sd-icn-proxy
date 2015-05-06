package itec.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.LocationInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

public class ProxyThread extends Thread {
    private Socket socket = null;
    private String resolverUrl;
    private static final int BUFFER_SIZE = 32768;
    public ProxyThread(Socket socket, String resolverUrl) {
        super("ProxyThread");
        this.socket = socket;
        this.resolverUrl = resolverUrl;
    }

    public void run() {
        //get input from user
        //send request to server
        //get response from server
        //send response to user

        try {
            DataOutputStream out =
                    new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            String inputLine, outputLine;
            int cnt = 0;
            String urlToCall = "";
            ///////////////////////////////////
            //begin get request from client
            while ((inputLine = in.readLine()) != null) {
                try {
                    StringTokenizer tok = new StringTokenizer(inputLine);
                    tok.nextToken();
                } catch (Exception e) {
                    break;
                }
                //parse the first line of the request to find the url
                if (cnt == 0) {
                    String[] tokens = inputLine.split(" ");
                    urlToCall = tokens[1];
                    //can redirect this to output log
                    System.out.println("Request for : " + urlToCall);
                }

                cnt++;
            }
            //end get request from client
            ///////////////////////////////////


            BufferedReader rd = null;
            try {
                RestTemplate restTemplate = new RestTemplate();

                String contentLocations =
                        restTemplate.getForObject("http://" + resolverUrl + ":6666/location/resolve?contentName=" + urlToCall, String.class);


                System.out.println(contentLocations);
                /*
                JSONObject jsonLocations = (JSONObject)JSONValue.parse(contentLocations);
                JSONArray locations = (JSONArray) jsonLocations.get("locations");

                System.out.println("1");
                if (locations.size() == 0) {
                    return;
                }
                */
                System.out.println("2");

                System.out.println(contentLocations);
                ObjectMapper mapper = new ObjectMapper();
                LocationInfo locationInfo = mapper.readValue(contentLocations, LocationInfo.class);

                if (locationInfo.getLocations().size() == 0) {
                    return;
                }
                System.out.println("3");
                String newUrl = "http://" + locationInfo.getLocations().get(0) + "/media" + urlToCall;
                System.out.println("sending request to real server for url: " + newUrl);
                ///////////////////////////////////

                //begin send request to server, get response from server
                URL url = new URL(newUrl);
                URLConnection conn = url.openConnection();
                System.out.println("4");
                conn.setDoInput(true);
                //not doing HTTP posts
                conn.setDoOutput(false);
                //System.out.println("Type is: "
                //+ conn.getContentType());
                //System.out.println("content length: "
                //+ conn.getContentLength());
                //System.out.println("allowed user interaction: "
                //+ conn.getAllowUserInteraction());
                //System.out.println("content encoding: "
                //+ conn.getContentEncoding());
                //System.out.println("content type: "
                //+ conn.getContentType());

                // Get the response
                InputStream is = null;
                HttpURLConnection huc = (HttpURLConnection)conn;
                System.out.println("5");
                if (conn.getContentLength() > 0) {
                    try {
                        is = conn.getInputStream();
                        rd = new BufferedReader(new InputStreamReader(is));
                    } catch (IOException ioe) {
                        System.out.println(
                                "********* IO EXCEPTION **********: " + ioe);
                    }
                }
                System.out.println("6");
                //end send request to server, get response from server
                ///////////////////////////////////

                ///////////////////////////////////
                //begin send response to client
                byte by[] = new byte[ BUFFER_SIZE ];
                int index = is.read( by, 0, BUFFER_SIZE );
                System.out.println("7");
                while ( index != -1 )
                {
                    System.out.println("received " + by[0]);
                    out.write( by, 0, index );
                    index = is.read( by, 0, BUFFER_SIZE );
                }
                out.flush();

                //end send response to client
                ///////////////////////////////////
            } catch (Exception e) {
                //can redirect this to error log
                System.err.println("Encountered exception: " + e);
                //encountered error - just send nothing back, so
                //processing can continue
                out.writeBytes("");
            }

            //close out all resources
            if (rd != null) {
                rd.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}