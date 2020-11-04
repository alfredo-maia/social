package br.com.social.asm.regrascabecalhonota;

import java.util.Collection;

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

public class RegrasCabNota implements EventoProgramavelJava {
	

	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		//Regra para Validar Campo de Garantia para Filial
		SessionHandle hnd = null;
		
		try {
			
			hnd = JapeSession.open();
			//Buscando conexão com o Banco
			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
			
			DynamicVO cabVo = (DynamicVO) event.getVo();
					
		    //Recebendo o valor da Flag Garantia Filial
		    String garantiFilial = cabVo.asString("AD_RETGARFILIAL");
		    
		    if("SIM".equals(garantiFilial)) {
		    	
		    	FinderWrapper finderWrapper = new FinderWrapper("ItemNota", "NUNOTA = " + cabVo.asBigDecimal("NUNOTA"));
		    	
		    	Collection <DynamicVO> itensVo = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
		    	
		    	for(DynamicVO itens : itensVo) {
		    		
		    		PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("ItemNota", new Object[] {itens.asBigDecimal("NUNOTA"), itens.asBigDecimal("SEQUENCIA")});
		    		
		    		
		    		EntityVO vo = ple.getValueObject();
		    		DynamicVO itemVo = (DynamicVO) vo;
		    		EntityVO prodModel = dwfFacade.findEntityByPrimaryKeyAsVO("Produto", itemVo.asBigDecimal("CODPROD"));
		    		DynamicVO prodVo = (DynamicVO) prodModel;
		    		
		    		int localPadraoGar  = prodVo.asBigDecimal("AD_CODLOCALPADRAOGAR").intValue();
				    
				    if ( localPadraoGar != 0 ) {
				    	
				    	itemVo.setProperty("CODLOCALORIG",prodVo.getProperty("AD_CODLOCALPADRAOGAR"));
				    	ple.setValueObject(vo);
				  
				    } else {
				    	
				    	itemVo.setProperty("CODLOCALORIG",prodVo.getProperty("CODLOCALPADRAO"));
				    	ple.setValueObject(vo);

				    }
				    
				    throw new Exception("Tô no produto: " + itemVo.asBigDecimal("CODPROD").intValue());
		    		
		    	}
		    	
		    }else if("NAO".equals(garantiFilial)) {

		    	FinderWrapper finderWrapper = new FinderWrapper("ItemNota", "NUNOTA = " + cabVo.asBigDecimal("NUNOTA"));
		    	
		    	Collection <DynamicVO> itensVo = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
		    	
		    	for(DynamicVO itens : itensVo) {
		    		
		    		PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("ItemNota", new Object[] {itens.asBigDecimal("NUNOTA"), itens.asBigDecimal("SEQUENCIA")});
		    		
		    		EntityVO vo = ple.getValueObject();
		    		DynamicVO itemVo = (DynamicVO) vo;
		    		EntityVO prodModel = dwfFacade.findEntityByPrimaryKeyAsVO("Produto", itemVo.asBigDecimal("CODPROD"));
		    		DynamicVO prodVo = (DynamicVO) prodModel;
		    		
		    		int localPadraoGar  = prodVo.asBigDecimal("AD_CODLOCALPADRAOGAR").intValue();
				    
				    if ( localPadraoGar != 0 ) {
				    	
				    	itemVo.setProperty("CODLOCALORIG",prodVo.getProperty("CODLOCALPADRAO"));
				    }
		        }
		     }

		}catch(Exception e) {
			
			e.printStackTrace();
		
		}finally {
			//Finalizando a Sessão
			JapeSession.close(hnd);
		}

		
	}
	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {}

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {}


}
