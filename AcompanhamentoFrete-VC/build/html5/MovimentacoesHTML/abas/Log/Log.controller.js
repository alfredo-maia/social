angular
.module("MovimentacoesHTMLApp")
.controller("LogController", ["ObjectUtils", "$scope", "i18n", "MessageUtils", "ServiceProxy",
   function(ObjectUtils, $scope, i18n, MessageUtils, ServiceProxy) {
      var self = this;
      self.dsOfDaynaform;
      self.dsLog;
      self.onDatasetCreated = onDatasetCreated;

      function onDatasetCreated(dataset) {
         if (dataset.getEntityName() == "LogIntegracao") {
            self.dsLog = dataset;
            self.dsLog.initAndRefresh();
         }
      }
   }
]);


