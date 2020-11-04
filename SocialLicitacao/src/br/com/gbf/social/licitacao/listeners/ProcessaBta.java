/* Decompiler 16ms, total 271ms, lines 89 */
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
import java.util.Iterator;

public class ProcessaBta implements EventoProgramavelJava {
   public void afterDelete(PersistenceEvent event) throws Exception {
   }

   public void afterInsert(PersistenceEvent event) throws Exception {
   }

   public void afterUpdate(PersistenceEvent event) throws Exception {
   }

   public void beforeCommit(TransactionContext arg0) throws Exception {
   }

   public void beforeDelete(PersistenceEvent event) throws Exception {
   }

   public void beforeInsert(PersistenceEvent event) throws Exception {
      SessionHandle hnd = null;

      try {
         hnd = JapeSession.open();
         EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
         DynamicVO registroVo = (DynamicVO)event.getVo();
         String nomeEntidade = registroVo.asString("NOMEENTIDADE");
         if (nomeEntidade.equalsIgnoreCase("AD_TGSCLILTEPRP")) {
            FinderWrapper finderWrapper = new FinderWrapper("AD_TGSCLILTEPPL", "CODLICITACAO = " + registroVo.asBigDecimal("CODLICITACAO") + " And CodLote = " + registroVo.asBigDecimal("SEQUENCIA") + " And Codparc = (Select Emp.Codparc From Ad_Tgscli cli Inner Join Tsiemp Emp on Cli.codemp = Emp.codemp Where Cli.codlicitacao = " + registroVo.asBigDecimal("CODLICITACAO") + ")");
            Collection<DynamicVO> dynamicVOs = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
            Iterator var9 = dynamicVOs.iterator();

            while(var9.hasNext()) {
               DynamicVO pregaoPrecoLote = (DynamicVO)var9.next();
               PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("AD_TGSCLILTEPPL", new Object[]{pregaoPrecoLote.asBigDecimal("CODLICITACAO"), pregaoPrecoLote.asBigDecimal("CODLOTE"), pregaoPrecoLote.asBigDecimal("SEQPPL")});
               EntityVO vo = ple.getValueObject();
               DynamicVO dynamicVO = (DynamicVO)vo;
               dynamicVO.setProperty("ATUALIZA", "S");
               if (registroVo.asString("PROPESCOLHIDA").equals("SIM")) {
                  dynamicVO.setProperty("PRECOPARTIDA", registroVo.asBigDecimal("PRECOVDAINF"));
               } else {
                  System.out.println("Alterando o Preço Partida para 0.");
                  dynamicVO.setProperty("PRECOPARTIDA", BigDecimal.valueOf(0L));
               }

               ple.setValueObject(vo);
               System.out.println("Objeto Salvo com Sucesso");
               if (registroVo.asString("PROPESCOLHIDA").equals("SIM")) {
                  System.out.println("IUnserindo Valores de Produto - Proposta Esscolhida: " + registroVo.asString("PROPESCOLHIDA") + " Valor Chave: " + JapeSession.getPropertyAsBoolean("ALTERA.VPR", Boolean.FALSE));
                  EntityVO entityVO2 = dwfFacade.getDefaultValueObjectInstance("AD_TGSCLILTEPPLVLP");
                  DynamicVO dynamicVO2 = (DynamicVO)entityVO2;
                  dynamicVO2.setProperty("CODLICITACAO", pregaoPrecoLote.asBigDecimal("CODLICITACAO"));
                  dynamicVO2.setProperty("CODLOTE", pregaoPrecoLote.asBigDecimal("CODLOTE"));
                  dynamicVO2.setProperty("SEQPPL", pregaoPrecoLote.asBigDecimal("SEQPPL"));
                  dynamicVO2.setProperty("PRECO", registroVo.asBigDecimal("PRECOVDAINF"));
                  dwfFacade.createEntity("AD_TGSCLILTEPPLVLP", entityVO2);
               }
            }
         } else if (nomeEntidade.equalsIgnoreCase("AD_TGSCLIPROPRP")) {
            return;
         }

         registroVo.setProperty("EXECUTADO", "S");
      } catch (Exception var18) {
         throw new Exception("Exceção: " + var18.getCause());
      } finally {
         JapeSession.close(hnd);
      }

   }

   public void beforeUpdate(PersistenceEvent arg0) throws Exception {
   }
}