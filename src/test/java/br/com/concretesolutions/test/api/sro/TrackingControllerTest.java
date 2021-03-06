/*
* Copyright 2016 Redecard S.A.
*************************************************************
*Nome     : PostalcocdeControllerTest.java
*Autor    : jfelipesp
*Data     : 5 de fev de 2016
*Empresa  : Concrete Solutions / Redecard
*************************************************************
*/
package br.com.concretesolutions.test.api.sro;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.concretesolutions.api.models.TrackingEntry;
import br.com.concretesolutions.api.models.TrackingResult;
import br.com.concretesolutions.api.sro.TrackingController;
import br.com.concretesolutions.config.ApplicationConfig;
import br.com.concretesolutions.scrapers.SroScraper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author jfelipesp
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ SroScraper.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TrackingControllerTest {

  @InjectMocks
  private TrackingController controller;

  private MockMvc mock;


  @Before
  public void setUp() {
    initMocks(this);
    mock = MockMvcBuilders.standaloneSetup(controller).build();
    PowerMockito.mockStatic(SroScraper.class);
  }

  @Test
  public void trackingNumberBadRequestTest() throws Exception {
    mock.perform(get("/sro")).andExpect(status().isBadRequest());
  }

  @Test
  public void trackingNumberNotFoundTest() throws Exception {
    PowerMockito.when(SroScraper.getTrackingResult(Mockito.eq("AA000000001BB"))).thenThrow(new IllegalStateException());

    mock.perform(get("/sro").param("trackingNumber", "AA000000001BB")).andExpect(status().isNotFound())
        .andExpect(content().string(""));
  }
  
  @Test
  public void trackingNumberServiceUnavailableTest() throws Exception {
    PowerMockito.when(SroScraper.getTrackingResult(Mockito.eq("AA000000002BB"))).thenThrow(new IOException());

    mock.perform(get("/sro").param("trackingNumber", "AA000000002BB")).andExpect(status().isServiceUnavailable())
        .andExpect(content().string(""));
  }
  
  @Test
  public void trackingNumberOKTest() throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(1970, 0, 1, 0, 0, 0);
    List<TrackingEntry> trackingEntries = new ArrayList<TrackingEntry>();
    trackingEntries.add(new TrackingEntry(cal.getTime(), "Unit Test", "Testing"));
    
    PowerMockito.when(SroScraper.getTrackingResult(Mockito.eq("AA000000003BB"))).thenReturn(new TrackingResult("AA000000003BB", trackingEntries));

    mock.perform(get("/sro").param("trackingNumber", "AA000000003BB")).andExpect(status().isOk())
        .andExpect(content().string("{\"tracking\":[{\"date\":\"1970-01-01 00:00\",\"location\":\"Unit Test\",\"action\":\"Testing\"}],\"trackingNumber\":\"AA000000003BB\"}"));
  }

}
