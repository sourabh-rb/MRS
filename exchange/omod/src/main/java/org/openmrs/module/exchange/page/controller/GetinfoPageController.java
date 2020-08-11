package org.openmrs.module.exchange.page.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.PatientService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;

public class GetinfoPageController {
	
	List<String> cookies;
	
	HttpURLConnection conn;
	
	@Autowired
	public void controller(@RequestParam("patientId") Patient patient, @RequestParam("option") String option,
	        UiSessionContext emrContext, UiUtils ui, @SpringBean("patientService") PatientService patientService,
	        @InjectBeans PatientDomainWrapper patientDomainWrapper, PageModel model,
	        HttpServletResponse httpServletResponse, HttpServletRequest request) throws Exception {
		
		System.out.println("in get info controller");
		patientDomainWrapper.setPatient(patient);
		
		String bIdentifier = patient.getPatientIdentifier("Blockchain UserId").toString();
		//System.out.println(bIdentifier);
		
		String string = "guest:Guest123";
		String credential = Base64.getEncoder().encodeToString(string.getBytes("UTF-8"));
		System.out.println(credential);
		
		PrivateKey pvt = getPemPrivateKey();
		bIdentifier = encrypt(bIdentifier, pvt);
		//System.out.println(bIdentifier);
		
		//		HttpClient httpClient = new HttpClient("http://10.114.56.245:8080/openmrs/login.htm");
		//		
		//		Map<String, String> map = new HashMap<String, String>();
		//		map.put("username", "admin");
		//		map.put("password", "Admin123");
		//		map.put("sessionLocation", "6");
		//		map.put("redirectUrl",
		//		    "http://10.114.56.245:8080/openmrs/coreapps/exchange/patientex.page?patientId=user1&hosp=hosp1");
		//		String response = httpClient.post(map);
		//		
		//		//	System.out.println(response);
		//		System.out.println(request.getCookies());
		//		System.out.println(request.getSession());
		
		//		String url = "http://10.114.56.245:8080/openmrs/login.htm";
		//		String redirecturl = "http://10.114.56.245:8080/openmrs/coreapps/exchange/patientex.page?patientId=" + bIdentifier
		//		        + "&hosp=hosp1";
		//		
		//		Connection.Response loginForm = Jsoup.connect(url).method(Connection.Method.GET).timeout(0).execute();
		//		Connection.Response loginpost = Jsoup.connect(url).data("username", "guest").data("password", "Guest123")
		//		        .data("sessionLocation", "6").data("redirectUrl", redirecturl).followRedirects(true)
		//		        .cookies(loginForm.cookies()).method(Connection.Method.POST).execute();
		//		System.out.println(loginForm.cookies());
		//		System.out.println(loginpost.cookies());
		//		
		//		String string2 = loginForm.cookie("JSESSIONID");
		//		System.out.println(string2);
		//		String string3 = loginpost.cookie("referenceapplication.lastSessionLocation");
		//		String string4 = loginpost.cookie("_REFERENCE_APPLICATION_LAST_USER_");
		//		Cookie cookie = new Cookie("JSESSIONID", string2);
		//		Cookie cookie2 = new Cookie("referenceapplication.lastSessionLocation", string3);
		//		Cookie cookie3 = new Cookie("_REFERENCE_APPLICATION_LAST_USER_", string4);
		//		URL urlToRedirect = new URL(redirecturl);
		//		cookie.setDomain(urlToRedirect.getHost());
		//		cookie.setPath("http://10.114.56.245:8080/openmrs");
		//		
		//		httpServletResponse.addCookie(cookie);
		//		httpServletResponse.addCookie(cookie2);
		//		httpServletResponse.addCookie(cookie3);
		//		httpServletResponse.addHeader("Location",
		//		    "http://10.114.56.245:8080/openmrs/coreapps/exchange/patientex.page?patientId=" + bIdentifier + "&hosp=hosp1");
		//		httpServletResponse
		//		        .encodeRedirectURL("http://10.114.56.245:8080/openmrs/coreapps/exchange/patientex.page?patientId="
		//		                + bIdentifier + "&hosp=hosp1");
		httpServletResponse.setHeader("Authorization", "Basic " + credential);
		httpServletResponse.sendRedirect("http://10.114.56.245:8080/openmrs/coreapps/exchange/patientex.page?patientId="
		        + bIdentifier + "&hosp=hosp1");
		
	}
	
	private void sendPost(String url, String postParams, String redirecturl) throws Exception {
		
		URL obj = new URL(url);
		conn = (HttpURLConnection) obj.openConnection();
		
		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		for (String cookie : this.cookies) {
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("redirectUrl", redirecturl);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		
		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();
		
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		// System.out.println(response.toString());
		
	}
	
	private String GetPageContent(String url) throws Exception {
		
		URL obj = new URL(url);
		conn = (HttpURLConnection) obj.openConnection();
		
		// default is GET
		conn.setRequestMethod("GET");
		conn.setUseCaches(false);
		
		// act like a browser
		
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (cookies != null) {
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		// Get the response cookies
		setCookies(conn.getHeaderFields().get("Set-Cookie"));
		
		return response.toString();
		
	}
	
	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
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
	
	public static PrivateKey getPemPrivateKey() throws Exception {
		String privateKeyContent = new String(Files.readAllBytes(Paths.get("G:\\private_key_pkcs8.pem")));
		privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "")
		        .replace("-----END PRIVATE KEY-----", "");
		System.out.println(privateKeyContent);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
		PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
		return privKey;
	}
	
	public static PublicKey getPemPublicKey() throws Exception {
		String publicKeyContent = new String(Files.readAllBytes(Paths.get("G:\\public_key.pem")));
		publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "")
		        .replace("-----END PUBLIC KEY-----", "");
		;
		System.out.println(publicKeyContent);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
		PublicKey pubKey = (PublicKey) kf.generatePublic(keySpecX509);
		return pubKey;
	}
	
}
