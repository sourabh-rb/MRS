package org.openmrs.module.attachments.page.controller;

import java.util.Arrays;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.module.attachments.BlockchainManager;

public class AttachmentHomePageController {
	
	public Object get(@RequestParam("patientId") Patient patient, UiUtils ui, UiSessionContext emrContext, PageModel model,
	        @SpringBean("patientService") PatientService patientService, @SpringBean("adtService") AdtService adtService,
	        @InjectBeans PatientDomainWrapper patientDomainWrapper) {
		
		BlockchainManager blockchainManager = new BlockchainManager();
		
		PatientIdentifier id = patient.getPatientIdentifier("Blockchain UserId");
		System.out.println("Blockchain Id =");
		System.out.println(id);
		System.out.println(id.toString());
		String hospaddr = blockchainManager.queryaddr("hosp1");
		int status = blockchainManager.getuserpermission(id.toString(), hospaddr, "read");
		System.out.println(status);
		if (status == 1) {
			patientDomainWrapper.setPatient(patient);
			SimpleObject appHomepageBreadcrumb = SimpleObject.create("label", ui.message("Laboratory Access"), "link",
			    ui.pageLink("coreapps", "findpatient/findPatient?app=attachments.attachments"));
			SimpleObject patientPageBreadcrumb = SimpleObject.create("label",
			    patient.getFamilyName() + ", " + patient.getGivenName(), "link", ui.thisUrlWithContextPath());
			
			Location visitLocation = adtService.getLocationThatSupportsVisits(emrContext.getSessionLocation());
			VisitDomainWrapper activeVisit = adtService.getActiveVisit(patient, visitLocation);
			
			model.addAttribute("visit", activeVisit != null ? activeVisit.getVisit() : null);
			model.addAttribute("patient", patientDomainWrapper);
			model.addAttribute("breadcrumbOverride", ui.toJson(Arrays.asList(appHomepageBreadcrumb, patientPageBreadcrumb)));
			return null;
		} else {
			
			return new Redirect("coreapps", "noAccess", "");
		}
	}
}
