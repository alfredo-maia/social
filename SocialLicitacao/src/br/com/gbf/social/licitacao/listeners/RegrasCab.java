/* Decompiler 24ms, total 187ms, lines 96 */
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
import java.math.BigDecimal;
import java.util.Collection;

public class RegrasCab implements EventoProgramavelJava {
   private static final String GBF_CHAVE_CONFIRMAR_LICITACAO = "gbf.confirmar.nota.licitacao";

   public void afterDelete(PersistenceEvent event) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO cabVo = (DynamicVO)event.getVo();
         if (cabVo.asBigDecimal("AD_CODLICITACAO") != null) {
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLIOCP", new Object[]{cabVo.asBigDecimal("AD_CODLICITACAO"), cabVo.asString("AD_CODLICITACAOOC")});
            DynamicVO opcVo = (DynamicVO)ple.getValueObject();
            opcVo.setProperty("NUNOTAVENDA", (Object)null);
            ple.setValueObject((EntityVO)opcVo);
         }
      } catch (Exception var10) {
         var10.printStackTrace();
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void afterInsert(PersistenceEvent event) throws Exception {
   }

   public void afterUpdate(PersistenceEvent event) throws Exception {
      SessionHandle hnd = null;
      String aplicacao = "CONFIRMACAO";
      BigDecimal nurelParm = BigDecimal.valueOf(140L);

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         boolean confirmando = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", Boolean.FALSE);
         System.out.println("Fora: " + confirmando);
         DynamicVO cabVo = (DynamicVO)event.getVo();
         boolean passouAqui = JapeSession.getPropertyAsBoolean("gbf.confirmar.nota.licitacao", Boolean.FALSE);
         if (!this.validaTops(cabVo, nurelParm, aplicacao)) {
            return;
         }

         if (confirmando && cabVo.asBigDecimal("AD_CODLICITACAO") != null && !passouAqui) {
            System.out.println("Dentro: " + confirmando);
            JapeSession.putProperty("gbf.confirmar.nota.licitacao", Boolean.TRUE);
            PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLIOCP", new Object[]{cabVo.asBigDecimal("AD_CODLICITACAO"), cabVo.asString("AD_CODLICITACAOOC")});
            DynamicVO opcVo = (DynamicVO)ple.getValueObject();
            opcVo.setProperty("NUNOTAVENDA", cabVo.asBigDecimal("NUNOTA"));
            ple.setValueObject((EntityVO)opcVo);
         }
      } catch (Exception var14) {
         var14.printStackTrace();
      } finally {
         JapeSession.close(hnd);
         JapeSession.putProperty("gbf.confirmar.nota.licitacao", Boolean.FALSE);
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

   public boolean validaTops(DynamicVO cabVO, BigDecimal nuRelParm, String aplicacao) throws Exception {
      EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
      FinderWrapper finder = new FinderWrapper("RELPARMTOP", "this.CODTIPOPER = ? AND this.NURELPARM = ? And this.APLICACAO = ?", new Object[]{cabVO.asBigDecimal("CODTIPOPER"), nuRelParm, aplicacao});
      Collection<EntityFacade> tops = dwfFacade.findByDynamicFinder(finder);
      return tops.size() > 0;
   }
}