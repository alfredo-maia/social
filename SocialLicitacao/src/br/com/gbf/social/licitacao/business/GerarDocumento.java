/* Decompiler 18ms, total 204ms, lines 141 */
package br.com.gbf.social.licitacao.business;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.CentralFaturamento.ConfiguracaoFaturamento;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class GerarDocumento {
   public HashMap<String, BigDecimal> criarDocumento(BigDecimal p_CodLicitacao, String p_NroOcp) throws Exception {
      HashMap<String, BigDecimal> map = new HashMap();
      EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
      AuthenticationInfo auth = AuthenticationInfo.getCurrent();
      BigDecimal nuNota = null;
      BigDecimal nuNotaListagem = null;

      try {
         DynamicVO licitacaoVo = (DynamicVO)dwfEntityFacade.findEntityByPrimaryKeyAsVO("AD_TGSCLI", p_CodLicitacao);
         DynamicVO ordemCompraVo = (DynamicVO)dwfEntityFacade.findEntityByPrimaryKeyAsVO("AD_TGSCLIOCP", new Object[]{p_CodLicitacao, p_NroOcp});
         DynamicVO relParmVO = (DynamicVO)dwfEntityFacade.findEntityByPrimaryKeyAsVO("RELPARM", BigDecimal.valueOf(140L));
         DynamicVO modeloNotaVO = (DynamicVO)dwfEntityFacade.findEntityByPrimaryKeyAsVO("CabecalhoNota", relParmVO.asBigDecimal("NUNOTA"));
         DynamicVO topVO = ComercialUtils.getTipoOperacao(modeloNotaVO.asBigDecimal("CODTIPOPER"));
         DynamicVO tpvVO = ComercialUtils.getTipoNegociacao(ordemCompraVo.asBigDecimal("CODTIPVENDA"));
         DynamicVO pedidoVO = (DynamicVO)dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
         pedidoVO.setProperty("CODEMP", modeloNotaVO.asBigDecimal("CODEMP"));
         pedidoVO.setProperty("TIPMOV", topVO.asString("TIPMOV"));
         pedidoVO.setProperty("DTNEG", TimeUtils.getNow());
         pedidoVO.setProperty("CODTIPOPER", modeloNotaVO.asBigDecimal("CODTIPOPER"));
         pedidoVO.setProperty("DHTIPOPER", topVO.asTimestamp("DHALTER"));
         pedidoVO.setProperty("CODTIPVENDA", tpvVO.asBigDecimal("CODTIPVENDA"));
         pedidoVO.setProperty("DHTIPVENDA", tpvVO.asTimestamp("DHALTER"));
         pedidoVO.setProperty("CODPARC", licitacaoVo.asBigDecimal("CODPARC"));
         pedidoVO.setProperty("CODNAT", modeloNotaVO.asBigDecimal("CODNAT"));
         pedidoVO.setProperty("CODCENCUS", modeloNotaVO.asBigDecimal("CODCENCUS"));
         pedidoVO.setProperty("CIF_FOB", modeloNotaVO.asString("CIF_FOB"));
         pedidoVO.setProperty("AD_CODLICITACAO", ordemCompraVo.asBigDecimal("CODLICITACAO"));
         pedidoVO.setProperty("AD_CODLICITACAOOC", ordemCompraVo.asString("NROOCP"));
         CACHelper cacHelper = new CACHelper();
         JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
         PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfEntityFacade, "CabecalhoNota", pedidoVO);
         BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);
         DynamicVO newCabVO = bRegrasCab.getState().getNewVO();
         nuNota = newCabVO.asBigDecimal("NUNOTA");
         Collection<DynamicVO> produtosOc = dwfEntityFacade.findByDynamicFinderAsVO(new FinderWrapper("AD_TGSCLIOCPPRO", "this.CODLICITACAO = " + p_CodLicitacao + " And this.NROOCP = '" + p_NroOcp + "' And this.LIBERACAO IN ('S', 'L')"));
         Collection<PrePersistEntityState> itensNota = new ArrayList();
         if (produtosOc.size() < 1) {
            throw new Exception("Não foram Localizados Produtos para Geração da Ordem de Compra <b>" + p_NroOcp + "</b> da Licitação de Código <b>" + p_CodLicitacao + "</b>.");
         } else {
            Iterator notasFaturadas = produtosOc.iterator();

            DynamicVO dyna;
            while(notasFaturadas.hasNext()) {
               DynamicVO produtoOc = (DynamicVO)notasFaturadas.next();
               dyna = (DynamicVO)dwfEntityFacade.getDefaultValueObjectInstance("ItemNota");
               dyna.setProperty("CODPROD", produtoOc.asBigDecimal("CODPROD"));
               dyna.setProperty("QTDNEG", produtoOc.asBigDecimal("QTDNEG"));
               dyna.setProperty("VLRUNIT", produtoOc.asBigDecimal("VLRUNIT"));
               dyna.setProperty("VLRDESC", BigDecimal.valueOf(0L));
               dyna.setProperty("PERCDESC", BigDecimal.valueOf(0L));
               dyna.setProperty("NUPROMOCAO", (Object)null);
               PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade, "ItemNota", dyna);
               itensNota.add(itePreState);
            }

            try {
               cacHelper.incluirAlterarItem(nuNota, auth, itensNota, true);
               bRegrasCab = BarramentoRegra.build(CentralFaturamento.class, "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
               bRegrasCab.setValidarSilencioso(true);
               ConfirmacaoNotaHelper.confirmarNota(nuNota, bRegrasCab);
            } catch (Exception e) {
               PersistentLocalEntity entity = dwfEntityFacade.findEntityByPrimaryKey("CabecalhoNota", nuNota);
               dyna = (DynamicVO)entity.getValueObject();
               dyna.setProperty("AD_CODLICITACAO", (Object)null);
               dyna.setProperty("AD_CODLICITACAOOC", (Object)null);
               entity.setValueObject((EntityVO)dyna);
               entity.remove();
               throw new Exception("Licitação: " + p_CodLicitacao + e.getMessage());
            }

            if (ordemCompraVo.asString("GERARLISTAGEM") == null) {
               throw new Exception("O Campo Gerar Listagem não pode ser Nulo");
            } else {
               if (!ordemCompraVo.asString("GERARLISTAGEM").equals("NG")) {
                  Collection<BigDecimal> nuNotaCollection = new ArrayList();
                  nuNotaCollection.add(nuNota);
                  notasFaturadas = null;
                  ConfiguracaoFaturamento cfg = new ConfiguracaoFaturamento();
                  cfg.setCodTipOper(BigDecimal.valueOf(1002L));
                  cfg.setDtFaturamento(TimeUtils.getNow());
                  cfg.setUmaNotaPorPedido(true);
                  cfg.setSerie("");
                  cfg.setDtEntSai(TimeUtils.getNow());
                  cfg.setHrEntSai(TimeUtils.getNow());
                  cfg.setFaturaEmEstoque(true);
                  if (ordemCompraVo.asString("GERARLISTAGEM").equals("GD")) {
                     cfg.setConfirmarNota(false);
                  } else if (ordemCompraVo.asString("GERARLISTAGEM").equals("GC")) {
                     cfg.setConfirmarNota(true);
                  }

                  cfg.setDeixarItemPendente(false);
                  cfg.setValidarData(false);
                  cfg.setEhWizardFaturamento(false);
                  CentralFaturamento centralFaturamento = new CentralFaturamento();
                  centralFaturamento.setConfiguracaoFaturamento(cfg);
                  Collection<BigDecimal> notasFaturadas = centralFaturamento.faturar(nuNotaCollection, new HashMap(), (BigDecimal)null, EntityFacadeFactory.getDWFFacade().getJdbcWrapper());
                  Iterator<BigDecimal> it = notasFaturadas.iterator();
                  if (it.hasNext()) {
                     nuNotaListagem = (BigDecimal)it.next();
                  }

                  map.put("NUNOTALISTAGEM", nuNotaListagem);
               }

               map.put("NUNOTA", nuNota);
               return map;
            }
         }
      } catch (Exception var27) {
         var27.printStackTrace();
         throw new Exception(var27.getMessage());
      }
   }
}