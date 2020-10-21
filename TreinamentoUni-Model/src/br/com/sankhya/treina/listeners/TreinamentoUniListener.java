package br.com.sankhya.treina.listeners;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

public class TreinamentoUniListener extends PersistenceEventAdapter {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {

		DynamicVO newVO = (DynamicVO) event.getVo();
		
		JapeWrapper parceiroDAO = JapeFactory.dao("Parceiro");
		
		newVO.setProperty("NOMEPARC", parceiroDAO.findByPK(newVO.asBigDecimal("CODPARC")).asString("NOMEPARC"));
		newVO.setProperty("ATIVO", "S");
		
	}

}
