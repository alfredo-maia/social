/* Decompiler 17ms, total 291ms, lines 105 */
package br.com.gbf.social.licitacao.listeners;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.util.Collection;
import java.util.Iterator;

public class AlteraLoteProdutos implements EventoProgramavelJava {
   public void afterDelete(PersistenceEvent event) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         FinderWrapper finderWrapper = new FinderWrapper("AD_TGSCLILTE", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And CodLote = " + registroVo.asBigDecimal("CODLOTE"));
         Collection<DynamicVO> dynamicVOs = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
         Iterator var8 = dynamicVOs.iterator();

         while(var8.hasNext()) {
            DynamicVO loteProduto = (DynamicVO)var8.next();
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLILTE", new Object[]{loteProduto.asBigDecimal("CODLICITACAO"), loteProduto.asBigDecimal("CODLOTE")});
            EntityVO vo = ple.getValueObject();
            DynamicVO dynamicVO = (DynamicVO)vo;
            dynamicVO.setProperty("ATUALIZA", "S");
            ple.setValueObject(vo);
         }
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void afterInsert(PersistenceEvent event) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         FinderWrapper finderWrapper = new FinderWrapper("AD_TGSCLILTE", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And CodLote = " + registroVo.asBigDecimal("CODLOTE"));
         Collection<DynamicVO> dynamicVOs = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
         Iterator var8 = dynamicVOs.iterator();

         while(var8.hasNext()) {
            DynamicVO loteProduto = (DynamicVO)var8.next();
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLILTE", new Object[]{loteProduto.asBigDecimal("CODLICITACAO"), loteProduto.asBigDecimal("CODLOTE")});
            EntityVO vo = ple.getValueObject();
            DynamicVO dynamicVO = (DynamicVO)vo;
            dynamicVO.setProperty("ATUALIZA", "S");
            ple.setValueObject(vo);
         }
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
         FinderWrapper finderWrapper = new FinderWrapper("AD_TGSCLILTE", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And CodLote = " + registroVo.asBigDecimal("CODLOTE"));
         Collection<DynamicVO> dynamicVOs = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
         Iterator var8 = dynamicVOs.iterator();

         while(var8.hasNext()) {
            DynamicVO loteProduto = (DynamicVO)var8.next();
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLILTE", new Object[]{loteProduto.asBigDecimal("CODLICITACAO"), loteProduto.asBigDecimal("CODLOTE")});
            EntityVO vo = ple.getValueObject();
            DynamicVO dynamicVO = (DynamicVO)vo;
            dynamicVO.setProperty("ATUALIZA", "S");
            ple.setValueObject(vo);
         }
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void beforeCommit(TransactionContext arg0) throws Exception {
   }

   public void beforeDelete(PersistenceEvent event) throws Exception {
   }

   public void beforeInsert(PersistenceEvent arg0) throws Exception {
   }

   public void beforeUpdate(PersistenceEvent arg0) throws Exception {
   }
}