package org.exoplatform.automatic.translation.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.exoplatform.automatic.translation.api.AutomaticTranslationService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/automatic-translation")
@Api(value = "/automatic-translation")
public class AutomaticTranslationRestService implements ResourceContainer {

  private AutomaticTranslationService automaticTranslationService;

  private static final Log LOG = ExoLogger.getLogger(AutomaticTranslationRestService.class);

  public AutomaticTranslationRestService(AutomaticTranslationService automaticTranslationService) {
    this.automaticTranslationService = automaticTranslationService;
  }

  @GET
  @Path("/configuration")
  @RolesAllowed("administrators")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
      value = "Gets Automatic Translation configuration",
      httpMethod = "GET",
      response = Response.class,
      produces = MediaType.APPLICATION_JSON,
      notes = "This returns the actual configuration for automatic translation"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled")
      }
  )
  public Response configuration() {
    JSONObject resultJSON = new JSONObject();

    String activeConnector = automaticTranslationService.getActiveConnector();
    JSONArray connectors = new JSONArray();
    automaticTranslationService.getConnectors().forEach((name, connector) -> {
      JSONObject jsonConnector = new JSONObject();
      jsonConnector.put("name",name);
      jsonConnector.put("description",connector.getDescription());
      connectors.add(jsonConnector);
      if (name.equals(activeConnector)) {
        resultJSON.put("activeApiKey", connector.getApiKey());
      }
    });

    resultJSON.put("connectors",connectors);
    resultJSON.put("active",activeConnector);
    return Response.ok(resultJSON.toString(), MediaType.APPLICATION_JSON)
                   .build();

  }


  @PUT
  @Path("/setActiveConnector")
  @RolesAllowed("administrators")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(
      value = "Set Automatic translation active connector",
      httpMethod = "PUT",
      response = Response.class,
      produces = MediaType.APPLICATION_JSON
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 403, message = "Not authorized to change the connector")
      }
  )
  public Response setActiveConnector(@ApiParam(value = "Connector name", required = true) @QueryParam("connector") String connector) {
    connector = (connector == null || connector.equals("null")) ? "" : connector;
    if (automaticTranslationService.setActiveConnector(connector)) {
      return Response.ok().build();
    } else {
      return Response.status(403).build();
    }

  }

  @PUT
  @Path("/setApiKey")
  @RolesAllowed("administrators")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(
      value = "Set Automatic translation API key",
      httpMethod = "PUT",
      response = Response.class,
      produces = MediaType.APPLICATION_JSON
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 403, message = "Not authorized to change the connector")
      }
  )
  public Response setApiKey(@ApiParam(value = "Connector name", required = true) @QueryParam("connector") String connector,
                            @ApiParam(value = "Api Key", required = true) @QueryParam("apikey") String apikey) {
    connector = (connector == null || connector.equals("null")) ? "" : connector;
    if (automaticTranslationService.setApiKey(connector,apikey)) {
      return Response.ok().build();
    } else {
      return Response.status(403).build();
    }

  }
}

