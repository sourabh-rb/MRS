package org.openmrs.module.coreapps.page.controller.exchange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.BlockchainManager;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.coreapps.contextmodel.PatientContextModel;
import org.openmrs.module.coreapps.contextmodel.VisitContextModel;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

public class PatientexPageController {
	
	public Object controller(@RequestParam("patientId") String patientId, PageModel model,
	        @RequestParam("hosp") String hosp, 
	        @RequestParam(required = false, value = "dashboard") String dashboard,
	        @RequestParam(required = false, value = "app") AppDescriptor app,
	        @InjectBeans PatientDomainWrapper patientDomainWrapper,
	        @SpringBean("adtService") AdtService adtService, @SpringBean("visitService") VisitService visitService,
	        @SpringBean("encounterService") EncounterService encounterService,
	        @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
	        @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
	        @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
	        @SpringBean("coreAppsProperties") CoreAppsProperties coreAppsProperties, UiSessionContext sessionContext) throws Exception {
		
		Context.authenticate("guest", "Guest123");
		System.out.println(Context.getAuthenticatedUser().toString());
		BlockchainManager blockchainManager = new BlockchainManager();
    	String hospaddr = blockchainManager.queryaddr(hosp);
    	
    	
    	
		PublicKey publicKey = getPemPublicKey();
		System.out.println(patientId);
		patientId = decrypt(patientId, publicKey);
		System.out.println(patientId);
		int status = blockchainManager.getuserpermission(patientId, hospaddr , "read");
		if (status == 1) {
		List<PatientIdentifierType> identifierTypes = new ArrayList<PatientIdentifierType>();
		identifierTypes.add(Context.getPatientService().getPatientIdentifierType(4));
		List<Patient> patientlist = Context.getPatientService().getPatients(null, patientId, identifierTypes, true);
		Patient patient = patientlist.get(0);
		if (!Context.hasPrivilege(CoreAppsConstants.PRIVILEGE_PATIENT_DASHBOARD)) {
			return new Redirect("coreapps", "noAccess", "");
		} else if (patient.isVoided() || patient.isPersonVoided()) {
			return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
		}
		
		if (StringUtils.isEmpty(dashboard)) {
            dashboard = "patientDashboard";
        }
		
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		Date dateobj = new Date();
		//System.out.println(df.format(dateobj));

		blockchainManager.publishtostream(patientId, "hosp2", "Exchange "+hosp+" "+df.format(dateobj));
		System.out.print(patient.getId());
		patientDomainWrapper.setPatient(patient);
		System.out.println(patientDomainWrapper);
		model.addAttribute("patient", patientDomainWrapper);
		model.addAttribute("app", app);
		 
		Location visitLocation = null;
        try {
            visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
        }
        catch (IllegalArgumentException ex) {
            // location does not support visits
        }

        VisitDomainWrapper activeVisit = null;
        if (visitLocation != null) {
            activeVisit = adtService.getActiveVisit(patient, visitLocation);
        }
        model.addAttribute("activeVisit", activeVisit);
        AppContextModel contextModel = sessionContext.generateAppContextModel();
        contextModel.put("patient", new PatientContextModel(patient));
        contextModel.put("visit", activeVisit == null ? null : new VisitContextModel(activeVisit));
        model.addAttribute("appContextModel", contextModel);

        List<Extension> overallActions = appFrameworkService.getExtensionsForCurrentUser(dashboard + ".overallActions", contextModel);
        Collections.sort(overallActions);
        model.addAttribute("overallActions", overallActions);

        List<Extension> visitActions;
        if (activeVisit == null) {
            visitActions = new ArrayList<Extension>();
        } else {
            visitActions = appFrameworkService.getExtensionsForCurrentUser(dashboard + ".visitActions", contextModel);
            Collections.sort(visitActions);
        }
        model.addAttribute("visitActions", visitActions);

        List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser(dashboard + ".includeFragments", contextModel);
        Collections.sort(includeFragments);
        model.addAttribute("includeFragments", includeFragments);

        List<Extension> firstColumnFragments = appFrameworkService.getExtensionsForCurrentUser(dashboard + ".firstColumnFragments", contextModel);
        Collections.sort(firstColumnFragments);
        model.addAttribute("firstColumnFragments", firstColumnFragments);
        System.out.println("First Column\n");
        System.out.println(firstColumnFragments);
        List<Extension> secondColumnFragments = appFrameworkService.getExtensionsForCurrentUser(dashboard + ".secondColumnFragments", contextModel);
        Collections.sort(secondColumnFragments);
        model.addAttribute("secondColumnFragments", secondColumnFragments);
        System.out.println("second Column\n");
        System.out.println(secondColumnFragments);
        List<Extension> otherActions = appFrameworkService.getExtensionsForCurrentUser(
                (dashboard == "patientDashboard" ? "clinicianFacingPatientDashboard" : dashboard) + ".otherActions", contextModel);
        Collections.sort(otherActions);
        model.addAttribute("otherActions", otherActions);

        model.addAttribute("baseDashboardUrl", coreAppsProperties.getDashboardUrl());  // used for breadcrumbs to link back to the base dashboard in the case when this is used to render a context-specific dashboard
        model.addAttribute("dashboard", dashboard);
        System.out.println("Dashboard \n");
        System.out.println(dashboard);
        applicationEventService.patientViewed(patient, sessionContext.getCurrentUser());

        return null;
		}
		
		else {
    		return new Redirect("coreapps", "noAccess", "");
    	}
	
	}
	
	 public static String encrypt(String rawText, PrivateKey privateKey) throws IOException, GeneralSecurityException {
	        Cipher cipher = Cipher.getInstance("RSA");
	        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
	        return Base64.getUrlEncoder().withoutPadding().encodeToString(cipher.doFinal(rawText.getBytes("UTF-8")));
}
	 
	 public static String decrypt(String cipherText, PublicKey publicKey) throws IOException, GeneralSecurityException {
	        Cipher cipher = Cipher.getInstance("RSA");
	        cipher.init(Cipher.DECRYPT_MODE, publicKey);
	        return new String(cipher.doFinal(Base64.getUrlDecoder().decode(cipherText)), "UTF-8");
	    }
	 
	 public  static PrivateKey getPemPrivateKey() throws Exception {
		 String privateKeyContent = new String(Files.readAllBytes(Paths.get("D:\\private_key_pkcs8.pem")));
		 privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
		 System.out.println(privateKeyContent);
		 KeyFactory kf = KeyFactory.getInstance("RSA");
		 PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
		 PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
		 return privKey;
	 }

	   public static  PublicKey getPemPublicKey() throws Exception {
		   String publicKeyContent = new String(Files.readAllBytes(Paths.get("D:\\public_key_hosp1.pem")));
		   publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");;
		   System.out.println(publicKeyContent);
		   KeyFactory kf = KeyFactory.getInstance("RSA");
		   X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
		   PublicKey pubKey = (PublicKey) kf.generatePublic(keySpecX509);
		   return pubKey;
	      }
	
	
}
