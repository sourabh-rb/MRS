package org.openmrs.module.coreapps.page.controller.labentry;

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
import org.openmrs.module.coreapps.helper.BreadcrumbHelper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.coreapps.BlockchainManager;


public class LabEntryPageController {
	
	 public Object get(@RequestParam("patientId") Patient patient, UiUtils ui, UiSessionContext emrContext, PageModel model,
			 @SpringBean("formService") FormService formService,
             @SpringBean("patientService") PatientService patientService,
             @SpringBean("adtService") AdtService adtService,
             @InjectBeans PatientDomainWrapper patientDomainWrapper) {
// model.addAttribute("afterSelectedUrl", app.getConfig().get("afterSelectedUrl").getTextValue());
// model.addAttribute("heading", app.getConfig().get("heading").getTextValue());
// BreadcrumbHelper.addBreadcrumbsIfDefinedInApp(app, model, ui);
		 
		 	BlockchainManager blockchainManager = new BlockchainManager();
	    	PatientIdentifier id = patient.getPatientIdentifier("Blockchain UserId");
	    	System.out.println("Blockchain Id =");
	    	System.out.println(id);
	    	System.out.println(id.toString());
	    	String hospaddr = blockchainManager.queryaddr("hosp2");
	    	int status = blockchainManager.getuserpermission(id.toString(), hospaddr , "read");
	    	System.out.println(status);
	    	if (status == 1) {
		 patientDomainWrapper.setPatient(patient);
		 SimpleObject appHomepageBreadcrumb = SimpleObject.create("label", ui.message("Laboratory Access"),
				    "link", ui.pageLink("coreapps", "findpatient/findPatient?app=coreapps.labEntry"));
		 SimpleObject patientPageBreadcrumb = SimpleObject.create("label",
				    patient.getFamilyName() + ", " + patient.getGivenName(), "link", ui.thisUrlWithContextPath());
		 
		 List<Form> labForm = formService.getAllForms(false);
			
			Location visitLocation = adtService.getLocationThatSupportsVisits(emrContext.getSessionLocation());
			VisitDomainWrapper activeVisit = adtService.getActiveVisit(patient, visitLocation);
			
			List<Encounter> existingEncounters = new ArrayList<Encounter>();
			if (activeVisit != null) {
				for (Encounter encounter : activeVisit.getVisit().getEncounters()) {
					if (!encounter.isVoided()) {
						existingEncounters.add(encounter);
					}
				}
			}
		 
		    model.addAttribute("visit", activeVisit != null ? activeVisit.getVisit() : null);
			model.addAttribute("existingEncounters", existingEncounters);
			model.addAttribute("patient", patientDomainWrapper);
			model.addAttribute("form", labForm);
			model.addAttribute("breadcrumbOverride", ui.toJson(Arrays.asList(appHomepageBreadcrumb, patientPageBreadcrumb)));
			return null;
}
	    	else {
	    		//TODO Implement no access response
	    		return new Redirect("coreapps", "noAccess", "");
	    	}
	 }

}
