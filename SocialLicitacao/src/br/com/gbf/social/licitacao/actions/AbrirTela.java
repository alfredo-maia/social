package br.com.gbf.social.licitacao.actions;

import java.math.BigDecimal;

import com.sankhya.util.Base64Impl;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class AbrirTela implements AcaoRotinaJava {
   public void doAction(ContextoAcao contexto) throws Exception {
      contexto.setMensagemRetorno(String.valueOf(String.format("Pedido de Nro Único gerada com sucesso.\nClique ", BigDecimal.valueOf(0L))) + this.getLinkNota("aqui", BigDecimal.valueOf(0L)) + " para abrir o Pedido de Venda gerado.");
   }

   private String getLinkNota(String descricao, BigDecimal nuNota) {
      String pk = "{\"CTX.PENDCONFIRMACAO\":true, \"TIPMOV\":\"V\", \"CODEMP\":0, \"DTNEG.INI\":null}";
      String url = "<a title=\"Abrir Tela\" href=\"/mge/system.jsp#app/{0}/{1}\" target=\"_top\"><u><b>{2}</b></u></a>".replace("{0}", Base64Impl.encode("br.com.sankhya.mgecom.mov.selecaodedocumento".getBytes()).trim());
      url = url.replace("{1}", Base64Impl.encode(pk.getBytes()).trim());
      url = url.replace("{2}", descricao);
      return url;
   }
}