package br.com.social.asm.regrasitemnota;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class RegrasItemNota implements EventoProgramavelJava {
	
	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
		
		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;
		NativeSql queTop = null;
		NativeSql queOrigem = null;
		ResultSet rs = null;
		DynamicVO itemVo = null;
		
		try {
			
			hnd = JapeSession.open();
			
			//Buscando conexão com o Banco
			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
			
			//Recebendo conexão jdbc
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			
			//Interceptando os dados do produto
		    itemVo = (DynamicVO) event.getVo();
		    
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
		    
		    //Buscando Informações do relatório 135
		    FinderWrapper finderWrapper = new FinderWrapper("RELPARMTOP", "NURELPARM = 135 AND CODTIPOPER = " + codTipOper);
		    
		    Collection<DynamicVO> parmModelVOS = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
		    
		    DynamicVO paramVO;
		    
		    String usaLocalGarantiaParam = "N";
		    
		    Iterator ite = parmModelVOS.iterator();
		    
		    if(ite.hasNext()) {
		    	paramVO = (DynamicVO) ite.next();
		    	usaLocalGarantiaParam = paramVO.asString("USALOCALGARANTIA");
		    }
		    
		    //Validando local com a top de origem 
		    if ("S".equals(usaLocalGarantiaParam)){
		    	
		    	rs = null;
		    	
			    queOrigem = new NativeSql(jdbc);
			    queOrigem.appendSql("SELECT CASE WHEN ( SELECT CAB.CODTIPOPER ");
			    queOrigem.appendSql("                     FROM TGFCAB CAB ");
			    queOrigem.appendSql("                     WHERE CAB.NUNOTA = VAR.NUNOTAORIG ");
			    queOrigem.appendSql("                 ) IN (1113,1613) ");
			    queOrigem.appendSql("            THEN 'S' ");
			    queOrigem.appendSql("        ELSE 'N' ");
			    queOrigem.appendSql("   END AS LOCALTRANSF ");
			    queOrigem.appendSql("  FROM TGFVAR VAR ");
			    queOrigem.appendSql("  WHERE VAR.NUNOTA = " + itemVo.asBigDecimal("NUNOTA") + " ");
			    queOrigem.appendSql("    AND VAR.SEQUENCIA = " + itemVo.asBigDecimal("SEQUENCIA") + " ");
			    
			    rs = queOrigem.executeQuery();
			    
			    if (rs.next()) {
			    	
			    	usaLocalGarantiaParam = rs.getString("LOCALTRANSF") == null ? usaLocalGarantiaParam : rs.getString("LOCALTRANSF") ;
			    	
			    }
			    
		    }
		    
		    //1º Regra: Validação do Local de Origem
		    if ("T".equals(tipMov) == false ) {
		    	 
		    	 BigDecimal codLocal = null;
		    	 
		    	 if (("S".equals(usaLocalGarantia) 
	    			     && prodVo.asBigDecimal("AD_CODLOCALPADRAOGAR").intValue() != 0) || "S".equals(usaLocalGarantiaParam)) {
	    				 
				    	itemVo.setProperty("CODLOCALORIG",prodVo.getProperty("AD_CODLOCALPADRAOGAR"));

	    	    } else {
	    	    	itemVo.setProperty("CODLOCALORIG",prodVo.getProperty("CODLOCALPADRAO"));
	    	    }
		    	 
		    	if ( itemVo.getProperty("CODLOCALORIG").toString().equals(prodVo.getProperty("CODLOCALPADRAO"))
		    		  == false  && "N".equals(usaLocalGarantia) ) {
		    		
		    		itemVo.setProperty("CODLOCALORIG", prodVo.getProperty("CODLOCALPADRAO"));
		    	}
		    	
		    }
		    
		    //2º Regra: Validação do Local de Origem
		    if ( ( prodVo.getProperty("CODLOCALPADRAO").toString().isEmpty() || 
		    	    "0".equals(prodVo.getProperty("CODLOCALPADRAO").toString()) 
		    	  ) && prodVo.getProperty("USALOCAL").equals("S")
		    	) {
		    	
		    	   throw new Exception("LOCAL_NAO_DEFINIDO");
		    	   
		    }
		    
		    
		    	 
		}catch(Exception e) {
		    //2º Regra: Validação do Local de Origem
			if(e.getMessage().equals("LOCAL_NAO_DEFINIDO")) {
				
		    	   throw new Exception("O produto " + itemVo.getProperty("CODPROD") + " é controlado por local e não existe local padrão definido"
		    	   		+ " no cadastro de produtos");
		    	   
			}
			System.out.println("Erro: " + e.getMessage());
			
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
