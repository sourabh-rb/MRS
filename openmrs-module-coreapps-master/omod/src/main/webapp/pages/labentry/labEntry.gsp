<%
    ui.decorateWith("appui", "standardEmrPage")
    
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("Laboratory") }", link: "${ ui.pageLink("coreapps", "findpatient/findPatient", [app: "coreapps.labEntry"]) }" },
        { label: "${ ui.escapeJs(ui.encodeHtmlContent(ui.format(patient.patient))) }" , link: '${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.id])}'},
    ];
</script>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient]) }


<style>
    #existing-encounters {
        margin-top: 2em;
    }
   

   button{display:block;}
</style>

<script type="text/javascript">

var object = "${form?.uuid}";
var formname = "${form?.name}";
console.log(object);
console.log(formname);
formname = formname.substring(1, formname.length-1);
var newstr = object.substring(1, object.length-1);
formname = formname.split(' ').join('');
var str= newstr.split(' ').join('');
formname = formname.split(',');
str = str.split(',');
console.log(str);
console.log(formname);
var obj = JSON.stringify(str);
obj = jQuery.parseJSON(obj); 


jq(function() {

var visitvar = "${visit?.id}";
console.log(visitvar);

 var buttons;
 console.log(formname);
 console.log(obj);
if( visitvar != "null"){
for(var i=0; i<formname.length; ++i){
var btn = document.createElement('input');
btn.type = "button";
btn.style = "width:80%";
btn.value = formname[i];
btn.id = obj[i];

btn.onclick = function(){
var id = \$(this).attr('id');
console.log(id);
 emr.navigateTo({
                provider: "htmlformentryui",
                page: "htmlform/enterHtmlFormWithSimpleUi",
                query: {
                    patientId: "${ patient.id }",
                    visitId: "${ visit?.id }",
                    formUuid: id,
                    returnUrl: "${ ui.escapeJs(ui.pageLink("coreapps", "findpatient/findPatient?app=coreapps.labEntry")) }",
                    breadcrumbOverride: "${ ui.escapeJs(breadcrumbOverride) }"
                }
            });
}
console.log(btn);
\$("#btn-group").append(btn);
document.body.appendChild(btn);
}
}
 });

</script>

<% if (visit) { %>

	 <div id="button" class="btn-group">
  
</div> 
    

<% } else { %>

    <h1>
        ${ ui.message("coreapps.vitals.noVisit") }
    </h1>

    <div id="actions">
        <button class="cancel big">
            <i class="icon-arrow-left"></i>
            ${ ui.message("coreapps.vitals.noVisit.findAnotherPatient") }
        </button>
    </div>

<% } %>