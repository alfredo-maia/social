package br.com.social.asm.regrasitemnota;

import java.sql.ResultSet;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class RegrasItemNota implements EventoProgramavelJava {
	
	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
		
		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;
		NativeSql queTop = null;
		ResultSet rs = null;
		
		try {
			
			hnd = JapeSession.open();
			
			//Buscando conexão com o Banco
			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
			
			//Recebendo conexão jdbc
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			
			//Interceptando os dados do produto
		    DynamicVO itemVo = (DynamicVO) event.getVo();
		    
		    //Buscando dados sobre o produto
		    EntityVO prodModelVo = dwfFacade.findEntityByPrimaryKeyAsVO("Produto", itemVo.asBigDecimal("CODPROD"));
		    DynamicVO prodVo = (DynamicVO) prodModelVo;
		
		    //Buscando Informações da TOP
		    queTop = new NativeSql(jdbc);
		    queTop.appendSql(" SELECT CAB.TIPMOV AS TIPMOV");
		    queTop.appendSql("      , NVL (TOP.AD_USALOCALGARANTIA, 'N') AS AD_USALOCALGARANTIA "); 
		    queTop.appendSql("      , TOP.CODTIPOPER AS CODTIPOPER "); 
		    queTop.appendSql("   FROM TGFCAB CAB, TGFTOP TOP ");
		    queTop.appendSql(" 	WHERE CAB.NUNOTA = :NUNOTA ");
		    queTop.appendSql("    AND TOP.CODTIPOPER = CAB.CODTIPOPER ");
		    queTop.appendSql("    AND TOP.DHALTER = CAB.DHTIPOPER ");
		    queTop.setNamedParameter("NUNOTA", itemVo.asBigDecimal("NUNOTA"));
		    
		    //Executando consulta
		    rs = queTop.executeQuery();
		    //Recebendo os valores 
		    String tipMov = null, usaLocalGarantia = null, codTipOper = null;
		    
		    if (rs.next()) {
		    	usaLocalGarantia = rs.getString("AD_USALOCALGARANTIA");
		    	tipMov =  rs.getString("TIPMOV");
		    	codTipOper = rs.getString("CODTIPOPER");
		    }
		    
		    throw new Exception("UsaLocalGarantia " + usaLocalGarantia + '\n' +
		    					"Tipo de Movimento: " + tipMov + '\n' +
		    					"Codigo da Operação: " + codTipOper);
		    /*
		}catch(Exception e) {
			
			System.out.println("Erro: " + e.getMessage());
			*/
		}finally {
			//Finalizando a Sessão
			JapeSession.close(hnd);
		}
	}

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

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {}

}
