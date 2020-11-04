/* Decompiler 78ms, total 271ms, lines 59 */
package br.com.gbf.social.licitacao.listeners;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class EVPLotePregaoPrecos implements EventoProgramavelJava {
   public void afterDelete(PersistenceEvent arg0) throws Exception {
   }

   public void afterInsert(PersistenceEvent arg0) throws Exception {
   }

   public void afterUpdate(PersistenceEvent arg0) throws Exception {
   }

   public void beforeCommit(TransactionContext arg0) throws Exception {
   }

   public void beforeDelete(PersistenceEvent arg0) throws Exception {
   }

   public void beforeInsert(PersistenceEvent event) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         if (registroVo.asString("PROPOSTAESCOLHIDA").equalsIgnoreCase("SIM")) {
            DynamicVO dynamicVO = (DynamicVO)dwfFacade.getDefaultValueObjectInstance("AD_TGSCLITEMPBTA");
            dynamicVO.setProperty("CODLICITACAO", registroVo.asBigDecimal("CODLICITACAO"));
            dynamicVO.setProperty("SEQUENCIA", registroVo.asBigDecimal("CODLOTE"));
            dynamicVO.setProperty("CODPROPOSTA", registroVo.asBigDecimal("SEQPRP"));
            dynamicVO.setProperty("NROPROPOSTA", registroVo.asBigDecimal("NROPROPOSTA"));
            dynamicVO.setProperty("NOMEENTIDADE", "AD_TGSCLILTEPRP");
            dynamicVO.setProperty("PRECOVDAINF", registroVo.asBigDecimal("PRECOVDAINF"));
            dynamicVO.setProperty("PROPESCOLHIDA", registroVo.asString("PROPOSTAESCOLHIDA"));
            dynamicVO.setProperty("EXECUTADO", "N");
            dwfFacade.createEntity("AD_TGSCLITEMPBTA", (EntityVO)dynamicVO);
         }
      } catch (Exception var9) {
         throw new Exception(var9.getMessage());
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void beforeUpdate(PersistenceEvent arg0) throws Exception {
   }
}