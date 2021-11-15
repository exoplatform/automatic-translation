/*
 * Copyright (C) 2021 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.automatic.translation.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.exoplatform.automatic.translation.api.AutomaticTranslationService;
import org.exoplatform.services.rest.resource.ResourceContainer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Locale;

@Path("/automatic-translation")
@Api(value = "/automatic-translation")
public class AutomaticTranslationRestService implements ResourceContainer {

  private AutomaticTranslationService automaticTranslationService;

  public AutomaticTranslationRestService(AutomaticTranslationService automaticTranslationService) {
    this.automaticTranslationService = automaticTranslationService;
  }

  @GET
  @Path("/configuration")
  @RolesAllowed("administrators")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets Automatic Translation configuration", httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON, notes = "This returns the actual configuration for automatic translation")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response configuration() {
    String activeConnector = automaticTranslationService.getActiveConnector();
    AutomaticTranslationConfiguration configuration = new AutomaticTranslationConfiguration(new ArrayList<>(), activeConnector);
    automaticTranslationService.getConnectors().forEach((name, connector) -> {
      if (name.equals(activeConnector)) {
        configuration.setActiveApiKey(connector.getApiKey());
        configuration.addConnector(name, connector.getDescription(), connector.getApiKey());
      } else {
        configuration.addConnector(name, connector.getDescription(), null);
      }
    });

    return Response.ok(configuration, MediaType.APPLICATION_JSON).build();

  }

  @PUT
  @Path("/setActiveConnector")
  @RolesAllowed("administrators")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Set Automatic translation active connector", httpMethod = "PUT", response = Response.class, produces = MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Not authorized to change the connector") })
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
  @ApiOperation(value = "Set Automatic translation API key", httpMethod = "PUT", response = Response.class, produces = MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Not authorized to change the connector") })
  public Response setApiKey(@ApiParam(value = "Connector name", required = true) @QueryParam("connector") String connector,
                            @ApiParam(value = "Api Key", required = true) @QueryParam("apikey") String apikey) {
    connector = (connector == null || connector.equals("null")) ? "" : connector;
    if (automaticTranslationService.setApiKey(connector, apikey)) {
      return Response.ok().build();
    } else {
      return Response.status(403).build();
    }

  }

  @POST
  @Path("/translate")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "translate message passed in parameter", httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON, notes = "This returns the message transalted in the asked locale, by using the active connector")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled") })
  public Response translate(@ApiParam(value = "message", required = true) @FormParam("message") String message,
                            @ApiParam(value = "locale", required = true) @FormParam("locale") String localeParam) {
    JSONObject resultJSON = new JSONObject();

    Locale locale = Locale.forLanguageTag(localeParam);
    if (locale == null) {
      return Response.status(500).build();
    }
    String translatedMessage = automaticTranslationService.translate(message, locale);

    if (translatedMessage != null) {
      resultJSON.put("locale", locale.toString());
      resultJSON.put("translation", translatedMessage);
      return Response.ok(resultJSON.toString(), MediaType.APPLICATION_JSON).build();
    } else {
      return Response.status(500).build();
    }

  }

}
