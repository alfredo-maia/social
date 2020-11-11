package br.com.gbf.social.portais.actionbuttons;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.sankhya.util.Base64Impl;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class GerarNotaTransferencia implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao contexto) throws Exception {
		
		Registro[] linhas;
		
		final AuthenticationInfo auth = AuthenticationInfo.getCurrent();
		
		final EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		
    	JdbcWrapper jdbc = dwfEntityFacade.getJdbcWrapper(); 
		
		if (contexto.getLinhas().length > 1) {
			contexto.mostraErro("Selecionar apenas 1 (Um) Pedido, para realizar a Geração da Transferência.");
		}
		
		for (int length = (linhas = contexto.getLinhas()).length, i = 0; i < length; i++) {
	    	
			final Registro registro = linhas[i];
			
	
        	try{
        		
        		jdbc.openSession();

        		CallableStatement cstmt = jdbc.getConnection().prepareCall("{call Stp_ValTransf_Social(?)}");
        		cstmt.setQueryTimeout(60);
        		cstmt.setBigDecimal(1, (BigDecimal) registro.getCampo("NUNOTA"));	
        		cstmt.execute();
                               
        	}catch(Exception e){
        		throw new Exception(e.getMessage());
        	} finally {
        		JdbcWrapper.closeSession(jdbc);	
        	}
			
			DynamicVO parceiroVo 	= (DynamicVO)dwfEntityFacade.findEntityByPrimaryKeyAsVO("Parceiro", registro.getCampo("CODPARC"));
			DynamicVO cidadeVo 		= (DynamicVO)dwfEntityFacade.findEntityByPrimaryKeyAsVO("Cidade", parceiroVo.asBigDecimal("CODCID"));			
			DynamicVO ufVo 		 	= (DynamicVO)dwfEntityFacade.findEntityByPrimaryKeyAsVO("UnidadeFederativa", cidadeVo.asBigDecimal("UF"));			
			DynamicVO empresaVo  	= (DynamicVO)dwfEntityFacade.findEntityByPrimaryKeyAsVO("Empresa", ufVo.asBigDecimal("AD_CODEMP"));
			DynamicVO parceiroEmpVo = (DynamicVO)dwfEntityFacade.findEntityByPrimaryKeyAsVO("Parceiro", empresaVo.asBigDecimal("CODPARC"));
			DynamicVO relParmVO 	= (DynamicVO)dwfEntityFacade.findEntityByPrimaryKeyAsVO("RELPARM", BigDecimal.valueOf(125));
			DynamicVO modeloNotaVO 	= (DynamicVO)dwfEntityFacade.findEntityByPrimaryKeyAsVO("CabecalhoNota", relParmVO.asBigDecimal("NUNOTA"));
			
			DynamicVO topVO = ComercialUtils.getTipoOperacao(modeloNotaVO.asBigDecimal("CODTIPOPER"));
			DynamicVO tpvVO = ComercialUtils.getTipoNegociacao(modeloNotaVO.asBigDecimal("CODTIPVENDA"));
			
			/*
			 * Gerar o Cabeçalho da Nota
			 */
			DynamicVO pedidoVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
			
			pedidoVO.setProperty("CODEMP"     	, modeloNotaVO.asBigDecimal("CODEMP"));
			pedidoVO.setProperty("TIPMOV" 		, topVO.asString("TIPMOV"));
			pedidoVO.setProperty("DTNEG"		, TimeUtils.getNow());
			
			pedidoVO.setProperty("CODTIPOPER" 	, modeloNotaVO.asBigDecimal("CODTIPOPER"));
			pedidoVO.setProperty("DHTIPOPER"	, topVO.asTimestamp("DHALTER"));
			pedidoVO.setProperty("CODTIPVENDA" 	, modeloNotaVO.asBigDecimal("CODTIPVENDA"));
			pedidoVO.setProperty("DHTIPVENDA"	, tpvVO.asTimestamp("DHALTER"));
			
			pedidoVO.setProperty("CODPARC"		, parceiroEmpVo.asBigDecimal("CODPARC"));
			pedidoVO.setProperty("CODNAT"		, modeloNotaVO.asBigDecimal("CODNAT"));
			pedidoVO.setProperty("CODCENCUS"	, modeloNotaVO.asBigDecimal("CODCENCUS"));

			pedidoVO.setProperty("CODPARCDEST"	, modeloNotaVO.asBigDecimal("CODEMP"));
			pedidoVO.setProperty("CODEMPNEGOC"	, parceiroEmpVo.asBigDecimal("CODPARC"));

			pedidoVO.setProperty("CIF_FOB"	, modeloNotaVO.asString("CIF_FOB"));
			pedidoVO.setProperty("AD_NUNOTAORIG"	, registro.getCampo("NUNOTA"));

			BigDecimal nuNota = (BigDecimal) registro.getCampo("NUNOTA");
			
			CACHelper cacHelper = new CACHelper();
  	    	 
  	    	JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", (Object)Boolean.TRUE);
  	    	PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfEntityFacade, "CabecalhoNota", pedidoVO);
  	    	BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);
  	    	
  	         DynamicVO newCabVO = bRegrasCab.getState().getNewVO();
  	        
  	        Collection<DynamicVO> produtosVo = dwfEntityFacade.findByDynamicFinderAsVO
  	        		                               (new FinderWrapper("ItemNota", "this.NUNOTA = " + registro.getCampo("NUNOTA")));

            Collection<PrePersistEntityState> itensNota = new ArrayList();
          
            if (produtosVo.size() < 1) {
                throw new Exception("Não foram Localizados Produtos para Geração da Transferência ");
            } else {
  	    	
                for (DynamicVO iteVo : produtosVo) {
					
                	DynamicVO itemVo;
                	
                	itemVo = (DynamicVO)dwfEntityFacade.getDefaultValueObjectInstance("ItemNota");
                	itemVo.setProperty("CODPROD", iteVo.asBigDecimal("CODPROD"));
                	itemVo.setProperty("QTDNEG", iteVo.asBigDecimal("QTDNEG"));
                	
                	PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade, "ItemNota", itemVo);
    				itensNota.add(itePreState);
    	        	
				}
	            
  	        }
  	    	
	        try {
	        	
        		jdbc.openSession();

   	    		cacHelper.incluirAlterarItem(newCabVO.asBigDecimal("NUNOTA"), auth, itensNota, true);
   	  
                CentralFaturamento fat = new CentralFaturamento();
                
                Collection<BigDecimal> notasSelecao = new ArrayList();
                
                notasSelecao.add(newCabVO.asBigDecimal("NUNOTA"));
                
                fat.faturar(notasSelecao,nuNota,jdbc);
                
                fat.
   	    		
   	    	}catch (Exception e) {
   	    		 e.printStackTrace();
   	    		 //final PersistentLocalEntity entity = dwfEntityFacade.findEntityByPrimaryKey("CabecalhoNota", (Object)newCabVO.asBigDecimal("NUNOTA"));
   	    		
   	    		// entity.remove();
   	    		 
   	    	 }
	        
	        finally {
	        	
        		JdbcWrapper.closeSession(jdbc);	

	        }
	    

			/*
			 * Finalização do Pedido de Venda
			 */
	        
			contexto.setMensagemRetorno(String.valueOf(String.format("Pedido de Nro Único gerada com sucesso.\nClique ", nuNota)) + this.getLinkNota("aqui", newCabVO.asBigDecimal("NUNOTA")) + " para abrir o Pedido de Venda gerado.");	
		}
		
	}
	
	private String getLinkNota(final String descricao, final BigDecimal nuNota) {
        final String pk = "{\"NUNOTA\":\"{0}\"}".replace("{0}", nuNota.toString());
        String url = "<a title=\"Abrir Tela\" href=\"/mge/system.jsp#app/{0}/{1}\" target=\"_top\"><u><b>{2}</b></u></a>".replace("{0}", Base64Impl.encode("br.com.sankhya.com.mov.CentralNotas".getBytes()).trim());
        url = url.replace("{1}", Base64Impl.encode(pk.getBytes()).trim());
        url = url.replace("{2}", descricao);
        return url;
    }

}
