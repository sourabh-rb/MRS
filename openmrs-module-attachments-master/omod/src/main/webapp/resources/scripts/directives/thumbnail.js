angular.module('att.widget.thumbnail')

.config(['$compileProvider', function ($compileProvider) {
  /* Prevent Angular from throwing error when querying images using 'data' protocol */
  $compileProvider.imgSrcSanitizationWhitelist(/^\s*(https?|local|data):/);
}])

.directive('attEnterKeyDown', function() {
  return function(scope, element, attrs) {
    element.bind("keydown keypress", function(event) {
      if(event.which === 13) {
        scope.$apply(function() {
          scope.$eval(attrs.attEnterKeyDown, {'event': event});
        });
        event.preventDefault();
      }
    });
  };
})

.directive('attEscapeKeyDown', function() {
  return function(scope, element, attrs) {
    element.bind("keydown keypress", function(event) {
      if(event.which === 27) {
        scope.$apply(function() {
          scope.$eval(attrs.attEscapeKeyDown, {'event': event});
        });
        event.preventDefault();
      }
    });
  };
})

.directive('attThumbnail', ['Attachment', 'ComplexObsCacheService', 'ModuleUtils', 'ngDialog', '$http', '$window', '$sce', function(Attachment, obsCache, module, ngDialog, $http, $window, $sce) {
  return {
    restrict: 'E',
    scope: {
      obs: '=',
      config: '='
    },
    templateUrl: '/' + module.getPartialsPath(OPENMRS_CONTEXT_PATH) + '/thumbnail.html',

    controller: function($scope) {

      var msgCodes = [
        module.getProvider() + ".misc.label.enterCaption",
        module.getProvider() + ".thumbail.get.error",
        module.getProvider() + ".thumbail.save.success",
        module.getProvider() + ".thumbail.save.error",
        module.getProvider() + ".thumbail.delete.success",
        module.getProvider() + ".thumbail.delete.error",
        module.getProvider() + ".attachmentspage.delete.title",
        module.getProvider() + ".attachmentspage.delete.confirm",
        "coreapps.yes",
        "coreapps.no"
      ]
      emr.loadMessages(msgCodes.toString(), function(msgs) {
        $scope.msgs = msgs;
      });
      moment.locale($scope.config.locale);

      $scope.canEdit = function() {
        if($scope.config.canEdit) {
          if($scope.config.canEdit === true) {
            return true;
          }
        }
        return false;
      }

      $scope.toggleVisible = function(visible) {
        $scope.active = visible;
      }

      $scope.toggleEditMode = function(editMode) {
        $scope.typedText = {};
        if ($scope.canEdit()) {
          $scope.typedText.newCaption = $scope.obs.comment;
          $scope.editMode = editMode;
          if ($scope.editMode) {
            $scope.editModeCss = "att_thumbnail-edit-mode";
          }
          else {
            $scope.editModeCss = "";
          }
        }
      }

      $scope.toggleEditMode(false);
      $scope.toggleVisible(true);
      $scope.src = "";
      $scope.loading = false;

      $scope.getEditModeCss = function() {
        return $scope.editModeCss;
      }

      $scope.getPrettyDate = function() {
        var timeFormat = "DD MMM YY";
        var now = new moment();
        var obsDate = moment($scope.obs.obsDatetime);  // new Date(..) would throw 'Invalid date' on Apple WebKit
        if ( ( obsDate.year() == now.year() ) && ( obsDate.dayOfYear() == now.dayOfYear() ) ) {
          timeFormat = "HH:mm";
        }
        else {
          if (obsDate.year() === now.year()) {
            timeFormat = "DD MMM";
          }
        }
        return moment(obsDate).format(timeFormat);
      }

      $scope.saveCaption = function() {

        var caption = $scope.obs.comment;
        if ((caption == $scope.typedText.newCaption) || ($scope.typedText.newCaption == "" && !$scope.config.allowNoCaption)) {
          $scope.toggleEditMode(false);
          return;
        }

        $scope.obs.comment = $scope.typedText.newCaption;

        var saved = Attachment.save({
          uuid: $scope.obs.uuid,
          comment: $scope.obs.comment
        });
        saved.$promise.then(function(attachment) {
          $scope.obs.uuid = attachment.uuid;
          $scope.toggleEditMode(false);
          emr.successMessage(module.getProvider() + ".thumbail.save.success");
        }, function(err) {
          $scope.obs.comment = caption;
          emr.errorMessage(module.getProvider() + ".thumbail.save.error");
          console.log(err);
        });
      }

      $scope.confirmDelete = function() {
        // https://github.com/likeastore/ngDialog/blob/master/README.md
        ngDialog.open({
          template: '/' + module.getPartialsPath(OPENMRS_CONTEXT_PATH) + '/deleteDialog.html',
          scope: $scope,
          controller: ['$scope', function($scope) {
            $scope.showSpinner = false;
            $scope.confirm = function() {
              $scope.showSpinner = true;
              $scope.purge(true, $scope);
            }
          }]
        });
      }

      $scope.purge = function(purge, scope) {
        Attachment.delete({
          uuid: scope.obs.uuid,
          purge: purge
        })
        .$promise.then(function(res) {
          scope.toggleVisible(false);
          scope.closeThisDialog();
          emr.successMessage(module.getProvider() + ".thumbail.delete.success");
        }, function(err) {
          scope.closeThisDialog();
          if (purge === true) { // We should only do this if error 500 is the cause: https://github.com/openmrs/openmrs-core/blob/1.11.x/api/src/main/java/org/openmrs/api/impl/ObsServiceImpl.java#L213
            scope.purge(null, scope);
          }
          else {
            emr.errorMessage(module.getProvider() + ".thumbail.delete.error");
            console.log(err);
          }
        }); 
      }

      /*
        Injects the icon's HTML into the DOM. We had to avoid an in-DOM ng-if (or ng-switch) due to performance issues.
      */
      var setIconHtml = function(complexObs) {

        if (complexObs.contentFamily === module.family.IMAGE) {
          $scope.imageUrl = 'data:' + complexObs.mimeType + ';base64,' + module.arrayBufferToBase64(complexObs.complexData);
        }

        var html = "";
        switch (complexObs.contentFamily) {
          case module.family.IMAGE:
            html =  '<img src="' +
                    'data:' + complexObs.mimeType + ';base64,' + module.arrayBufferToBase64(complexObs.complexData) +
                    '"/>';
            break;

          case module.family.PDF:
            html =  '<i class="icon-file-pdf-o"></i>' +
                    '<span class="att_thumbnail-extension">' + complexObs.contentFamily.toUpperCase() + '</span>';
            break;
            
          case module.family.CSV:
              
              html =  '<i class="icon-file"></i>' +
                      '<span class="att_thumbnail-extension">.' + complexObs.fileExt + '</span>';
              console.info("CSV");
              break;

          case module.family.OTHER:
          default:
        	  console.log("OTHER");
            html =  '<i class="icon-file"></i>' +
                    '<span class="att_thumbnail-extension">.' + complexObs.fileExt + '</span>';
            break;
        }
        $scope.iconHtml = $sce.trustAsHtml(html);
      }

      $scope.init = function() {
        $scope.displayDefaultContentFamily = true;

        obsCache.getComplexObs($scope.obs, $scope.config.downloadUrl, $scope.config.thumbView)
        .then(function(res) {
          $scope.loading = false;
          $scope.obs = res.obs;
          $scope.obs.complexData = res.complexData; // Turning the obs into a complex obs.
          setIconHtml($scope.obs);
        }, function(err) {
          $scope.loading = false;
          emr.errorMessage(module.getProvider() + ".thumbail.get.error");
          console.log(err);
        });
      }

      $scope.displayContent = function() {
        var win = getWindow($scope.obs.contentFamily);

        $scope.loading = true;
        obsCache.getComplexObs($scope.obs, $scope.config.downloadUrl, $scope.config.originalView)
        .then(function(res) {
          $scope.loading = false;
          switch ($scope.obs.contentFamily) {
            case module.family.IMAGE:
              displayImage($scope.obs, res.complexData);
              break;

            case module.family.PDF:
              displayPdf($scope.obs, res.complexData, win);
              break;
              
            case module.family.CSV:
            	console.info("in CSV");
            	displayCsv($scope.obs, res.complexData, win);
            	break;

            case module.family.OTHER:
            default:
              console.info("in OTHER");
              displayOther($scope.obs, res.complexData);
              break;
          }
        }, function(err) {
          $scope.loading = false;
          emr.errorMessage(module.getProvider() + ".thumbail.get.error");
          console.log(err);
        });
      }

      var displayImage = function(obs, data) {
        $scope.imageConfig = {};
        $scope.imageConfig.bytes = module.arrayBufferToBase64(data);
        $scope.imageConfig.mimeType = obs.mimeType;
        $scope.imageConfig.caption = obs.comment;
      }

      var displayPdf = function(obs, data, win) {
        var blob = new Blob([data], {type: obs.mimeType});
        var blobUrl = URL.createObjectURL(blob);
        win.location.href = blobUrl;
      }
      
      var displayCsv = function(obs, data, win){
     	 
    	  console.info("Inside display CSV ");
    	  console.info(data);
    	 // var chart = getChart(obs,data);
    	 // chart.draw();
    	 // console.info("Drew chart");
    	  //$scope.chartURI = chart.getImageURI();
    	  var blob = new Blob([data], {type: "text/plain"});
    	  console.info(blob);
    	  var myReader = new FileReader();
    	//handler executed once reading(blob content referenced to a variable) from blob is finished. 
    	  myReader.onload = function() {
    		    var text = myReader.result;
    		    var text1 = text.split(/\r?\n/);
    		    console.info(text1);
    		   var array1 = []; // better to define using [] instead of new Array();
    		   var array2 = [];
    		   var array3 = [];
    		   var array4 = [];
    		   var sh ="3";
    		    for (var i = 0; i < text1.length-1; i++) {
    		        var split = text1[i].split(",");  // just split once
    		        array1.push(parseFloat(split[0])); // before the dot
    		       // array2.push(parseFloat(split[1])*0.0025); // after the dot
    		       // console.log(split[2]);
    		        //console.log(split.length);
    		        if(split.length > sh){
    		        array2.push(parseFloat(split[1])*0.0001);
    		        array3.push(parseFloat(split[2])*0.0001);
    		        array4.push(parseFloat(split[3])*0.0001);
    		        }
    		        else{
    		        	array2.push(parseFloat(split[1])*0.0025);
    		        }
    		    	}
    		    console.log(split.length);
    		    console.log(sh);
    		    if(split.length > sh){
    		    	drawChart2(array1,array2,array3,array4);
    		    }
    		    else{
    		    drawChart(array1,array2);
    		    }

    		    }
    	  myReader.readAsText(blob);
      	
        }
      
      function drawChart(array1,array2) {
        	console.log("array1", array1);
  		    console.log("array2", array2);
        	console.log("Inside drawchart 1");
          	var data = new google.visualization.DataTable();
        	
        	console.log("Data Table");
            data.addColumn('number', 'qrs');
            data.addColumn('number', 'II');
            
            for (var i = 0; i < array2.length; i++) {
                var row = [array1[i], array2[i]];
                data.addRow(row);
              }
            

            var options = {
              chart: {
                title: 'ECG',
                subtitle: 'in mV'
              },
              tooltip: {trigger: 'selection'},

              width: 900,
              height: 500,
              axes: {
                x: {
                  0: {side: 'top'}
                }
              },
              
              explorer: { 
                  actions: ['dragToZoom', 'rightClickToReset'],
                  axis: 'horizontal',
                  keepInBounds: true,
                  maxZoomIn: 40.0},
                colors: ['#D44E41'],
            };

            var element = document.getElementById('chart_div');
            var chart = new google.visualization.LineChart(element);
            element.style.display = 'block';
            google.visualization.events.addListener(chart, 'ready', function () {
          	  
              });
            
            chart.draw(data,options);
            

  	        }
      
      function drawChart2(array1,array2,array3,array4) {
      	console.log("array1", array1);
		console.log("array2", array2);
		console.log("array3", array3);
		console.log("array4", array4);
      	console.log("Inside drawchart 2");
        	var data = new google.visualization.DataTable();
      	
      	console.log("Data Table");
          data.addColumn('number', 'qrs');
          data.addColumn('number', 'I');
          data.addColumn('number', 'II');
          data.addColumn('number', 'III');
          
          for (var i = 0; i < array2.length; i++) {
              var row = [array1[i], array2[i]+3, array3[i], array4[i]-3];
              data.addRow(row);
            }
          

          var options = {
            chart: {
              title: 'ECG',
              subtitle: 'in mV'
            },
            tooltip: {trigger: 'selection'},

            width: 900,
            height: 500,
            axes: {
              x: {
                0: {side: 'top'}
              }
            },
            
            explorer: { 
                actions: ['dragToZoom', 'rightClickToReset'],
                axis: 'horizontal',
                keepInBounds: true,
                maxZoomIn: 40.0},
              colors: ['#D44E41', '#FF0000', '#3FA561'],
          };

          var element = document.getElementById('chart_div');
          var chart = new google.visualization.LineChart(element);
          element.style.display = 'block';
          google.visualization.events.addListener(chart, 'ready', function () {
        	  
            });
          
          chart.draw(data,options);
          

	        }



      var displayOther = function(obs, data) {   // http://stackoverflow.com/a/28541187/321797
        var blob = new Blob([data], {type: obs.mimeType});
        var downloadLink = angular.element('<a></a>');
        downloadLink.attr('href', $window.URL.createObjectURL(blob));
        downloadLink.attr('download', obs.fileName);
        downloadLink[0].click();
      }

      var getWindow = function(contentFamily) {
        switch ($scope.obs.contentFamily) {
          case module.family.PDF:
          return $window.open('');
          default:
          return {};
        }
      }
      
      var getChart = function(obs,data){
     	 // var arrayData = $.csv.toArrays([data], {onParseValue: $.csv.hooks.castToScalar});
     	  console.info("Inside get Chart");
     	  var view   = new Int32Array(data);
     	  console.info("to Array");
     	  console.info(view);
     	  var data1 = new google.visualization.arrayToDataTable(view);
     	 
     	  var crt_ertdlyYY = new google.visualization.ChartWrapper({
     	         chartType: 'LineChart',
     	         containerId: 'crt_ertdlyYY',
     	         dataTable: data1,
     	         options:{
     	            width: 450, height: 160,
     	            title: 'ECG',
     	            titleTextStyle : {color: 'grey', fontSize: 11},
     	         },
     	  		explorer: { 
     	  			actions: ['dragToZoom', 'rightClickToReset'],
     	  			axis: 'horizontal',
     	  			keepInBounds: true,
     	  			maxZoomIn: 4.0},
     	      });
     	  return crt_ertdlyYY;
     
       }
    }
  };
}]);