/* Decompiler 7ms, total 250ms, lines 66 */
package br.com.gbf.social.licitacao.listeners;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class AlteraProduto implements EventoProgramavelJava {
   public void afterDelete(PersistenceEvent arg0) throws Exception {
   }

   public void afterInsert(PersistenceEvent event) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLIPRO", new Object[]{registroVo.asBigDecimal("CODLICITACAO"), registroVo.asBigDecimal("SEQUENCIA")});
         EntityVO vo = ple.getValueObject();
         DynamicVO dynamicVO = (DynamicVO)vo;
         dynamicVO.setProperty("ATUALIZA", "S");
         ple.setValueObject(vo);
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void afterUpdate(PersistenceEvent event) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLIPRO", new Object[]{registroVo.asBigDecimal("CODLICITACAO"), registroVo.asBigDecimal("SEQUENCIA")});
         EntityVO vo = ple.getValueObject();
         DynamicVO dynamicVO = (DynamicVO)vo;
         dynamicVO.setProperty("ATUALIZA", "S");
         ple.setValueObject(vo);
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void beforeCommit(TransactionContext arg0) throws Exception {
   }

   public void beforeDelete(PersistenceEvent arg0) throws Exception {
   }

   public void beforeInsert(PersistenceEvent arg0) throws Exception {
   }

   public void beforeUpdate(PersistenceEvent arg0) throws Exception {
   }
}