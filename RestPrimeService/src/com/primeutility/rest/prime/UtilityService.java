package com.primeutility.rest.prime;

import java.util.Arrays;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;

import com.primeutility.rest.prime.utility.PrimeUtility;

@Path("/computeprime/{number}")
public class UtilityService {

    @GET
    @Produces({MediaType.TEXT_HTML})
    @Consumes({MediaType.TEXT_HTML})
    public Response getBooleanResponse(@PathParam(value = "number") String number) {
        JSONArray jsonArray = new JSONArray(Arrays.asList(number, String.valueOf(PrimeUtility.isPrime(number))));
        return Response.status(200).entity(jsonArray.toString()).build();
    }

}