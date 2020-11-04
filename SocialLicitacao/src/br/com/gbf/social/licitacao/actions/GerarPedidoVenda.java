/* Decompiler 22ms, total 662ms, lines 51 */
package br.com.gbf.social.licitacao.actions;

import br.com.gbf.social.licitacao.business.GerarDocumento;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;

public class GerarPedidoVenda implements AcaoRotinaJava {
   public void doAction(ContextoAcao contexto) throws Exception {
      EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
      Registro[] linhasSelecionadas = contexto.getLinhas();
      Registro[] var7 = linhasSelecionadas;
      int var6 = linhasSelecionadas.length;

      for(int var5 = 0; var5 < var6; ++var5) {
         Registro linha = var7[var5];
         if (!linha.getCampo("STATUSOC").equals("PN")) {
            contexto.mostraErro("Ordem de Compra <b>" + linha.getCampo("NROOCP") + "</b> está com o Status diferente de <b>Pedido Não Emitido</b>.<br><br>Cancelando a Operação.");
         }

         StringBuffer pkRegistro = new StringBuffer();
         pkRegistro.append(linha.getCampo("CODLICITACAO"));
         pkRegistro.append("_");
         pkRegistro.append(linha.getCampo("NROOCP"));
         pkRegistro.append("_");
         pkRegistro.append("AD_TGSCLIOCP");
         FinderWrapper finder = new FinderWrapper("AD_TGSCLIOCPDOC", " this.CODLICITACAO = " + linha.getCampo("CODLICITACAO") + " And this.NROOCP = '" + (String)linha.getCampo("NROOCP") + "' ");
         Collection<DynamicVO> coll = dwfFacade.findByDynamicFinderAsVO(finder);
         if (coll.isEmpty()) {
            throw new Exception("O arquivo .PDF da ordem de compra não foi adicionado na aba Documentos. Adicione-o para que possa gerar o pedido da ordem de compra.");
         }

         GerarDocumento cab = new GerarDocumento();
         HashMap<String, BigDecimal> mapaValores = cab.criarDocumento((BigDecimal)linha.getCampo("CODLICITACAO"), (String)linha.getCampo("NROOCP"));
         linha.setCampo("NUNOTAPED", mapaValores.get("NUNOTA"));
         linha.setCampo("NUNOTALISTAGEM", mapaValores.get("NUNOTALISTAGEM"));
         linha.setCampo("FINANCEIRO", "NN");
         linha.setCampo("STATUSOC", "PE");
      }

      contexto.setMensagemRetorno("Pedidos de Venda Gerado com Sucesso");
   }
}