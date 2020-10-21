angular
	.module('GestaoDevSocialApp', [ 'snk' ])
	.controller(
			'GestaoDevSocialController',
			['SkApplication', 'i18n', 'ObjectUtils', 'MGEParameters',
				'GridConfig', 'AngularUtil', 'StringUtils', 'ServiceProxy', 'MessageUtils',
				'SanPopup', '$scope',
function(SkApplication, i18n, ObjectUtils,
		MGEParameters, GridConfig, AngularUtil,
		StringUtils, ServiceProxy, MessageUtils,
		SanPopup, $scope) {

	var self = this;
	
	self.onDynaformLoaded = onDynaformLoaded;
	self.customTabsLoader = customTabsLoader;
	self.interceptNavigator = interceptNavigator;
	self.interceptFieldMetadata = interceptFieldMetadata;
	//Declara para o método 
	self.buttonAction = buttonAction;
	
	ObjectUtils.implements(self, IDynaformInterceptor);
	 
	function customTabsLoader(entityName) {
		var customTabs = [];
		return customTabs;
	}

	function interceptNavigator(navigator, dynaform) {
	}

	function interceptFieldMetadata(fieldMetadata,
			dataset, dynaform) {
	}
	
	function onDynaformLoaded(dynaform, dataset){

	}

	//Método de inserção de fretes
	function buttonAction(){
		alert("Cliquei no botão");
	}

} ]);