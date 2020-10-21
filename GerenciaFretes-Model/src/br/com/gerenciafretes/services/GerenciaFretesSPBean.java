package br.com.gerenciafretes.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;

import javax.ejb.SessionBean;

import com.google.gson.JsonObject;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.VOProperty;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.util.BaseSPBean;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.sankhya.ws.ServiceContext;

/**
 * @author Alfredo S M
 * @ejb.bean name="GerenciaFretesSP" 
 * jndi-name="br/com/gerenciafretes/services/GerenciaFretesSP" 
 * type="Stateless" 
 * transaction-type="Container" 
 * view-type="remote"
 * 
 * @ejb.transaction type="Supports"
 *
 * @ejb.util generate="false"
 */

public class GerenciaFretesSPBean extends BaseSPBean implements SessionBean{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	
	public void inserirFrete(ServiceContext sctx) {
		
		SessionHandle hnd = null;
		JsonObject response = new JsonObject();
		
		try {
			
			//Abrindo a sessão
			hnd = JapeSession.open();
			
			//Pegar o valor no parâmetro
			BigDecimal vlrFrete = (BigDecimal) MGECoreParameter.getParameter("asm.gerenciafretes","br.com.sankhya.gerenciafretes.param.gervlrfrete");
			
			JapeWrapper gerenFreDetDAO = JapeFactory.dao("GerenciaFretesDet");
			
			//Buscar ID PAI
			JsonObject req = sctx.getJsonRequestBody();
			
			BigDecimal idpai = req.get("IDPAI").getAsBigDecimal();
			
			//Criando Financeiro
			DynamicVO newVO = gerenFreDetDAO.create()
				.set("IDPAI", idpai)
				.set("VALOR", vlrFrete)
				.set("DESCRICAO", "Financeiro teste inserido")
				.save();
		
			//Preparando mensagem de retorno
			response.addProperty("response", "Registro: " + newVO.asBigDecimal("ID") + " inserido com sucesso!");
			
		}catch(Exception e) {
			e.printStackTrace();
			response.addProperty("response","Erro ao inserir: " + e.getMessage()); 
		} finally {
			JapeSession.close(hnd);
		}
	
		//Enviando mensagem de retorno
		sctx.setJsonResponse(response);
		
	}
	
	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	
	public void dividirValor(ServiceContext sctx) {
		
		SessionHandle hnd = null;
		JsonObject response = new JsonObject();
		
		try {
			
			//Abrindo a sessão
			hnd = JapeSession.open();
		
			JapeWrapper gerenFreDetDAO = JapeFactory.dao("GerenciaFretesDet");
			
			//Buscar ID PAI
			JsonObject req = sctx.getJsonRequestBody();
			
			BigDecimal id = req.get("ID").getAsBigDecimal();
			BigDecimal idpai = req.get("IDPAI").getAsBigDecimal();
			BigDecimal valor = req.get("VALOR").getAsBigDecimal();

			//Dividindo o valor do frete
			gerenFreDetDAO.prepareToUpdateByPK(id,idpai)
				.set("VALOR",valor.divide(new BigDecimal(2), 4, RoundingMode.HALF_DOWN)).update();
			
			//Preparando mensagem de retorno
			response.addProperty("response", "O valor do registro: " + id + " foi atualizado com sucesso!");
			
		}catch(Exception e) {
			e.printStackTrace();
			response.addProperty("response","Erro ao dividir valor: " + e.getMessage()); 
		} finally {
			JapeSession.close(hnd);
		}
	
		//Enviando mensagem de retorno
		sctx.setJsonResponse(response);
		
	}
	
	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	
	public void duplicarFrete(ServiceContext sctx) {
		
		SessionHandle hnd = null;
		JsonObject response = new JsonObject();
		
		try {
			
			//Abrindo a sessão
			hnd = JapeSession.open();
				
			JapeWrapper gerenFreDetDAO = JapeFactory.dao("GerenciaFretesDet");
			
			//Buscar ID PAI
			JsonObject req = sctx.getJsonRequestBody();
			
			BigDecimal idpai = req.get("IDPAI").getAsBigDecimal();
			BigDecimal id = req.get("ID").getAsBigDecimal();
			
			//Buscando o valor do dado no banco
			DynamicVO vo = gerenFreDetDAO.findByPK(id,idpai);
			
			//Alterando a chave primaria
			vo.setProperty("ID", null);
			
			//Duplicando, quando insiro o sistema gera uma nova pk
			DynamicVO newVO = duplicar(vo,"GerenciaFretesDet");
			
			//Preparando mensagem de retorno
			response.addProperty("response", "O registro : " + id + " foi duplicado para o " +  newVO.asBigDecimal("ID") );
			
		}catch(Exception e) {
			
			e.printStackTrace();
			response.addProperty("response","Erro ao duplicar: " + e.getMessage()); 
			
		} finally {
			JapeSession.close(hnd);
		}
	
		//Enviando mensagem de retorno
		sctx.setJsonResponse(response);
		
	}
	
	//Método Help responsável pelo duplicamento do lançamento
	private static DynamicVO duplicar(DynamicVO modeloVO, String dao) throws Exception {
		
		try {
			
			JapeWrapper japeDao = JapeFactory.dao(dao);
			FluidCreateVO fluidCreateVO = japeDao.create();
			Iterator <VOProperty>  iterator = modeloVO.iterator();
			
			while(iterator.hasNext()) {
				VOProperty property = iterator.next();
				fluidCreateVO.set(property.getName(), property.getValue());
			}
			
			DynamicVO saved = fluidCreateVO.save();
			
			return saved;
		}	
		catch(Exception e) {
			
			e.printStackTrace();
			throw new Exception(e);
			
		}
	}
	
}


