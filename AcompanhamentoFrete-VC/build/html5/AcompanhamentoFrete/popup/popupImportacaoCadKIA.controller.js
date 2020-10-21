angular
    .module('AcompanhamentoFreteApp')
    .controller('PopupImportacaoCadKIAController', ['$scope', 'SkFileInputConstant', 'ServiceProxy', 'AvisosUtils', '$popupInstance', function($scope, SkFileInputConstant, ServiceProxy, AvisosUtils, $popupInstance) {
        var self = this;

        self.chaveArquivo = 'ARQUIVO_IMPORTACAO_NOTA';
        self.onUploadFineshed = onUploadFineshed;

        $scope.$success = executarImportacao;

        function onUploadFineshed(state, value) {
            if (angular.isUndefined(value)) {
                $scope.$enableBtnOk = false;
                return;
            }

            if (state == SkFileInputConstant.UPLOAD_IN_PROGRESS) {
                $scope.$enableBtnOk = false;
                return;
            }

            if (state == SkFileInputConstant.UPLOAD_FAILED) {
                $scope.$enableBtnOk = false;
                MessageUtils.showError(i18n('Core.Produtos.arquivoSelecionado'));
            } else {
                $scope.$enableBtnOk = true;
            }
        }

        function executarImportacao() {
            var request = {
                param: {
                    chave: self.chaveArquivo
                }
            };

            ServiceProxy
                .callService('acompfrete@LancamentoNotasFreteSP.lancarNota', request)
                .then(function(response){
                    AvisosUtils.open(response.responseBody.avisos.aviso, true);
                    $popupInstance.success();
                });
        }
    }]);