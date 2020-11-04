/* Decompiler 41ms, total 223ms, lines 107 */
package br.com.gbf.social.licitacao.actions;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;
import java.math.BigDecimal;
import java.util.Collection;

public class IncluirItemOcp implements AcaoRotinaJava {
   public void doAction(ContextoAcao contexto) throws Exception {
      SessionHandle hnd = null;
      EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
      Registro[] linhasSelecionadas = contexto.getLinhas();
      String chave = "USU.LIB.QTDMAIOR";
      String liberado = null;
      BigDecimal codLicitacao = null;
      String nroOcp = null;
      double paramQtdNeg = (Double)contexto.getParam("P_QTDNEG");

      try {
         hnd = JapeSession.open();
         Registro linha;
         if (linhasSelecionadas.length < 1) {
            linha = contexto.getLinhaPai();
            codLicitacao = (BigDecimal)linha.getCampo("CODLICITACAO");
            nroOcp = (String)linha.getCampo("NROOCP");
         } else if (linhasSelecionadas.length == 1) {
            Registro[] var14 = linhasSelecionadas;
            int var13 = linhasSelecionadas.length;

            for(int var12 = 0; var12 < var13; ++var12) {
               linha = var14[var12];
               codLicitacao = (BigDecimal)linha.getCampo("CODLICITACAO");
               nroOcp = (String)linha.getCampo("NROOCP");
            }
         } else {
            contexto.mostraErro("Não é Possivel Selecionar Mais de 1 Registro");
         }

         EntityVO entityVO = dwfFacade.getDefaultValueObjectInstance("AD_TGSCLIOCPPRO");
         DynamicVO dynamicVO = (DynamicVO)entityVO;
         dynamicVO.setProperty("CODLICITACAO", codLicitacao);
         dynamicVO.setProperty("NROOCP", nroOcp);
         dynamicVO.setProperty("CODPROD", new BigDecimal((String)contexto.getParam("P_CODPROD")));
         dynamicVO.setProperty("QTDNEG", new BigDecimal(paramQtdNeg));
         BigDecimal relParm = BigDecimal.valueOf(140L);
         DynamicVO usuarioVo = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("Usuario", contexto.getUsuarioLogado());
         if (!this.validaUsuario(relParm, chave, usuarioVo.asBigDecimal("CODUSU"), usuarioVo.asBigDecimal("CODGRUPO"))) {
            liberado = "A";
         } else {
            liberado = "L";
         }

         if (liberado.equalsIgnoreCase("A")) {
            DynamicVO qtdeVO = null;

            try {
               qtdeVO = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("AD_VGSCLIOCPQTD", new Object[]{codLicitacao, new BigDecimal((String)contexto.getParam("P_CODPROD"))});
            } catch (Exception var24) {
               throw new Exception("O Produto " + contexto.getParam("P_CODPROD") + " não existe na Aba Produtos Vencedores, para a Licitação de Nro " + codLicitacao);
            }

            if (qtdeVO != null) {
               String sbTitulo = "";
               String sbMensagem = "";
               BigDecimal vlrTotal = qtdeVO.asBigDecimal("QTDNEG").add(new BigDecimal(paramQtdNeg));
               if (qtdeVO.asBigDecimal("QTDLICITACAO").compareTo(vlrTotal) == -1) {
                  DynamicVO produtoVO = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("Produto", contexto.getParam("P_CODPROD"));
                  dynamicVO.setProperty("LIBERACAO", liberado);
                  sbTitulo = "Quantidade Superior ao Disponível na Licitação";
                  sbMensagem = "O Produto " + (String)contexto.getParam("P_CODPROD") + " - " + produtoVO.asString("MARCA") + " - " + produtoVO.asString("REFERENCIA") + " - " + produtoVO.asString("COMPLDESC") + " já contempla a Quantidade de {" + StringUtils.formatNumeric(qtdeVO.asBigDecimal("QTDNEG")) + "}" + " para a Licitação de Nro " + codLicitacao + ".<br><br>";
                  sbMensagem = sbMensagem + "A Quantidade Solicitada <b>" + StringUtils.formatNumeric(contexto.getParam("P_QTDNEG")) + "</b> mais a quantidade já incluída do produto <b>" + StringUtils.formatNumeric(qtdeVO.asBigDecimal("QTDNEG")) + "</b> será superior a Quantidade da Licitação <b>{" + StringUtils.formatNumeric(qtdeVO.asBigDecimal("QTDLICITACAO")) + "}</b>." + "<br> Deseja realizar a Inclusão mediante a <b>Liberação</b> ?";
                  boolean executa = contexto.confirmarSimNao(sbTitulo, sbMensagem, 1);
                  if (!executa) {
                     contexto.setMensagemRetorno("Operação Cancelada pelo Usuário");
                     return;
                  }
               }
            }
         } else {
            dynamicVO.setProperty("LIBERACAO", liberado);
         }

         dwfFacade.createEntity("AD_TGSCLIOCPPRO", entityVO);
         contexto.setMensagemRetorno("Produto Incluído com Sucesso");
      } finally {
         JapeSession.close(hnd);
      }

   }

   private boolean validaUsuario(BigDecimal relParm, String chave, BigDecimal codusu, BigDecimal codGrupo) throws Exception {
      EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
      FinderWrapper finder = new FinderWrapper("RELPARMUSU", "(this.CODUSU = ? OR this.CODGRUPO = ? ) AND this.NURELPARM = ? And this.DESCRICAO = ?", new Object[]{codusu, codGrupo, relParm, chave});
      Collection<EntityFacade> usuario = dwfFacade.findByDynamicFinder(finder);
      return usuario.size() > 0;
   }
}