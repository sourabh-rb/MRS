<%
    ui.decorateWith("appui", "standardEmrPage")
    
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("Attachments") }", link: "${ ui.pageLink("coreapps", "findpatient/findPatient", [app: "attachments.attachments"]) }" },
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

function buttonclick(){

emr.navigateTo({
                provider: "attachments",
                page: "attachments",
                query: {
               		patient: "${ patient.id }",
                    visitId: "${ visit?.id }",
                    returnUrl: "${ ui.escapeJs(ui.pageLink("coreapps", "findpatient/findPatient?app=attachments.attachments")) }",
                    breadcrumbOverride: "${ ui.escapeJs(breadcrumbOverride) }"
                }
            });
 }

</script>

<script type="text/javascript"> 
var visitvar = "${visit?.id}";
console.log(visitvar);
if( visitvar != "null"){
console.log("Visit not null");
}
else{
console.log("No active visits");
}
</script>

<% if (visit) { %>
<button id="btn" onclick="buttonclick()" >Are you sure you want to proceed? If Yes, click here !</button>

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