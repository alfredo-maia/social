/* Decompiler 9ms, total 927ms, lines 41 */
package br.com.gbf.social.licitacao.actions;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class IncluirValorProduto implements AcaoRotinaJava {
   public void doAction(ContextoAcao contexto) throws Exception {
      SessionHandle hnd = null;
      EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
      Registro[] linhasSelecionadas = contexto.getLinhas();

      try {
         hnd = JapeSession.open();
         Registro[] var8 = linhasSelecionadas;
         int var7 = linhasSelecionadas.length;

         for(int var6 = 0; var6 < var7; ++var6) {
            Registro linha = var8[var6];
            EntityVO entityVO = dwfFacade.getDefaultValueObjectInstance("AD_TGSCLIPROPREVPR");
            DynamicVO dynamicVO = (DynamicVO)entityVO;
            dynamicVO.setProperty("PRECO", BigDecimal.valueOf((Double)contexto.getParam("P_PRECO")));
            dynamicVO.setProperty("CODLICITACAO", linha.getCampo("CODLICITACAO"));
            dynamicVO.setProperty("SEQUENCIA", linha.getCampo("SEQUENCIA"));
            dynamicVO.setProperty("SEQPRE", linha.getCampo("SEQPRE"));
            dwfFacade.createEntity("AD_TGSCLIPROPREVPR", entityVO);
         }
      } finally {
         JapeSession.close(hnd);
      }

   }
}