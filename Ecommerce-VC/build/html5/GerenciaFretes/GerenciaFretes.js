angular
	.module('GerenciaFretesApp', [ 'snk' ])
	.controller(
			'GerenciaFretesController',
			['SkApplication', 'i18n', 'ObjectUtils', 'MGEParameters',
				'GridConfig', 'AngularUtil', 'StringUtils', 'ServiceProxy', 'MessageUtils',
				'SanPopup', '$scope',
function(SkApplication, i18n, ObjectUtils,
		MGEParameters, GridConfig, AngularUtil,
		StringUtils, ServiceProxy, MessageUtils,
		SanPopup, $scope) {

	var self = this;
	
	var _dynaformGerenciaFretes;
	var _dsGerenciaFretes;
	
	var _dynaformGerenciaFretesDet;
	var _dsGerenciaFretesDet;
	
	
	self.onDynaformLoaded = onDynaformLoaded;
	self.customTabsLoader = customTabsLoader;
	self.interceptNavigator = interceptNavigator;
	self.interceptFieldMetadata = interceptFieldMetadata;
	//Declara para o método 
	self.buttonAction = buttonAction;
	//Declaração para a função inserir frete
	self.inserirFrete = inserirFrete;
	self.dividirValor = dividirValor;
	self.duplicarFrete = duplicarFrete;
	
	ObjectUtils.implements(self, IDynaformInterceptor);
	 
	function onDynaformLoaded(dynaform, dataset) {
		
		if (dataset.getEntityName() == 'GerenciaFretes'){
			
			_dynaformGerenciaFretes = dynaform;
			_dsGerenciaFretes = dataset;
			
		}
		
		if (dataset.getEntityName() == 'GerenciaFretesDet'){
			
			_dynaformGerenciaFretesDet = dynaform;
			_dsGerenciaFretesDet = dataset;
			
			_dynaformGerenciaFretesDet.getNavigatorAPI()
					.showAddButton(false)
				    .showCopyButton(false)
					.showSaveButton(false);
		} 
		
	}

	function customTabsLoader(entityName) {
		var customTabs = [];
		return customTabs;
	}

	function interceptNavigator(navigator, dynaform) {
	}

	function interceptFieldMetadata(fieldMetadata,
			dataset, dynaform) {
	}
	
	//Método de inserção de fretes
	function buttonAction(){
		alert("Cliquei no botão");
	}
	
	//Função de inserção de fretes
	function inserirFrete(){
		
		var param = {"IDPAI":_dsGerenciaFretes.getFieldValue('ID')};
		
		ServiceProxy.callService('gerenciafretes@GerenciaFretesSP.inserirFrete', param)
			.then(function(response){
				var mensagem = ObjectUtils.getProperty (response, 'responseBody.response');
				MessageUtils.showInfo('Aviso', mensagem);
				_dsGerenciaFretesDet.refresh();
			})
			
	}

	//Função de dividir valor do frete
	function dividirValor(){
		
		var param = {"IDPAI":_dsGerenciaFretes.getFieldValue('ID'),
					 "ID": _dsGerenciaFretesDet.getFieldValue('ID'),
					 "VALOR":_dsGerenciaFretesDet.getFieldValue('VALOR')};
		
		ServiceProxy.callService('gerenciafretes@GerenciaFretesSP.dividirValor', param)
			.then(function(response){
				var mensagem = ObjectUtils.getProperty (response, 'responseBody.response');
				MessageUtils.showInfo('Aviso', mensagem);
				_dsGerenciaFretesDet.refreshCurrentRow();
			})
			
	}

	//Função de duplicar valor do frete
	function duplicarFrete(){
		
		var param = {"IDPAI":_dsGerenciaFretes.getFieldValue('ID'),
					 "ID":_dsGerenciaFretesDet.getFieldValue('ID')};
		
		ServiceProxy.callService('gerenciafretes@GerenciaFretesSP.duplicarFrete', param)
			.then(function(response){
				var mensagem = ObjectUtils.getProperty (response, 'responseBody.response');
				MessageUtils.showInfo('Aviso', mensagem);
				_dsGerenciaFretesDet.refresh();
			})
			
	}
	
} ]);