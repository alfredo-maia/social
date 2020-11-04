/* Decompiler 60ms, total 345ms, lines 189 */
package br.com.gbf.social.licitacao.listeners;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.metadata.EntityMetaData;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

public class AlteraPregaoPrecos implements EventoProgramavelJava {
	
   public void afterDelete(PersistenceEvent event) throws Exception {
      EntityMetaData entityMetaData = event.getEntity();
      String nameEntity = entityMetaData.getName();
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         PersistentLocalEntity ple = null;
         DynamicVO dynamicVO;
         if (nameEntity.equalsIgnoreCase("AD_TGSCLIPROPRP")) {
            System.out.println("afterDelete: " + nameEntity);
            DynamicVO licitacaoVo = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("AD_TGSCLI", registroVo.asBigDecimal("CODLICITACAO"));
            dynamicVO = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("Empresa", licitacaoVo.asBigDecimal("CODEMP"));
            FinderWrapper finderWrapperProPre = new FinderWrapper("AD_TGSCLIPROPRE", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And Sequencia = " + registroVo.asBigDecimal("SEQUENCIA") + " And Codparc = " + dynamicVO.asBigDecimal("CODPARC"));
            Collection<DynamicVO> dynamicVOsProPre = dwfFacade.findByDynamicFinderAsVO(finderWrapperProPre);
            Iterator var13 = dynamicVOsProPre.iterator();

            while(var13.hasNext()) {
               DynamicVO pregao = (DynamicVO)var13.next();
               ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLIPROPRE", new Object[]{pregao.asBigDecimal("CODLICITACAO"), pregao.asBigDecimal("SEQUENCIA"), pregao.asBigDecimal("SEQPRE")});
               EntityVO vo = ple.getValueObject();
               DynamicVO dynamicVO = (DynamicVO)vo;
               dynamicVO.setProperty("ATUALIZA", "S");
               ple.setValueObject(vo);
            }
         } else if (nameEntity.equalsIgnoreCase("AD_TGSCLIPROPREVPR")) {
            System.out.println("afterDelete: " + nameEntity);
            ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLIPROPRE", new Object[]{registroVo.asBigDecimal("CODLICITACAO"), registroVo.asBigDecimal("SEQUENCIA"), registroVo.asBigDecimal("SEQPRE")});
            EntityVO vo = ple.getValueObject();
            dynamicVO = (DynamicVO)vo;
            dynamicVO.setProperty("ATUALIZA", "S");
            ple.setValueObject(vo);
         }
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void afterInsert(PersistenceEvent event) throws Exception {
      EntityMetaData entityMetaData = event.getEntity();
      String nameEntity = entityMetaData.getName();
      System.out.println("Evento em afterInsert " + nameEntity);
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         PersistentLocalEntity ple = null;
         DynamicVO dynamicVO;
         if (nameEntity.equalsIgnoreCase("AD_TGSCLIPROPRP")) {
            System.out.println("afterInsert: " + nameEntity);
            DynamicVO licitacaoVo = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("AD_TGSCLI", registroVo.asBigDecimal("CODLICITACAO"));
            dynamicVO = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("Empresa", licitacaoVo.asBigDecimal("CODEMP"));
            System.out.println("afterInsert - Name Entity: " + nameEntity.toString() + " Valor Chave: " + JapeSession.getPropertyAsBoolean("ALTERA.VPR", Boolean.FALSE));
            FinderWrapper finderWrapperProPre = new FinderWrapper("AD_TGSCLIPROPRE", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And Sequencia = " + registroVo.asBigDecimal("SEQUENCIA") + " And Codparc = " + dynamicVO.asBigDecimal("CODPARC"));
            Collection<DynamicVO> dynamicVOsProPre = dwfFacade.findByDynamicFinderAsVO(finderWrapperProPre);
            Iterator var13 = dynamicVOsProPre.iterator();

            while(var13.hasNext()) {
               DynamicVO pregao = (DynamicVO)var13.next();
               ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLIPROPRE", new Object[]{pregao.asBigDecimal("CODLICITACAO"), pregao.asBigDecimal("SEQUENCIA"), pregao.asBigDecimal("SEQPRE")});
               EntityVO vo = ple.getValueObject();
               DynamicVO dynamicVO = (DynamicVO)vo;
               dynamicVO.setProperty("ATUALIZA", "S");
               ple.setValueObject(vo);
            }
         } else if (!nameEntity.equalsIgnoreCase("AD_TGSCLIPROPRP")) {
            System.out.println("afterInsert: " + nameEntity);
            ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLIPROPRE", new Object[]{registroVo.asBigDecimal("CODLICITACAO"), registroVo.asBigDecimal("SEQUENCIA"), registroVo.asBigDecimal("SEQPRE")});
            EntityVO vo = ple.getValueObject();
            dynamicVO = (DynamicVO)vo;
            dynamicVO.setProperty("ATUALIZA", "S");
            ple.setValueObject(vo);
            System.out.println("afterInsert - Name Entity: " + nameEntity.toString() + " Valor Chave: " + JapeSession.getPropertyAsBoolean("ALTERA.VPR", Boolean.FALSE));
         }
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void afterUpdate(PersistenceEvent event) throws Exception {
      EntityMetaData entityMetaData = event.getEntity();
      String nameEntity = entityMetaData.getName();
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         PersistentLocalEntity ple = null;
         if (nameEntity.equalsIgnoreCase("AD_TGSCLIPROPRP")) {
            if (registroVo.asString("ATUALIZA") != null && registroVo.asString("ATUALIZA").equals("S") || registroVo.asString("BTAEXEC") != null) {
               System.out.println("Dentro do If " + nameEntity + " Campo btaExec: " + registroVo.asString("BTAEXEC"));
               DynamicVO licitacaoVo = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("AD_TGSCLI", registroVo.asBigDecimal("CODLICITACAO"));
               DynamicVO empresaVo = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("Empresa", licitacaoVo.asBigDecimal("CODEMP"));
               FinderWrapper finderWrapperProPre = new FinderWrapper("AD_TGSCLIPROPRE", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And Sequencia = " + registroVo.asBigDecimal("SEQUENCIA") + " And Codparc = " + empresaVo.asBigDecimal("CODPARC"));
               Collection<DynamicVO> dynamicVOsProPre = dwfFacade.findByDynamicFinderAsVO(finderWrapperProPre);
               Iterator var13 = dynamicVOsProPre.iterator();

               while(var13.hasNext()) {
                  DynamicVO pregao = (DynamicVO)var13.next();
                  if (registroVo.asString("PROPOSTAESCOLHIDA").equals("NAO")) {
                     FinderWrapper finder = new FinderWrapper("AD_TGSCLIPROPREVPR", "this.CODLICITACAO = " + pregao.asBigDecimal("CODLICITACAO") + "And this.SEQUENCIA = " + pregao.asBigDecimal("SEQUENCIA") + "And this.SEQPRE = " + pregao.asBigDecimal("SEQPRE"));
                     finder.setOrderBy("this.CODCLIPROPREVPR DESC");
                     JapeSession.putProperty("ALTERA.VPR", Boolean.TRUE);
                     dwfFacade.removeByCriteria(finder);
                     JapeSession.putProperty("ALTERA.VPR", Boolean.FALSE);
                  }

                  ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLIPROPRE", new Object[]{pregao.asBigDecimal("CODLICITACAO"), pregao.asBigDecimal("SEQUENCIA"), pregao.asBigDecimal("SEQPRE")});
                  EntityVO vo = ple.getValueObject();
                  DynamicVO dynamicVO = (DynamicVO)vo;
                  dynamicVO.setProperty("ATUALIZA", "S");
                  if (registroVo.asString("PROPOSTAESCOLHIDA").equals("NAO")) {
                     dynamicVO.setProperty("PRECOPARTIDA", BigDecimal.valueOf(0L));
                     System.out.println("Proposta Esscolhida: " + registroVo.asString("PROPOSTAESCOLHIDA") + " Valor Chave: " + JapeSession.getPropertyAsBoolean("ALTERA.VPR", Boolean.FALSE));
                     JapeSession.putProperty("ALTERA.VPR", Boolean.TRUE);
                  } else if (registroVo.asString("PROPOSTAESCOLHIDA").equals("SIM")) {
                     dynamicVO.setProperty("PRECOPARTIDA", registroVo.asBigDecimal("PRECOVDAINF"));
                  }

                  ple.setValueObject(vo);
                  if (registroVo.asString("PROPOSTAESCOLHIDA").equals("SIM")) {
                     System.out.println("IUnserindo Valores de Produto - Proposta Esscolhida: " + registroVo.asString("PROPOSTAESCOLHIDA") + " Valor Chave: " + JapeSession.getPropertyAsBoolean("ALTERA.VPR", Boolean.FALSE));
                     JapeSession.putProperty("ALTERA.VPR", Boolean.TRUE);
                     EntityVO entityVO2 = dwfFacade.getDefaultValueObjectInstance("AD_TGSCLIPROPREVPR");
                     DynamicVO dynamicVO2 = (DynamicVO)entityVO2;
                     dynamicVO2.setProperty("CODLICITACAO", pregao.asBigDecimal("CODLICITACAO"));
                     dynamicVO2.setProperty("SEQUENCIA", pregao.asBigDecimal("SEQUENCIA"));
                     dynamicVO2.setProperty("SEQPRE", pregao.asBigDecimal("SEQPRE"));
                     dynamicVO2.setProperty("PRECO", registroVo.asBigDecimal("PRECOVDAINF"));
                     dwfFacade.createEntity("AD_TGSCLIPROPREVPR", entityVO2);
                  }
               }
            }
         } else if (!nameEntity.equalsIgnoreCase("AD_TGSCLIPROPRP")) {
            ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLIPROPRE", new Object[]{registroVo.asBigDecimal("CODLICITACAO"), registroVo.asBigDecimal("SEQUENCIA"), registroVo.asBigDecimal("SEQPRE")});
            System.out.println("Name Entity: " + nameEntity.toString() + " Valor Chave: " + JapeSession.getPropertyAsBoolean("ALTERA.VPR", Boolean.FALSE));
            boolean alteraVpr = JapeSession.getPropertyAsBoolean("ALTERA.VPR", Boolean.FALSE);
            if (alteraVpr) {
               EntityVO vo = ple.getValueObject();
               DynamicVO dynamicVO = (DynamicVO)vo;
               dynamicVO.setProperty("ATUALIZA", "S");
               ple.setValueObject(vo);
            }
         }
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void beforeCommit(TransactionContext event) throws Exception {
   }

   public void beforeDelete(PersistenceEvent event) throws Exception {
   }

   public void beforeInsert(PersistenceEvent arg0) throws Exception {
   }

   public void beforeUpdate(PersistenceEvent event) throws Exception {
   }
}