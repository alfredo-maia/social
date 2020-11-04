/* Decompiler 26ms, total 231ms, lines 88 */
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

public class InsereProdutoPregaoPrecos implements EventoProgramavelJava {
   public void afterDelete(PersistenceEvent arg0) throws Exception {
   }

   public void afterInsert(PersistenceEvent event) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         EntityVO entityVO = dwfFacade.getDefaultValueObjectInstance("AD_TGSCLIPROPRE");
         DynamicVO dynamicVO = (DynamicVO)entityVO;
         DynamicVO licitacaoVo = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("AD_TGSCLI", new Object[]{registroVo.asBigDecimal("CODLICITACAO")});
         DynamicVO empresaVo = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("Empresa", licitacaoVo.asBigDecimal("CODEMP"));
         if (empresaVo.asBigDecimal("CODPARC") == null) {
            throw new Exception("<br><br><b>Operação Não Permitida. <br><br>Motivo: Parceiro não foi Identificado no Cadastro de Empresas.<br><br>Solução: Informar o Código do Parceiro no Cadastro de Empresas.</b><br><br>");
         }

         dynamicVO.setProperty("CODLICITACAO", registroVo.asBigDecimal("CODLICITACAO"));
         dynamicVO.setProperty("SEQUENCIA", registroVo.asBigDecimal("SEQUENCIA"));
         dynamicVO.setProperty("CODPARC", empresaVo.asBigDecimal("CODPARC"));
         dynamicVO.setProperty("CODEMP", empresaVo.asBigDecimal("CODEMP"));
         dynamicVO.setProperty("CODMARCA", registroVo.asBigDecimal("MARCA"));
         dwfFacade.createEntity("AD_TGSCLIPROPRE", entityVO);
      } catch (Exception var12) {
         throw new Exception("Exception: " + var12.getCause());
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
         FinderWrapper finderWrapper = new FinderWrapper("AD_TGSCLIPROPRP", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And Sequencia = " + registroVo.asBigDecimal("SEQUENCIA"));
         Collection<DynamicVO> dynamicVOs = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
         Iterator var8 = dynamicVOs.iterator();

         while(var8.hasNext()) {
            DynamicVO propostaPreco = (DynamicVO)var8.next();
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLIPROPRP", new Object[]{propostaPreco.asBigDecimal("CODLICITACAO"), propostaPreco.asBigDecimal("SEQUENCIA"), propostaPreco.asBigDecimal("COCLIDPROPRP")});
            EntityVO vo = ple.getValueObject();
            DynamicVO dynamicVO = (DynamicVO)vo;
            dynamicVO.setProperty("ATUALIZA", "S");
            dynamicVO.setProperty("BTAEXEC", (Object)null);
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
}