package br.com.asm.validaitem;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;

public class ValidaItem implements EventoProgramavelJava{
	
	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
	
	    JapeSession.SessionHandle hnd = null;
	    
	    try {
	    	
	      hnd = JapeSession.open();
	      
	      EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
	      DynamicVO registroVo = (DynamicVO)event.getVo();
	      BigDecimal iteNunota = registroVo.asBigDecimal("NUNOTA");
	      
	      dwfFacade.getDefaultValueObjectInstance()
	      
	      BigDecimal vlrbaixa = registroVo.asBigDecimal("VLRBAIXA");
	     
	      if ((databaixa != null || databaixa.length() > 0) && 
	        vlrbaixa.intValue() > 0) {
	        BigDecimal codemp = (BigDecimal)registroVo.getProperty("CODEMP");
	        BigDecimal codempbaixa = (BigDecimal)registroVo.getProperty("CODEMPBAIXA");
	        BigDecimal nufin = (BigDecimal)registroVo.getProperty("NUFIN");
	        if (codemp.intValue() != codempbaixa.intValue())
	          throw new Exception("<div align='left'>A empresa do lando Nro. <b><font color='#FF0000'>" + 
	              nufin.toString() + "</font> </b> npode ser diferente da empresa da baixa!" + 
	              "</div>"); 
	        BigDecimal contadabaixa = registroVo.asBigDecimal("CODCTABCOINT");
	        if (contadabaixa != null) {
	          System.out.println("Conta: " + contadabaixa);
	          PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("ContaBancaria", contadabaixa);
	          EntityVO vo = ple.getValueObject();
	          DynamicVO dynamicVO = (DynamicVO)vo;
	          BigDecimal empresadaconta = (BigDecimal)dynamicVO.getProperty("CODEMP");
	          if (empresadaconta.intValue() != codempbaixa.intValue())
	            throw new Exception("<div align='left'>A empresa da Conta da Baixa: " + 
	                contadabaixa.toString() + " do Nro. <b><font color='#FF0000'>" + nufin.toString() + 
	                "</font> </b> npode ser diferente da empresa do Lan+ 
	                "</div>"); 
	        } 
	      } 
	    } finally {
	      JapeSession.close(hnd);
	    } 
		
	}
	
	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
