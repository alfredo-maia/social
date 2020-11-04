/* Decompiler 9ms, total 190ms, lines 59 */
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

public class InsereLotePregaoPrecos implements EventoProgramavelJava {
   public void afterDelete(PersistenceEvent arg0) throws Exception {
   }

   public void afterInsert(PersistenceEvent event) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         EntityVO entityVO = dwfFacade.getDefaultValueObjectInstance("AD_TGSCLILTEPPL");
         DynamicVO dynamicVO = (DynamicVO)entityVO;
         DynamicVO licitacaoVo = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("AD_TGSCLI", new Object[]{registroVo.asBigDecimal("CODLICITACAO")});
         DynamicVO empresaVo = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("Empresa", licitacaoVo.asBigDecimal("CODEMP"));
         if (empresaVo.asBigDecimal("CODPARC") == null) {
            throw new Exception("<br><br><b>Operação Não Permitida. <br><br>Motivo: Parceiro não foi Identificado no Cadastro de Empresas.<br><br>Solução: Informar o Código do Parceiro no Cadastro de Empresas.</b><br><br>");
         }

         dynamicVO.setProperty("CODLICITACAO", registroVo.asBigDecimal("CODLICITACAO"));
         dynamicVO.setProperty("CODLOTE", registroVo.asBigDecimal("CODLOTE"));
         dynamicVO.setProperty("CODPARC", empresaVo.asBigDecimal("CODPARC"));
         dwfFacade.createEntity("AD_TGSCLILTEPPL", entityVO);
      } catch (Exception var12) {
         throw new Exception(var12.getMessage());
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void afterUpdate(PersistenceEvent event) throws Exception {
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