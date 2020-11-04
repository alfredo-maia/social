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
			
			//Buscando conex�o com o Banco
			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
			
			//Recebendo conex�o jdbc
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			
			//Interceptando os dados do produto
		    itemVo = (DynamicVO) event.getVo();
		    
		    //Buscando dados sobre o produto
		    EntityVO prodModelVo = dwfFacade.findEntityByPrimaryKeyAsVO("Produto", itemVo.asBigDecimal("CODPROD"));
		    DynamicVO prodVo = (DynamicVO) prodModelVo;
		
		    //Buscando Informa��es da TOP
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
		    
		    int codemp = itemVo.asBigDecimal("CODEMP").intValue();
		    
		    if (rs.next()) {
		    	usaLocalGarantia = rs.getString("AD_USALOCALGARANTIA");
		    	tipMov =  rs.getString("TIPMOV");
		    	codTipOper = rs.getString("CODTIPOPER");
		    }
		    
		    //Buscando dados da Empresa
		    EntityVO empFatModel = dwfFacade.findEntityByPrimaryKeyAsVO("Empresa", itemVo.asBigDecimal("CODEMP"));	
		    DynamicVO empFatVo = (DynamicVO) empFatModel;
		    
		    //Buscando dados da UF de faturamento da empresa
		    EntityVO cidModel = dwfFacade.findEntityByPrimaryKeyAsVO("Cidade", empFatVo.asBigDecimal("CODCID"));	
		    DynamicVO cidVo = (DynamicVO) cidModel;

		    //Buscando Informa��es do relat�rio 146
		    FinderWrapper finderWrapper = new FinderWrapper("RELPARMTOP", "NURELPARM = 146 AND CODTIPOPER = " + codTipOper + " AND UFFAT = " + cidVo.asBigDecimal("UF"));
		    
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
	              queOrigem.appendSql("            WHEN (  SELECT C.CODTIPOPER ");
	              queOrigem.appendSql("                      FROM TGFCAB C ");
	              queOrigem.appendSql("                     WHERE C.NUNOTA = ");
	              queOrigem.appendSql("                                     (   SELECT CAB.AD_NUNOTAORIG ");
	              queOrigem.appendSql("                                           FROM TGFCAB CAB ");
	              queOrigem.appendSql("                                          WHERE CAB.NUNOTA = VAR.NUNOTA " );
	              queOrigem.appendSql("                                            AND CODEMP <> 1 ) ");
	              queOrigem.appendSql("                                              ) = 1113 ");
	              queOrigem.appendSql("                                               THEN 'S' ");
	              queOrigem.appendSql("        ELSE 'N' ");
	              queOrigem.appendSql("   END AS LOCALTRANSF ");
	              queOrigem.appendSql("  FROM TGFVAR VAR ");
	              queOrigem.appendSql("  WHERE VAR.NUNOTA = " + itemVo.asBigDecimal("NUNOTA") + " ");
	              queOrigem.appendSql("    AND VAR.SEQUENCIA = " + itemVo.asBigDecimal("SEQUENCIA") + " ");
			    
			    rs = queOrigem.executeQuery();

			    if (rs.next()) {
			    	
			    	usaLocalGarantiaParam = rs.getString("LOCALTRANSF") == null ? usaLocalGarantiaParam : rs.getString("LOCALTRANSF") ;
			    	
			    }
			    
		    	throw new Exception("Maria_Linda: " + usaLocalGarantiaParam );
			    
		    }
		    
		    //1� Regra: Valida��o do Local de Origem
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
		    
		    //2� Regra: Valida��o do Local de Origem
		    if ( ( prodVo.getProperty("CODLOCALPADRAO").toString().isEmpty() || 
		    	    "0".equals(prodVo.getProperty("CODLOCALPADRAO").toString()) 
		    	  ) && prodVo.getProperty("USALOCAL").equals("S")
		    	) {
		    	
		    	   throw new Exception("LOCAL_NAO_DEFINIDO");
		    	   
		    }
		   
		}catch(Exception e) {
		    //2� Regra: Valida��o do Local de Origem
			if(e.getMessage().equals("LOCAL_NAO_DEFINIDO")) {
				
		    	   throw new Exception("O produto " + itemVo.getProperty("CODPROD") + " � controlado por local e n�o existe local padr�o definido"
		    	   		+ " no cadastro de produtos");
		    	   
			}
			System.out.println("Erro: " + e.getMessage());
        	
		}finally {
			//Finalizando a Sess�o
			JapeSession.close(hnd);
		}
	}
	

	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		//Regra para Validar Campo de Garantia para Filial
		SessionHandle hnd = null;
		
		try {
			
			hnd = JapeSession.open();
			//Buscando conex�o com o Banco
			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		    
			//Interceptando os dados do produto
		    DynamicVO itemVo = (DynamicVO) event.getVo();
		    
		    EntityVO nunotaModel = dwfFacade.findEntityByPrimaryKeyAsVO("CabecalhoNota", itemVo.asBigDecimal("NUNOTA"));	
		    DynamicVO nunota = (DynamicVO) nunotaModel;
		    
		    //Recebendo o valor da Flag Garantia Filial
		    String garantiFilial = nunota.asString("AD_RETGARFILIAL");
		    
		  
		    if("SIM".equals(garantiFilial)) {
		    	
		    	//Buscando dados sobre o produto
			    EntityVO prodModelVo = dwfFacade.findEntityByPrimaryKeyAsVO("Produto", itemVo.asBigDecimal("CODPROD"));
			    DynamicVO prodVo = (DynamicVO) prodModelVo;
			    
			    int localPadraoGar  = prodVo.asBigDecimal("AD_CODLOCALPADRAOGAR").intValue();
			    
			    if ( localPadraoGar != 0 ) {
			    	
			    	itemVo.setProperty("CODLOCALORIG",prodVo.getProperty("AD_CODLOCALPADRAOGAR"));
			    }
		    }
		
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}finally {
			//Finalizando a Sess�o
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


}
