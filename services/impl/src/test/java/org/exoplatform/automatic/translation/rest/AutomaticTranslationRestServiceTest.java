package org.exoplatform.automatic.translation.rest;

import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.automatic.translation.api.AutomaticTranslationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutomaticTranslationRestServiceTest {

  AutomaticTranslationRestService automaticTranslationRestService;

  @Mock
  AutomaticTranslationService automaticTranslationService;

  @Before
  public void setUp() {
    automaticTranslationService=mock(AutomaticTranslationService.class);
    automaticTranslationRestService = new AutomaticTranslationRestService(automaticTranslationService);
  }
  private void startSessionAs(String username) {
    Identity identity = new Identity(username);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  @Test
  public void testGetConfiguration() {
    Map<String, AutomaticTranslationComponentPlugin> connectors = new HashMap<>();
    AutomaticTranslationComponentPlugin googleConnector = new AutomaticTranslationComponentPlugin();
    googleConnector.setName("google");
    googleConnector.setDescription("google connector description");
    connectors.put("google",googleConnector);
    AutomaticTranslationComponentPlugin systranConnector = new AutomaticTranslationComponentPlugin();
    systranConnector.setName("systran");
    systranConnector.setDescription("systran connector description");
    connectors.put("systran",systranConnector);
    when(automaticTranslationService.getConnectors()).thenReturn(connectors);
    when(automaticTranslationService.getActiveConnector()).thenReturn("google");

    Response response = automaticTranslationRestService.configuration();
    assertEquals(200,response.getStatus());
    assertEquals("{\"connectors\":[{\"name\":\"google\",\"description\":\"google connector description\"},"
                     + "{\"name\":\"systran\",\"description\":\"systran connector description\"}],\"active\":\"google\"}",response.getEntity());
  }

}
