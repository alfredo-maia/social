package br.com.social.asm.regrasprodsocial;

import java.math.BigDecimal;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ValidaProduto implements EventoProgramavelJava{
    
	int codUsu;
	
	public void setCodUsu(int codigo) {
		this.codUsu = codigo;
	}
	
	public int getCodUsu() {
		return this.codUsu;
	}
	
	public static String trataNulo(String field) {
		
		if(field.isEmpty() || field == null) {
			field = "N";
		}
		
		return field;
	}
	
	public boolean validaPermissao(int codUsu) {
		
		SessionHandle hnd = null;
				
		try {
			
			hnd = JapeSession.open();
			
			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
			
			DynamicVO perVo = (DynamicVO)dwfFacade.findEntityByPrimaryKeyAsVO("RELPARMUSU",new Object[]{new BigDecimal(144),new BigDecimal(codUsu)});
			
			if (perVo.containsProperty("CODUSU") || codUsu == 0) {
				return true;
			}else {
				return false;
			}

		}catch(Exception e) {
			
			e.printStackTrace();
			return false;
			
		}finally {
			JapeSession.close(hnd);
		}
		
	}
	
	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		
				SessionHandle hnd = null;
				
				AuthenticationInfo auth = AuthenticationInfo.getCurrent();
				
				try {
					 
					//Abrindo a sessão
					hnd = JapeSession.open();
					
					//Recebendo uma conexão com o Banco
					EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
				
					//Obtendo valores antigos e novos
				    DynamicVO prodVoOld = (DynamicVO) event.getOldVO();
				    DynamicVO prodVoNew = (DynamicVO) event.getVo();
				    
				    //Pegando o valor do campo
					String ncmOld = prodVoOld.asString("NCM");
					String ncmNew = prodVoNew.asString("NCM");
					
					//Tratando objetos nulos
					ncmOld = ValidaProduto.trataNulo(ncmOld);
					ncmNew = ValidaProduto.trataNulo(ncmNew);
					
					if (!ncmOld.equals(ncmNew)) {
						
						ValidaProduto usu = new ValidaProduto();
						usu.setCodUsu(auth.getUserID().intValue());
						
						boolean permissao = usu.validaPermissao(usu.getCodUsu());
						
						if(!permissao) {
							throw new Exception("Erro de validação do NCM");
						}
						
					}
					
				}catch (Exception e) {
					
					e.printStackTrace();
					
					throw new Exception("<div align='left'> <font color='#FF0000'> Você não possui acesso para alterar o  <b>NCM</b>. </font>  </div>");

				}finally {
					//Finalizando a sessão
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
	public void beforeInsert(PersistenceEvent arg0) throws Exception {}

}
