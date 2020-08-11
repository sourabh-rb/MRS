<% ui.decorateWith("appui", "standardEmrPage") %>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient]) }
 <style>
    #existing-encounters {
        margin-top: 2em;
    }
   
button{display:block;}
</style>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
<script src="http://code.jquery.com/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jsencrypt/2.3.1/jsencrypt.min.js"></script>

<script type="text/javascript">

\$(document).ready(function() {

var fileSelected = document.getElementById('txtfiletoread');

    fileSelected.addEventListener('change', function (e) { 
         //Set the extension for the file 
         var fileExtension = /text.*/; 
         //Get the file object 
         var fileTobeRead = fileSelected.files[0];
         console.log(fileTobeRead);
        //Check of the extension match 
         
             //Initialize the FileReader object to read the 2file 
             var fileReader = new FileReader(); 
             fileReader.onload = function (e) { 
                 console.log(fileReader.result); 
             } 
             fileReader.readAsText(fileTobeRead); 
         
    }, false);
    

var curhosp = "${currenthosp}";
console.log(curhosp);
var patient = "${patientlist.id}";
patient = patient.substring(1,patient.length-1);
console.log(patient);
var selecthosp = "${hosp}";
console.log(selecthosp);
selecthosp= selecthosp.substring(1,selecthosp.length-1);
console.log(selecthosp);
var hs = selecthosp.split(',');
console.log(hs);
console.log(hs.length);

var html ='';
for (i = 0; i < hs.length; i++) {
   html+= '<option value="' +hs[i]+ '">' +hs[i]+ '</option>';
    console.log(hs[i]);
}
console.log(html);
\$('#hosp_list').append(html);
});


 </script>
 

 
 <script type="text/javascript">

      // Call this code when the page is done loading.
      \$(function() {
       
        // Run a quick encryption/decryption when they click.
        \$('#testme').click(function() {

          // Encrypt with the public key...
          var encrypt = new JSEncrypt();
          encrypt.setPublicKey(\$('#pubkey').val());
          var encrypted = encrypt.encrypt(\$('#input').val());
          console.log(encrypted);

          // Decrypt with the private key...
          var decrypt = new JSEncrypt();
          decrypt.setPrivateKey(\$('#privkey').val());
          var uncrypted = decrypt.decrypt(encrypted);
		  console.log(uncrypted);
          // Now a simple check to see if the round-trip worked.
          if (uncrypted == \$('#input').val()) {
            alert('It works!!!');
          }
          else {
            alert('Something went wrong....');
          }
        });
      });
    </script>

<script>
 function GetItemValue(q) {
   var e = document.getElementById(q);
   var selValue = e.options[e.selectedIndex].text ;
   console.log(selValue);
  var id = "${patient.id}";
   window.open("http://localhost:8080/openmrs/exchange/getinfo.page?patientId="+id+"&option="+selValue);

}
   


</script> 

 
<select class = "hosp_list" width=150 style="width:150px" name="hosp_list" id="hosp_list">
   	 <option value=''>Hospital</option>
</select>

<button onClick="GetItemValue('hosp_list');">Choose</button>

<label for="privkey" style="display: none;">Private Key</label><br/>
    <textarea id="privkey" rows="15" cols="65" type="hidden" style="display: none;">-----BEGIN RSA PRIVATE KEY-----
MIICXQIBAAKBgQDlOJu6TyygqxfWT7eLtGDwajtNFOb9I5XRb6khyfD1Yt3YiCgQ
WMNW649887VGJiGr/L5i2osbl8C9+WJTeucF+S76xFxdU6jE0NQ+Z+zEdhUTooNR
aY5nZiu5PgDB0ED/ZKBUSLKL7eibMxZtMlUDHjm4gwQco1KRMDSmXSMkDwIDAQAB
AoGAfY9LpnuWK5Bs50UVep5c93SJdUi82u7yMx4iHFMc/Z2hfenfYEzu+57fI4fv
xTQ//5DbzRR/XKb8ulNv6+CHyPF31xk7YOBfkGI8qjLoq06V+FyBfDSwL8KbLyeH
m7KUZnLNQbk8yGLzB3iYKkRHlmUanQGaNMIJziWOkN+N9dECQQD0ONYRNZeuM8zd
8XJTSdcIX4a3gy3GGCJxOzv16XHxD03GW6UNLmfPwenKu+cdrQeaqEixrCejXdAF
z/7+BSMpAkEA8EaSOeP5Xr3ZrbiKzi6TGMwHMvC7HdJxaBJbVRfApFrE0/mPwmP5
rN7QwjrMY+0+AbXcm8mRQyQ1+IGEembsdwJBAN6az8Rv7QnD/YBvi52POIlRSSIM
V7SwWvSK4WSMnGb1ZBbhgdg57DXaspcwHsFV7hByQ5BvMtIduHcT14ECfcECQATe
aTgjFnqE/lQ22Rk0eGaYO80cc643BXVGafNfd9fcvwBMnk0iGX0XRsOozVt5Azil
psLBYuApa66NcVHJpCECQQDTjI2AQhFc1yRnCU/YgDnSpJVm1nASoRUnU8Jfm3Oz
uku7JUXcVpt08DFSceCEX9unCuMcT72rAQlLpdZir876
-----END RSA PRIVATE KEY-----</textarea><br/>

    <label for="pubkey" style="display: none;">Public Key</label><br/>
    <textarea id="pubkey" rows="15" cols="65" style="display: none;">-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDlOJu6TyygqxfWT7eLtGDwajtN
FOb9I5XRb6khyfD1Yt3YiCgQWMNW649887VGJiGr/L5i2osbl8C9+WJTeucF+S76
xFxdU6jE0NQ+Z+zEdhUTooNRaY5nZiu5PgDB0ED/ZKBUSLKL7eibMxZtMlUDHjm4
gwQco1KRMDSmXSMkDwIDAQAB
-----END PUBLIC KEY-----</textarea><br/>
    <label for="input" style="display: none;">Text to encrypt:</label><br/>
    <textarea id="input" name="input" type="text" rows=4 cols=70 style="display: none;">This is a test!</textarea><br/>
    <input id="testme" style="display: none;" type="button" value="Test Me!!!" /><br/>

<input type="file" id="txtfiletoread" style="display: none;" value="file:///G:/private_key_pkcs8.pem" />
