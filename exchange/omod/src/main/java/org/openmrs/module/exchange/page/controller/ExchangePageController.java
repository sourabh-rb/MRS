package org.openmrs.module.exchange.page.controller;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class ExchangePageController {
	
	public Object controller(@RequestParam("patient") Patient patient, UiSessionContext emrContext, UiUtils ui,
	        @SpringBean("patientService") PatientService patientService,
	        @InjectBeans PatientDomainWrapper patientDomainWrapper, PageModel model) {
		
		patientDomainWrapper.setPatient(patient);
		String fileName = "C:\\Users\\HP\\hosp.txt";
		ArrayList<String> hospchoices = new ArrayList<String>();
		String line = null;
		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(fileName);
			
			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			while ((line = bufferedReader.readLine()) != null) {
				//System.out.println(line);
				hospchoices.add(line);
			}
			bufferedReader.close();
		}
		catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		}
		catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
			// Or we could just do this: 
			// ex.printStackTrace();
		}
		User user = Context.getAuthenticatedUser();
		System.out.println(user.toString());
		List<PatientIdentifierType> identifierTypes = new ArrayList<PatientIdentifierType>();
		identifierTypes.add(Context.getPatientService().getPatientIdentifierType(4));
		List<Patient> patientlist = Context.getPatientService().getPatients(null, "user1", identifierTypes, true);
		model.addAttribute("patient", patientDomainWrapper);
		model.addAttribute("hosp", hospchoices);
		model.addAttribute("currenthosp", "hosp1");
		model.addAttribute("patientlist", patientlist);
		return null;
		
	}
}
