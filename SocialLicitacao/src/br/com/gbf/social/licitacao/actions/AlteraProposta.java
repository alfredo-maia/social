/* Decompiler 59ms, total 229ms, lines 123 */
package br.com.gbf.social.licitacao.actions;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class AlteraProposta implements AcaoRotinaJava {
   public void doAction(ContextoAcao contexto) throws Exception {
      SessionHandle hnd = null;
      EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
      Registro[] linhasSelecionadas = contexto.getLinhas();
      String valor = (String)contexto.getParam("P_VALOR");
      BigDecimal nroProposta = null;
      String valorRepresenta;
      if (valor.equals("SIM")) {
         valorRepresenta = "Sim";
      } else {
         valorRepresenta = "Não";
      }

      if (linhasSelecionadas.length != 0 && linhasSelecionadas != null) {
         if (linhasSelecionadas.length > 1) {
            throw new Exception("Selecione apenas uma Linha para Executar a Ação");
         } else {
            try {
               hnd = JapeSession.open();
               boolean executa = contexto.confirmarSimNao("Alterar Proposta Escolhida ?", "A Proposta Selecionada será Alterada para " + valorRepresenta + ".<br>Deseja Continuar ?", 1);
               if (executa) {
                  Registro[] var12 = linhasSelecionadas;
                  int var11 = linhasSelecionadas.length;

                  for(int var10 = 0; var10 < var11; ++var10) {
                     Registro linha = var12[var10];
                     String nomeInstancia = (String)linha.getCampo("NOMEINSTANCIA");
                     int contador = 0;
                     nroProposta = (BigDecimal)linha.getCampo("NROPROPOSTA");
                     FinderWrapper finderWrapperProPrp = new FinderWrapper(nomeInstancia, "CODLICITACAO = " + linha.getCampo("CODLICITACAO") + " And NROPROPOSTA = " + linha.getCampo("NROPROPOSTA"));
                     Collection<DynamicVO> dynamicVOsProPrp = dwfFacade.findByDynamicFinderAsVO(finderWrapperProPrp);
                     Iterator var18 = dynamicVOsProPrp.iterator();

                     while(var18.hasNext()) {
                        DynamicVO propostas = (DynamicVO)var18.next();
                        Object[] pks = null;
                        DynamicVO pregaoPrecoLote;
                        if (nomeInstancia.equalsIgnoreCase("AD_TGSCLILTEPRP")) {
                           pks = new Object[]{propostas.asBigDecimal("CODLICITACAO"), propostas.asBigDecimal("CODLOTE"), propostas.asBigDecimal("SEQPRP")};
                           FinderWrapper finderWrapper = new FinderWrapper("AD_TGSCLILTEPPL", "CODLICITACAO = " + propostas.asBigDecimal("CODLICITACAO") + " And CodLote = " + propostas.asBigDecimal("CODLOTE") + " And Codparc = (Select Emp.Codparc From Ad_Tgscli cli Inner Join Tsiemp Emp on Cli.codemp = Emp.codemp Where Cli.codlicitacao = " + propostas.asBigDecimal("CODLICITACAO") + ")");
                           Collection<DynamicVO> dynamicVOs = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
                           Iterator var23 = dynamicVOs.iterator();

                           while(var23.hasNext()) {
                              pregaoPrecoLote = (DynamicVO)var23.next();
                              if (valor.equals("NAO")) {
                                 FinderWrapper finder = new FinderWrapper("AD_TGSCLILTEPPLVLP", "this.CODLICITACAO = " + pregaoPrecoLote.asBigDecimal("CODLICITACAO") + "And this.CODLOTE = " + pregaoPrecoLote.asBigDecimal("CODLOTE") + "And this.SEQPPL = " + pregaoPrecoLote.asBigDecimal("SEQPPL"));
                                 finder.setOrderBy("this.CODCLILTEPPL DESC");
                                 dwfFacade.removeByCriteria(finder);
                                 System.out.println("Removido os Itens com Sucesso");
                              }
                           }
                        } else {
                           pks = new Object[]{propostas.asBigDecimal("CODLICITACAO"), propostas.asBigDecimal("SEQUENCIA"), propostas.asBigDecimal("COCLIDPROPRP")};
                        }

                        PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey(nomeInstancia, pks);
                        EntityVO vo = ple.getValueObject();
                        pregaoPrecoLote = (DynamicVO)vo;
                        pregaoPrecoLote.setProperty("BTAEXEC", "S");
                        pregaoPrecoLote.setProperty("PROPOSTAESCOLHIDA", valor);
                        ple.setValueObject(vo);
                        DynamicVO dynamicVO;
                        if (nomeInstancia.equalsIgnoreCase("AD_TGSCLILTEPRP")) {
                           dynamicVO = (DynamicVO)dwfFacade.getDefaultValueObjectInstance("AD_TGSCLITEMPBTA");
                           dynamicVO.setProperty("CODLICITACAO", propostas.asBigDecimal("CODLICITACAO"));
                           dynamicVO.setProperty("SEQUENCIA", propostas.asBigDecimal("CODLOTE"));
                           dynamicVO.setProperty("CODPROPOSTA", propostas.asBigDecimal("SEQPRP"));
                           dynamicVO.setProperty("NROPROPOSTA", propostas.asBigDecimal("NROPROPOSTA"));
                           dynamicVO.setProperty("NOMEENTIDADE", "AD_TGSCLILTEPRP");
                           dynamicVO.setProperty("PRECOVDAINF", propostas.asBigDecimal("PRECOVDAINF"));
                           dynamicVO.setProperty("PROPESCOLHIDA", valor);
                           dynamicVO.setProperty("EXECUTADO", "N");
                           dwfFacade.createEntity("AD_TGSCLITEMPBTA", (EntityVO)dynamicVO);
                        } else if (contador == 0) {
                           dynamicVO = (DynamicVO)dwfFacade.getDefaultValueObjectInstance("AD_TGSCLITEMPBTA");
                           dynamicVO.setProperty("CODLICITACAO", propostas.asBigDecimal("CODLICITACAO"));
                           dynamicVO.setProperty("SEQUENCIA", propostas.asBigDecimal("SEQUENCIA"));
                           dynamicVO.setProperty("CODPROPOSTA", propostas.asBigDecimal("COCLIDPROPRP"));
                           dynamicVO.setProperty("NROPROPOSTA", propostas.asBigDecimal("NROPROPOSTA"));
                           dynamicVO.setProperty("NOMEENTIDADE", "AD_TGSCLIPROPRP");
                           dynamicVO.setProperty("PRECOVDAINF", propostas.asBigDecimal("PRECOVDAINF"));
                           dynamicVO.setProperty("PROPESCOLHIDA", valor);
                           dynamicVO.setProperty("EXECUTADO", "N");
                           dwfFacade.createEntity("AD_TGSCLITEMPBTA", (EntityVO)dynamicVO);
                           ++contador;
                        }
                     }
                  }

                  contexto.setMensagemRetorno("Proposta de Nro. " + nroProposta + " alterada com Sucesso para o Valor {" + valorRepresenta + "}");
               } else {
                  contexto.setMensagemRetorno("Operação Cancelada com Sucesso");
               }
            } finally {
               JapeSession.close(hnd);
            }

         }
      } else {
         throw new Exception("Selecione uma Linha para Executar a Ação");
      }
   }
}