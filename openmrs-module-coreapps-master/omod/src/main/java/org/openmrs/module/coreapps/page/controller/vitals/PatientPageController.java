/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.coreapps.page.controller.vitals;

import static org.hamcrest.CoreMatchers.nullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.FormService;
import org.openmrs.api.PatientService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.module.coreapps.BlockchainManager;

public class PatientPageController {
	
	@Autowired
	public Object controller(@RequestParam("patientId") Patient patient, UiUtils ui, UiSessionContext emrContext, PageModel model,
	                       @SpringBean("formService") FormService formService,
	                       @SpringBean("patientService") PatientService patientService,
	                       @SpringBean("adtService") AdtService adtService,
	                       @InjectBeans PatientDomainWrapper patientDomainWrapper) {
		
		BlockchainManager blockchainManager = new BlockchainManager();
    	
    	PatientIdentifier id = patient.getPatientIdentifier("Blockchain UserId");
    	System.out.println("Blockchain Id =");
    	System.out.println(id.toString());
    	String hospaddr = blockchainManager.queryaddr("hosp2");
    	int status = blockchainManager.getuserpermission(id.toString(), hospaddr , "read");
    	System.out.println(status);
		if (status ==1) {
		patientDomainWrapper.setPatient(patient);
		
		SimpleObject appHomepageBreadcrumb = SimpleObject.create("label", ui.message("referenceapplication.app.capturevitals.title"),
		    "link", ui.pageLink("coreapps", "findpatient/findPatient?app=referenceapplication.vitals"));
		SimpleObject patientPageBreadcrumb = SimpleObject.create("label",
		    patient.getFamilyName() + ", " + patient.getGivenName(), "link", ui.thisUrlWithContextPath());
		
		Form vitalsForm = formService.getFormByUuid("a000cb34-9ec1-4344-a1c8-f692232f6edd");
		
		Location visitLocation = adtService.getLocationThatSupportsVisits(emrContext.getSessionLocation());
		VisitDomainWrapper activeVisit = adtService.getActiveVisit(patient, visitLocation);
		
		List<Encounter> existingEncounters = new ArrayList<Encounter>();
		if (activeVisit != null) {
			for (Encounter encounter : activeVisit.getVisit().getEncounters()) {
				if (!encounter.isVoided() && vitalsForm.equals(encounter.getForm())) {
					existingEncounters.add(encounter);
				}
			}
		}
		
		model.addAttribute("visit", activeVisit != null ? activeVisit.getVisit() : null);
		model.addAttribute("existingEncounters", existingEncounters);
		model.addAttribute("patient", patientDomainWrapper);
		model.addAttribute("breadcrumbOverride", ui.toJson(Arrays.asList(appHomepageBreadcrumb, patientPageBreadcrumb)));
		return null;
	}
		else {
			//TODO What should be done in case of no Access?
			System.out.println("No Access");
			return new Redirect("coreapps", "noAccess", "");
		}
	}
	
}
