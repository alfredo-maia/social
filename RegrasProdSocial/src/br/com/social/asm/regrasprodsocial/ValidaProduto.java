package br.com.social.asm.regrasprodsocial;

import java.util.Collection;
import java.util.Iterator;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
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
			
			DynamicVO perVo = null;
			
			FinderWrapper finderWrapper = new FinderWrapper("RELPARMUSU","NURELPARM = 144 AND CODUSU = " + codUsu);
			
	        Collection<DynamicVO> dynamicVOs = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
					
	        if (dynamicVOs.isEmpty()) {
	        	return false;
	        } else {
	        	return true;
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
						
						/*
						 * Validando Permissão de Alteração
						 */
						if(permissao == false) {
							throw new Exception("SEM_PEMISSAO");
						}
						
						
						/*
						 * Buscando Tributação de Transferência por NCM
						 */
						
						FinderWrapper finderWrapper = new FinderWrapper("AD_TGSNCMEST", "NCM = " + ncmNew);
						
						Collection<DynamicVO> tributos = dwfFacade.findByDynamicFinder(finderWrapper);
						
						Iterator it = tributos.iterator();
						
						while (it.hasNext()) {
							/*
							 * Alterando o cadastro da Aba Impostos por Empresa
							 */
							DynamicVO trib = (DynamicVO) it.next();
							
							//Buscando Empresa da UF de destino
							DynamicVO empVo = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("UnidadeFederativa", trib.asBigDecimal("UFDEST"));
							
							//Verificando se existe cadastro
							PersistentLocalEntity ple = dwfFacade.findEntityByPrimaryKey("EmpresaProdutoImpostos", new Object[] {empVo.asBigDecimal("AD_CODEMP"),prodVoNew});
							
							EntityVO prodEmpImpModel = (EntityVO) ple.getValueObject();
							
							DynamicVO prodEmpImpVO = (DynamicVO) prodEmpImpModel;
							
							if(prodEmpImpVO.asBigDecimal("CODPROD").intValue() > 0 ) {
								
								prodEmpImpVO.setProperty("TIPSUBST",trib.asString("TIPSUBST"));
								prodEmpImpVO.setProperty("ORIGPROD", prodVoNew.asBigDecimal("ORIGPROD"));
								prodEmpImpVO.setProperty("USOPROD",prodVoNew.asBigDecimal("USOPROD"));
								
								
							}else {
								
							}
						}
					}
					
				}catch (Exception e) {
					
					String msg_err = e.getMessage();
					
					if (msg_err.equals("SEM_PEMISSAO")) {
						
						throw new Exception("<div align='left'> <font color='#FF0000'> Você não possui acesso para alterar o  <b>NCM</b>. </font>  </div>");
						
					}

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
