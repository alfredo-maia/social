package br.com.gerenciafretes.listeners;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;


public class GerenciaFretesListener extends PersistenceEventAdapter {

	private static final long serialVersionUID = 1L;

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
		
		//Recebi os registros da tela
		DynamicVO newRegistro = (DynamicVO) event.getVo();
		//Setando a data atual
		newRegistro.setProperty("DTALTER", TimeUtils.getNow());
		//Alterando o valor para ativo
		newRegistro.setProperty("ATIVO","S");
		//Definindo modelo
		JapeWrapper parceiroDAO = JapeFactory.dao("Parceiro");
		
		if(newRegistro.asBigDecimal("CODPARC")!= null) {
			newRegistro.setProperty("NOME", parceiroDAO.findByPK(newRegistro.asBigDecimal("CODPARC")).asString("NOMEPARC") );
		}
	}
	
	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		//Buscando novo valor
		DynamicVO newRegistro = (DynamicVO) event.getVo();
		//Buscando valor antigo
		DynamicVO oldRegistro = (DynamicVO) event.getOldVO();
		
		//Alterando a data de atualização
		newRegistro.setProperty("DTALTER", TimeUtils.getNow());
		
		if ( !newRegistro.asString("ATIVO").equals(oldRegistro.asString("ATIVO")) ){
			String log = "O campo ativo foi alterado de " + oldRegistro.asString("ATIVO").toString() + " para "
										+ " " + newRegistro.asString("ATIVO").toString();
			
			newRegistro.setProperty("OBSERVACAO", log);
									  
		}
	}
}
