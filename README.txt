Openmrs:
 To run OpenMRS server, open cygwin terminal and type the command:

 mvn -Dmaven.test.skip=true openmrs-sdk:run -DserverId=server

 where DserverId= name of the OpenMRS server alloted.
 Module codes are in C:/Users/HP. For any further questions, read OpenMRS SDK WIKI documentation.
 localhost:8080/openmrs/login.htm to access the login screen after the server starts completely.
 Modules developed in Eclipse for Java EE.
 Blockchain library functions are added in the modules that require them in the file BlockchainManager.java.
 
Android Client:
 Developed in Android studio for API 23+. Android Client folder contains the code for the Android App. Import project into Android studio to 
 make any modifications.

 Code for Arrhythmia detection present along with the app. Make sure Bluetooth, location and Internet connectivity is present while running the app.
 Also, the server should be online.

Blockchain:
 
 Blockchain Manager folder conatins the source code for the user application to grant and revoke permissions. The runnable jar file inside will open a login 
 screen when run.
 To run jar file,
 java -jar BlockchainApp.jar
 Username and password are user1 for the first user and user2 for the second.
 Make sure multichain is running on the node before using the application.
 Developed in Eclipse IDE.
 
 For queries regarding the commands and usage of MultiChain, refer to its documentation.
 
 
Other info: 
 IP Address:
 Hospital 1: 10.114.56.240
 Hospital 2: 10.114.56.245
 Our laptops were used as the User consoles
 User1: 10.114.56.250 
 User2: 10.114.56.251
 
 The blockchain address database is in the HOSPITAL 1 mySQL database in a database called addressdb and table address.
 
 System passwords:
 Hospital 1: password
 Hospital 2: hey!itsme