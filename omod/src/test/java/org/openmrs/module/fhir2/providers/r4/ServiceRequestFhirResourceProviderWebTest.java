/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;

import lombok.AccessLevel;
import lombok.Getter;
import org.hamcrest.MatcherAssert;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRequestFhirResourceProviderWebTest extends BaseFhirR4ResourceProviderWebTest<ServiceRequestFhirResourceProvider, ServiceRequest> {
	
	private static final String SERVICE_REQUEST_UUID = "7d13b03b-58c2-43f5-b34d-08750c51aea9";
	
	private static final String WRONG_SERVICE_REQUEST_UUID = "92b04062-e57d-43aa-8c38-90a1ad70080c";
	
	@Getter(AccessLevel.PUBLIC)
	private ServiceRequestFhirResourceProvider resourceProvider;
	
	@Mock
	private FhirServiceRequestService service;
	
	@Override
	public void setup() throws ServletException {
		resourceProvider = new ServiceRequestFhirResourceProvider();
		resourceProvider.setServiceRequestService(service);
		super.setup();
	}
	
	@Test
	public void getServiceRequestById_shouldReturnServiceRequest() throws Exception {
		ServiceRequest serviceRequest = new ServiceRequest();
		serviceRequest.setId(SERVICE_REQUEST_UUID);
		
		when(service.get(SERVICE_REQUEST_UUID)).thenReturn(serviceRequest);
		
		MockHttpServletResponse response = get("/ServiceRequest/" + SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON).go();
		
		MatcherAssert.assertThat(response, isOk());
		MatcherAssert.assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));
		MatcherAssert.assertThat(readResponse(response).getIdElement().getIdPart(), equalTo(SERVICE_REQUEST_UUID));
	}
	
	@Test
	public void getEncounterByWrongUuid_shouldReturn404() throws Exception {
		MockHttpServletResponse response = get("/ServiceRequest/" + WRONG_SERVICE_REQUEST_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		MatcherAssert.assertThat(response, isNotFound());
	}
}
