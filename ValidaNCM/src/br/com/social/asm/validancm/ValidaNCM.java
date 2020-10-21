package br.com.social.asm.validancm;

import java.math.BigDecimal;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ValidaNCM implements EventoProgramavelJava{
	
	
	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
	    
		//Criando vari�vel para receber a sess�o
		JapeSession.SessionHandle hnd = null;
		
		try {
			 
			//Abrindo a sess�o
			hnd = JapeSession.open();
			
			//Recebendo uma conex�o com o Banco
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			
			//Modelo de objeto antigo
		    DynamicVO prodVoOld = (DynamicVO) event.getOldVO();
		    
		    //Modelo de objeto novo
			DynamicVO prodVO = (DynamicVO) event.getVo();
			
			//Recebendo NCM antigo
			int ncmOld = prodVoOld.asBigDecimal("NCM").intValue();
			
			//Recebendo NCM novo 
			int ncmNew = prodVO.asBigDecimal("NCM").intValue();
			
		    //Buscando autoriza��o de mudan�a
			
			/* Construindo valida��o do usu�rio  
			 * 
			 */
		      
		}catch (Exception e) {
			
		}finally {
			//Finalizando a sess�o
			JapeSession.close(hnd);
		}

		
	}
	
	
	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// N�o utilizado
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		// N�o utilizado
		
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// N�o utilizado
		
	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// N�o utilizado
		
	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// N�o utilizado
		
	}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
		// N�o utilizado
		
	}

}
