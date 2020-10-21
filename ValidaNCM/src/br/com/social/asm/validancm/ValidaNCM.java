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
	    
		//Criando variável para receber a sessão
		JapeSession.SessionHandle hnd = null;
		
		try {
			 
			//Abrindo a sessão
			hnd = JapeSession.open();
			
			//Recebendo uma conexão com o Banco
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			
			//Modelo de objeto antigo
		    DynamicVO prodVoOld = (DynamicVO) event.getOldVO();
		    
		    //Modelo de objeto novo
			DynamicVO prodVO = (DynamicVO) event.getVo();
			
			//Recebendo NCM antigo
			int ncmOld = prodVoOld.asBigDecimal("NCM").intValue();
			
			//Recebendo NCM novo 
			int ncmNew = prodVO.asBigDecimal("NCM").intValue();
			
		    //Buscando autorização de mudança
			
			/* Construindo validação do usuário  
			 * 
			 */
		      
		}catch (Exception e) {
			
		}finally {
			//Finalizando a sessão
			JapeSession.close(hnd);
		}

		
	}
	
	
	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// Não utilizado
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		// Não utilizado
		
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// Não utilizado
		
	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// Não utilizado
		
	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// Não utilizado
		
	}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
		// Não utilizado
		
	}

}
