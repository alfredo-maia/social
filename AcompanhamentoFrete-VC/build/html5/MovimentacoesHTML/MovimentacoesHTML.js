angular.module("MovimentacoesHTMLApp", ["snk"])
	.controller("MovimentacoesHTMLController",
		["$scope", "ObjectUtils", "SkApplicationInstance", "ServiceProxy", "DateUtils", "StringUtils", "NumberUtils", "MessageUtils","i18n","DatasetObserverEvents",
			function ($scope, ObjectUtils, SkApplicationInstance, ServiceProxy, DateUtils, StringUtils, NumberUtils, MessageUtils,i18n,DatasetObserverEvents) {
				var self = this;

			      
				self.dynaformID = StringUtils.nextUid();
				self.onDynaformLoad = onDynaformLoad;
				self.otherOptionsLoader = otherOptionsLoader;
				self.customTabsLoader = customTabsLoader;
				self.onClick1 = onClick1;
				self.onClick2 = onClick2;
				self.uploadFineshed = uploadFineshed;
				self.beforeState = beforeState;
				self.importarArquivo = importarArquivo;
				self.chaveArquivo = 'ProcessamentoRetornoCartao_ARQUIVO_RETORNO_CARTAO';
				
				var _dsInstanciaPai;
				var _dsInstanciaFilha;
    
				$scope.loadByPK = loadByPK;

				function uploadFineshed(state, value) {
	                if (angular.isUndefined(value) || state == SkFileInputConstant.UPLOAD_FAILED) {
	                    self.arquivo = undefined;
	                    return;
	                }
	
	                self.arquivo = self.chaveArquivo;
		        }
				 
				function beforeState() {
	                self.arquivo = undefined;
	            }
				
				 function importarArquivo() {

			            var params = {
			                chaveArquivo: self.chaveArquivo
			            };

			            ServiceProxy
			                .callService('acompfrete@MovimentacoesSP.importarArquivo', {params})
			                .then(function (response) {
			                    MessageUtils.showInfo('Arquivo Importado com sucesso !!!');
			                    $popupInstance.success();
			                });
			        }   
				 
				function loadByPK(objPK) {
				};

				function onClick1() {
					console.log('Click1!');
					window.alert('Click1!');
					console.log('Click1!');
				}
				
				function onClick2() {
					console.log('Click2!');
					window.alert('Click2!');
					console.log('Click2!');
				}
				
				function onDynaformLoad(dynaform, dataset) {
					
					 if (dataset.getEntityName() == 'InstanciaPai') {
						 _dsInstanciaPai = dataset;
					 } else  if (dataset.getEntityName() == 'InstanciaFilha') {
						 _dsInstanciaFilha = dataset;
					 }
					 
				};

				function customTabsLoader(entityName) {
			         if (entityName == 'InstanciaPai') {
			            var customTabs = [];
			            customTabs.push({
			               blockId: 'Log',
			               description: i18n('Log'),
			               controller: 'LogController',
			               controllerAs: 'ctrl',
			               templateUrl: 'html5/MovimentacoesHTML/abas/Log/Log.tpl.html'
			            },
			            {
			               blockId: 'Teste',
			               description: i18n('Teste'),
			               controller: 'TestesController',
			               controllerAs: 'ctrl',
			               templateUrl: 'html5/MovimentacoesHTML/abas/Testes/Testes.tpl.html'
			            });
			            
			         }
			            return customTabs;
			         
			      }
				
				function otherOptionsLoader(dynaform) {
					return [
							{label: "Teste Financeiro", action: lancarFin},
							{label: "Teste Nota", action: lancarNota}
						];
				};

				

				function lancarFin() {

					var params = {"teste2" : "lancarFin"};

					ServiceProxy.callService("acompfrete@MovimentacoesSP.inserirFinanceiro2", params)
						.then(function (response) {
							 MessageUtils.showInfo("inserirFinanceiro2");
						});
				};
				
				function lancarNota() {

					var params = { "Teste1" : "lancarNota"};

					ServiceProxy.callService("acompfrete@MovimentacoesSP.inserirNota2", params)
						.then(function (response) {
							 MessageUtils.showInfo("inserirNota2");
						});

				};

			}]);