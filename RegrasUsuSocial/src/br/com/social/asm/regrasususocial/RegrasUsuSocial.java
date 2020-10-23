package br.com.social.asm.regrasususocial;

import java.math.BigDecimal;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class RegrasUsuSocial implements EventoProgramavelJava{

	@Override
	public void afterInsert(PersistenceEvent event) throws Exception {
		     
		SessionHandle hnd = null;
		
		try {
			 
			//Abrindo a sessão
			hnd = JapeSession.open();
			
			//Recebendo uma conexão com o Banco
			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		
			//Modelo de objeto antigo
		    DynamicVO usuVo = (DynamicVO) event.getVo();
			
			//Recebendo NCM novo 
			String alteraNcm = usuVo.asString("AD_ALTNCM");
			
			if (!alteraNcm.isEmpty()) {
				
				if("S".equals(alteraNcm)) {
					
					//Insere informações no relatório 144
			        EntityVO entityVO = dwfFacade.getDefaultValueObjectInstance("RELPARMUSU");
					DynamicVO parmVO = (DynamicVO)entityVO;
					parmVO.setProperty("CODUSU", usuVo.asBigDecimal("CODUSU"));
					parmVO.setProperty("NURELPARM"	 , new BigDecimal(144));
			        dwfFacade.createEntity("RELPARMUSU", entityVO);
	
				}
			
			}
		      
		}catch (Exception e) {
			
			e.printStackTrace();
			
		}finally {
			//Finalizando a sessão
			JapeSession.close(hnd);
		}
		
	}
	
    
	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
	     
		SessionHandle hnd = null;
		
		try {
			 
			//Abrindo a sessão
			hnd = JapeSession.open();
			
			//Recebendo uma conexão com o Banco
			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		
			//Obtendo valores antigos e novos
		    DynamicVO usuVoOld = (DynamicVO) event.getOldVO();
		    DynamicVO usuVoNew = (DynamicVO) event.getVo();
		    
		    //Pegando o valor do campo
			String ncmOld = usuVoOld.asString("AD_ALTNCM");
			String ncmNew = usuVoNew.asString("AD_ALTNCM");
			
			//Tratando objetos nulos
			ncmOld = RegrasUsuSocial.trataNulo(ncmOld);
			ncmNew = RegrasUsuSocial.trataNulo(ncmNew);
			
			if (ncmOld.equals("N") && ncmNew.equals("S")) {
				
				//Insere informações no relatório 144
		        EntityVO entityVO = dwfFacade.getDefaultValueObjectInstance("RELPARMUSU");
				DynamicVO parmVO = (DynamicVO)entityVO;
				parmVO.setProperty("CODUSU", usuVoNew.asBigDecimal("CODUSU"));
				parmVO.setProperty("NURELPARM"	 , new BigDecimal(144));
		        dwfFacade.createEntity("RELPARMUSU", entityVO);
		        
			}else if (ncmOld.equals("S") && ncmNew.equals("N")) {
				
				//Remover informações no relatório 144
			     FinderWrapper finderWrapper = new FinderWrapper("RELPARMUSU", "NURELPARM = 144 AND CODUSU = " + usuVoNew.asBigDecimal("CODUSU"));
			     dwfFacade.removeByCriteria(finderWrapper);
			   
			}
			
		}catch (Exception e) {
			
			e.printStackTrace();
			
		}finally {
			//Finalizando a sessão
			JapeSession.close(hnd);
		}
		
	}
	
	public static String trataNulo(String field) {
		
		if(field.isEmpty() || field == null) {
			field = "N";
		}
		
		return field;
	}
	

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		
	}
	
	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {}

}
