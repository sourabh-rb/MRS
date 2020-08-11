package org.openmrs.module.attachments.page.controller;

import static org.openmrs.module.attachments.AttachmentsContext.getContentFamilyMap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Visit;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.BlockchainManager;
import org.openmrs.module.attachments.fragment.controller.ClientConfigFragmentController;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class AttachmentsPageController {
	
	public Object controller(@RequestParam("patient") Patient patient,
	        @RequestParam(value = "visit", required = false) Visit visit, UiSessionContext sessionContext, UiUtils ui,
	        @InjectBeans AttachmentsContext context, @SpringBean DomainWrapperFactory domainWrapperFactory,
	        PageModel model) {
		//
		// The client-side config specific to the page
		//
		BlockchainManager blockchainManager = new BlockchainManager();
		
		PatientIdentifier id = patient.getPatientIdentifier("Blockchain UserId");
		System.out.println("Blockchain Id =");
		System.out.println(id);
		System.out.println(id.toString());
		String hospaddr = blockchainManager.queryaddr("hosp1");
		int status = blockchainManager.getuserpermission(id.toString(), hospaddr, "read");
		System.out.println(status);
		if (status == 1) {
			
			DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			Date dateobj = new Date();
			System.out.println(df.format(dateobj));
			Map<String, Object> jsonConfig = ClientConfigFragmentController.getClientConfig(context, ui);
			jsonConfig.put("patient", convertToRef(patient));
			
			VisitDomainWrapper visitWrapper = getVisitDomainWrapper(domainWrapperFactory, patient, visit,
			    context.getAdtService(), sessionContext.getSessionLocation());
			jsonConfig.put("visit", visitWrapper == null ? null : convertVisit(visitWrapper.getVisit()));
			
			jsonConfig.put("contentFamilyMap", getContentFamilyMap());
			jsonConfig.put("associateWithVisitAndEncounter", context.associateWithVisitAndEncounter());
			
			model.put("jsonConfig", ui.toJson(jsonConfig));
			
			// For Core Apps's patient header.
			model.put("patient", patient);
			return null;
		} else {
			return new Redirect("coreapps", "noAccess", "");
		}
	}
	
	protected VisitDomainWrapper getVisitDomainWrapper(DomainWrapperFactory domainWrapperFactory, Patient patient,
	        Visit visit, AdtService adtService, Location sessionLocation) {
		VisitDomainWrapper visitWrapper = null;
		if (visit == null) {
			// Fetching the active visit, if any.
			Location visitLocation = adtService.getLocationThatSupportsVisits(sessionLocation);
			visitWrapper = adtService.getActiveVisit(patient, visitLocation);
		} else {
			visitWrapper = domainWrapperFactory.newVisitDomainWrapper(visit);
		}
		return visitWrapper;
	}
	
	protected Object convertVisit(Object object) {
		return object == null ? null
		        : ConversionUtil.convertToRepresentation(object,
		            new CustomRepresentation(AttachmentsConstants.REPRESENTATION_VISIT));
	}
	
	protected Object convertToRef(Object object) {
		return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.REF);
	}
}
