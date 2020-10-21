angular.module("AcompanhamentoFreteApp", ["snk"])
    .controller("AcompanhamentoFreteController",
        ["$scope", "ObjectUtils", "SkApplicationInstance", "ServiceProxy", "DateUtils", "StringUtils", "NumberUtils", "MessageUtils", "i18n", "DatasetObserverEvents", "SanPopup","SkFileInputConstant",
            function ($scope, ObjectUtils, SkApplicationInstance, ServiceProxy, DateUtils, StringUtils, NumberUtils, MessageUtils, i18n, DatasetObserverEvents, SanPopup,SkFileInputConstant) {

                var self = this;

                self.dynaformID = StringUtils.nextUid();
                self.onDynaformLoad = onDynaformLoad;
                self.otherOptionsLoader = otherOptionsLoader;
                self.customTabsLoader = customTabsLoader;

                var _dsAcompanhamentoFreteCHG;
                var _dsAcompFreteFinanceiro;
                var _dsAcompFreteNota;

                self.arquivo;
				self.uploadFineshed = uploadFineshed;
				self.beforeState = beforeState;
                self.importarArquivo = importarArquivo;
				self.chaveArquivo = 'ProcessamentoRetornoCartao_ARQUIVO_RETORNO_CARTAO';
                
                $scope.loadByPK = loadByPK;

                init();

                function init() {
                };

                function loadByPK(objPK) {
                };

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
			                .callService('acompfrete@LancamentoFinancFreteSP.importarArquivo', {params})
			                .then(function (response) {
			                    MessageUtils.showInfo('Arquivo Importado com sucesso !!!');
			                });
			        }   
				 
            	 
                function onDynaformLoad(dynaform, dataset) {

                    if (dataset.getEntityName() == 'AcompanhamentoFreteCHG') {
                        _dsAcompanhamentoFreteCHG = dataset;
                    } else if (dataset.getEntityName() == 'AcompFreteFinanceiro') {
                        _dsAcompFreteFinanceiro = dataset;
                        dynaform.getNavigatorAPI()
                            .showAddButton(false)
                            .showCopyButton(false)
                            .showRemoveButton(false)
                            .showSaveButton(false)
                            .showCancelButton(false);
                    } else if (dataset.getEntityName() == 'AcompFreteNota') {
                        _dsAcompFreteNota = dataset;
                        dynaform.getNavigatorAPI()
                            .showAddButton(false)
                            .showCopyButton(false)
                            .showRemoveButton(false)
                            .showSaveButton(false)
                            .showCancelButton(false);
                    }

                };

                function customTabsLoader(entityName) {

                    if (entityName == 'AcompanhamentoFreteCHG') {
                        var customTabs = [];

                        customTabs.push(
                            {
                                blockId: 'Form1',
                                description: i18n('Form1'),
                                controller: 'Form1ChgController',
                                controllerAs: 'ctrl',
                                templateUrl: 'html5/AcompanhamentoFrete/Forms/Form1/Form1.html'
                            },
                            {
                                blockId: 'Form2',
                                description: i18n('Form2'),
                                controller: 'Form2ChgController',
                                controllerAs: 'ctrl',
                                templateUrl: 'html5/AcompanhamentoFrete/Forms/Form2/Form2.html'
                            });


                        return customTabs;
                    }
                }

                function otherOptionsLoader(dynaform) {
                    return [
                        { label: "Lançar Financeiro", action: lancarFin },
                        { label: "Lançar Nota", action: lancarNota }
                    ];
                };

                function showPopUpImportacaoKIA() {
                    SanPopup.open({
                        title: "Importar Arquivo Nota",
                        templateUrl: 'html5/AcompanhamentoFrete/popup/popupImportacaoCadKIA.tpl.html',
                        controller: 'PopupImportacaoCadKIAController',
                        controllerAs: 'ctrl',
                        size: 'alert',
                        okBtnLabel: i18n('Importar'),
                        showBtnNo: false,
                        enableBtnOk: false
                    });
                }

                function lancarFin() {
                    console.log("lancarFin");
                    MessageUtils.showInfo("Sucesso", "Você apertou o Botão LançarFin");
                }

              function  lancarNota() {
                    ServiceProxy.callService("acompfrete@LancamentoNotasFreteSP.lancarNota", {})
                    .then(function (response) {

                    });
                }

            }]);