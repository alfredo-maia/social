package br.com.sankhya.acompfrete.listeners;

import java.math.BigDecimal;

import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;

public class AcompFreteListener extends PersistenceEventAdapter {

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
		DynamicVO newVO = (DynamicVO) event.getVo();
		
		if(StringUtils.getNullAsEmpty(newVO.asString("TIPVIAGEM")).isEmpty())
			throw new MGEModelException("Por favor, preencha o campo de 'Tipo de Viagem'");
		
		System.out.println("Veiculo sendo salvo: " + 
		newVO.asBigDecimal("CODVEICULO"));
		
		newVO.setProperty("DTALTER", TimeUtils.getNow());
		
		BigDecimal codUsu = (BigDecimal) JapeSessionContext.getProperty("usuario_logado");

		newVO.setProperty("CODUSUARIO", codUsu);
		
		newVO.setProperty("OBSERVACOES", "TESTE CAMPO CLOB".toCharArray());
		
		/*
		 varchar2 = string
		 number = bigdedcimal
		 float = bigdecimal
		 char = char
		 clob = char[]
		 blob = byte[]
		 date = timestamp
		 */
		
	}

	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		
		DynamicVO newVO = (DynamicVO) event.getVo();
		DynamicVO oldVO = (DynamicVO) event.getOldVO();
		
		char[] obs1 = oldVO.asClob("OBSERVACOES");
		char[] obs2 = newVO.asClob("OBSERVACOES");
		
		if( (obs1 != null && new String(obs1).equals(new String(obs2))) || (obs1 == obs2)) {
			throw new MGEModelException("Favor alterar campo observações");
		}
		
		newVO.setProperty("DTALTER", TimeUtils.getNow());
		
		BigDecimal codUsu = (BigDecimal) JapeSessionContext.getProperty("usuario_logado");

		newVO.setProperty("CODUSUARIO", codUsu);
		
		System.out.println("afterUpdate");
	}

}
