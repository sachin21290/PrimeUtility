package com.primeutility.rest.prime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import com.primeutility.rest.prime.utility.PrimeUtility;

@Path("/isprime")
public class PrimeService {

    public static final String NODE1 = "http://localhost:8080/RestPrimeService/computeprime/";
    public static final String NODE2 = "http://localhost:8080/RestPrimeService/computeprime/";
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getPrimeResponse(String numbers) throws InterruptedException, ExecutionException {
        JSONArray jsonArray = new JSONArray(numbers);
        List<String> input = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            String number = (String)jsonArray.get(i);
            System.out.println(number);
            input.add(number);
        }
        Set<String> inputSet = new HashSet<String>(input);
        Map<String, String> resultMap = launchPrimeUtilityService(new ArrayList<String>(inputSet));
        System.out.println(resultMap);
        List<String> primeResult = new ArrayList<String>();
        for (String number : input) {
            primeResult.add(resultMap.get(number));
        }
        JSONArray jsonArray1 = new JSONArray(primeResult);
        return Response.status(200).entity(jsonArray1.toString()).build();
    }

    private Map<String, String> launchPrimeUtilityService(List<String> input) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CompletionService<String> compService = new ExecutorCompletionService<>(executor);
        List<Future<String>> tasks = new ArrayList<Future<String>>();
        Map<String, String> resultMap = new HashMap<String, String>();
        for (String number : input) {
        	
        	if (!PrimeUtility.isPrime(number)) { 
        		 resultMap.put(number, "false");
        		 continue;
        	}
            Callable<String> task = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    try {

                        DefaultHttpClient httpClient = new DefaultHttpClient();
                        HttpGet getRequest =
                            new HttpGet(input.indexOf(number) % 2 == 0 ? NODE1 + number : NODE2 + number);

                        getRequest.addHeader("accept", "text/html");

                        HttpResponse response = httpClient.execute(getRequest);

                        if (response.getStatusLine().getStatusCode() != 200) {
                            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
                        }
                        BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
                        String output;
                        System.out.println("Output from Server .... \n");
                        if ((output = br.readLine()) != null) {
                            System.out.println(output);
                            return output;
                        }
                        httpClient.getConnectionManager().shutdown();
                      } catch (ClientProtocolException e) {
                        e.printStackTrace();
                      } catch (IOException e) {
                        e.printStackTrace();
                      }
                    return null;
                    }
            };
            tasks.add(compService.submit(task));
        }
       
        for (Future<String> future : tasks) {
            String result = future.get();
            JSONArray jsonArray = new JSONArray(result);
            resultMap.put((String)jsonArray.get(0), (String)jsonArray.get(1));
        }
        return resultMap;
    }

}