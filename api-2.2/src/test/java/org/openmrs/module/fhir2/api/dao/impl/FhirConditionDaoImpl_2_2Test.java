/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import java.util.Date;

import org.exparity.hamcrest.date.DateMatchers;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirConditionDaoImpl_2_2Test extends BaseModuleContextSensitiveTest {
	
	private static final String CONDITION_UUID = "2cc6880e-2c46-15e4-9038-a6c5e4d22fb7";
	
	private static final String EXISTING_CONDITION_UUID = "2cc6880e-2c46-11e4-9138-a6c5e4d20fb7";
	
	private static final String WRONG_CONDITION_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private static final String CONDITION_INITIAL_DATA_XML = "org/openmrs/api/include/ConditionServiceImplTest-SetupCondition.xml";
	
	private static final String END_REASON = "End reason";
	
	private static final Integer PATIENT_ID = 2;
	
	@Inject
	@Named("sessionFactory")
	private Provider<SessionFactory> sessionFactoryProvider;
	
	@Inject
	private PatientService patientService;
	
	@Inject
	private FhirConditionDaoImpl_2_2 dao;
	
	@Before
	public void setUp() {
		dao = new FhirConditionDaoImpl_2_2();
		dao.setSessionFactory(sessionFactoryProvider.get());
		executeDataSet(CONDITION_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldRetrieveConditionByUuid() {
		Condition condition = dao.getConditionByUuid(CONDITION_UUID);
		assertThat(condition, notNullValue());
		assertThat(condition.getUuid(), notNullValue());
		assertThat(condition.getUuid(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldReturnNullWhenGetConditionByWrongUuid() {
		Condition condition = dao.getConditionByUuid(WRONG_CONDITION_UUID);
		assertThat(condition, nullValue());
	}
	
	@Test
	public void shouldSaveNewCondition() {
		Condition condition = new Condition();
		condition.setUuid(CONDITION_UUID);
		condition.setOnsetDate(new Date());
		
		Patient patient = patientService.getPatient(PATIENT_ID);
		condition.setPatient(patient);
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(CONDITION_UUID));
	}
	
	@Test
	public void shouldReturnExistingConditionIfBothAreEquals() {
		Condition condition = new Condition();
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setPatient(patientService.getPatient(PATIENT_ID));
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(EXISTING_CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result, equalTo(dao.getConditionByUuid(EXISTING_CONDITION_UUID)));
	}
	
	@Test
	public void shouldUpdateExistingCondition() {
		Condition condition = new Condition();
		condition.setUuid(EXISTING_CONDITION_UUID);
		
		condition.setPatient(patientService.getPatient(PATIENT_ID));
		condition.setOnsetDate(new Date());
		condition.setClinicalStatus(ConditionClinicalStatus.HISTORY_OF);
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(EXISTING_CONDITION_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(EXISTING_CONDITION_UUID));
		assertThat(result.getPatient(), equalTo(patientService.getPatient(PATIENT_ID)));
		assertThat(result.getEndDate(), notNullValue());
		assertThat(result.getEndDate(), DateMatchers.sameDay(condition.getOnsetDate()));
	}
	
	@Test
	public void shouldSetConditionVoidedStatusTrue() {
		Condition condition = new Condition();
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setPatient(patientService.getPatient(PATIENT_ID));
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		
		dao.saveCondition(condition);
		Condition result = dao.getConditionByUuid(EXISTING_CONDITION_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getVoided(), is(true));
	}
}
