/* Decompiler 30ms, total 226ms, lines 165 */
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

public class AlteraLtePregaoPrecos2 implements EventoProgramavelJava {
   public void afterDelete(PersistenceEvent event) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         this.alteraLotePrp(event, dwfFacade);
         FinderWrapper finderWrapper = new FinderWrapper("AD_TGSCLILTEPPL", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And CodLote = " + registroVo.asBigDecimal("CODLOTE"));
         Collection<DynamicVO> dynamicVOs = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
         Iterator var8 = dynamicVOs.iterator();

         while(var8.hasNext()) {
            DynamicVO pregaoPrecoLote = (DynamicVO)var8.next();
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLILTEPPL", new Object[]{pregaoPrecoLote.asBigDecimal("CODLICITACAO"), pregaoPrecoLote.asBigDecimal("CODLOTE"), pregaoPrecoLote.asBigDecimal("SEQPPL")});
            EntityVO vo = ple.getValueObject();
            DynamicVO dynamicVO = (DynamicVO)vo;
            dynamicVO.setProperty("ATUALIZA", "S");
            ple.setValueObject(vo);
         }

         FinderWrapper finderWrapperVlp = new FinderWrapper("AD_TGSCLILTEPPLVLP", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And CodLote = " + registroVo.asBigDecimal("CODLOTE"));
         Collection<DynamicVO> dynamicVOsVlp = dwfFacade.findByDynamicFinderAsVO(finderWrapperVlp);
         Iterator var17 = dynamicVOsVlp.iterator();

         while(var17.hasNext()) {
            DynamicVO valoresProduto = (DynamicVO)var17.next();
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLILTEPPLVLP", new Object[]{valoresProduto.asBigDecimal("CODLICITACAO"), valoresProduto.asBigDecimal("CODLOTE"), valoresProduto.asBigDecimal("SEQPPL"), valoresProduto.asBigDecimal("CODCLILTEPPL")});
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
         this.alteraLotePrp(event, dwfFacade);
         FinderWrapper finderWrapper = new FinderWrapper("AD_TGSCLILTEPPL", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And CodLote = " + registroVo.asBigDecimal("CODLOTE"));
         Collection<DynamicVO> dynamicVOs = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
         Iterator var8 = dynamicVOs.iterator();

         while(var8.hasNext()) {
            DynamicVO pregaoPrecoLote = (DynamicVO)var8.next();
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLILTEPPL", new Object[]{pregaoPrecoLote.asBigDecimal("CODLICITACAO"), pregaoPrecoLote.asBigDecimal("CODLOTE"), pregaoPrecoLote.asBigDecimal("SEQPPL")});
            EntityVO vo = ple.getValueObject();
            DynamicVO dynamicVO = (DynamicVO)vo;
            dynamicVO.setProperty("ATUALIZA", "S");
            ple.setValueObject(vo);
         }

         FinderWrapper finderWrapperVlp = new FinderWrapper("AD_TGSCLILTEPPLVLP", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And CodLote = " + registroVo.asBigDecimal("CODLOTE"));
         Collection<DynamicVO> dynamicVOsVlp = dwfFacade.findByDynamicFinderAsVO(finderWrapperVlp);
         Iterator var17 = dynamicVOsVlp.iterator();

         while(var17.hasNext()) {
            DynamicVO valoresProduto = (DynamicVO)var17.next();
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLILTEPPLVLP", new Object[]{valoresProduto.asBigDecimal("CODLICITACAO"), valoresProduto.asBigDecimal("CODLOTE"), valoresProduto.asBigDecimal("SEQPPL"), valoresProduto.asBigDecimal("CODCLILTEPPL")});
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
         this.alteraLotePrp(event, dwfFacade);
         FinderWrapper finderWrapper = new FinderWrapper("AD_TGSCLILTEPPL", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And CodLote = " + registroVo.asBigDecimal("CODLOTE"));
         Collection<DynamicVO> dynamicVOs = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
         Iterator var8 = dynamicVOs.iterator();

         while(var8.hasNext()) {
            DynamicVO pregaoPrecoLote = (DynamicVO)var8.next();
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLILTEPPL", new Object[]{pregaoPrecoLote.asBigDecimal("CODLICITACAO"), pregaoPrecoLote.asBigDecimal("CODLOTE"), pregaoPrecoLote.asBigDecimal("SEQPPL")});
            EntityVO vo = ple.getValueObject();
            DynamicVO dynamicVO = (DynamicVO)vo;
            dynamicVO.setProperty("ATUALIZA", "S");
            ple.setValueObject(vo);
         }

         FinderWrapper finderWrapperVlp = new FinderWrapper("AD_TGSCLILTEPPLVLP", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And CodLote = " + registroVo.asBigDecimal("CODLOTE"));
         Collection<DynamicVO> dynamicVOsVlp = dwfFacade.findByDynamicFinderAsVO(finderWrapperVlp);
         Iterator var17 = dynamicVOsVlp.iterator();

         while(var17.hasNext()) {
            DynamicVO valoresProduto = (DynamicVO)var17.next();
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLILTEPPLVLP", new Object[]{valoresProduto.asBigDecimal("CODLICITACAO"), valoresProduto.asBigDecimal("CODLOTE"), valoresProduto.asBigDecimal("SEQPPL"), valoresProduto.asBigDecimal("CODCLILTEPPL")});
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

   public void beforeDelete(PersistenceEvent arg0) throws Exception {
   }

   public void beforeInsert(PersistenceEvent arg0) throws Exception {
   }

   public void beforeUpdate(PersistenceEvent arg0) throws Exception {
   }

   public void alteraLotePrp(PersistenceEvent event, EntityFacade dwfFacade) throws Exception {
      DynamicVO registroVo = (DynamicVO)event.getVo();
      System.out.println("Consultando o Finder em AlteraLtePregao2");
      FinderWrapper finderWrapper = new FinderWrapper("AD_TGSCLILTEPRP", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And CodLote = " + registroVo.asBigDecimal("CODLOTE"));
      Collection<DynamicVO> dynamicVOs = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
      Iterator var7 = dynamicVOs.iterator();

      while(var7.hasNext()) {
         DynamicVO propostaPrecoLote = (DynamicVO)var7.next();
         PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLILTEPRP", new Object[]{propostaPrecoLote.asBigDecimal("CODLICITACAO"), propostaPrecoLote.asBigDecimal("CODLOTE"), propostaPrecoLote.asBigDecimal("SEQPRP")});
         EntityVO vo = ple.getValueObject();
         DynamicVO dynamicVO = (DynamicVO)vo;
         dynamicVO.setProperty("ATUALIZA", "S");
         ple.setValueObject(vo);
      }

   }
}