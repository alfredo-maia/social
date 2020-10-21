package br.com.sankhya.acompfrete.services;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.SessionBean;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.acompfrete.helpers.ChgHelper;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.BaseSPBean;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.sankhya.ws.ServiceContext;

/**
 * @author Wellyton
 * @ejb.bean name="LancamentoNotasFreteSP" 
 * jndi-name="br/com/sankhya/acompfrete/services/LancamentoNotasFreteSP" 
 * type="Stateless" 
 * transaction-type="Container" 
 * view-type="remote"
 * @ejb.transaction type="Supports"
 *
 * @ejb.util generate="false"
 */
public class LancamentoNotasFreteSPBean extends BaseSPBean implements SessionBean {

	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public void lancarNota(final ServiceContext ctx) throws MGEModelException {
		
		/*
		 {
		 "CODPARC": 0
		 "CODCIDDESPACHO":0
		 "CODCIDDESTINO":0
		 "VLRNOTA":-1
		 "ITENS":[{
				 "codprod": 1
				 "qtd": 12
				 "preco": 1},
				 {
				 "codprod": 2
				 "qtd": 6
				 "preco": 1.12}
				 }
		 		]
		 }
		 */
		
		String fileAsString = "";
		
		try {
			
			InputStream is = new FileInputStream("C:\\Users\\wellyton.santos\\Downloads\\nota.json");
			BufferedReader buf = new BufferedReader(new InputStreamReader(is));
			        
			String line = buf.readLine();
			StringBuilder sb = new StringBuilder();
			        
			while(line != null){
			   sb.append(line).append("\n");
			   line = buf.readLine();
			}
			        
			fileAsString = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		final JsonObject request = new JsonParser().parse(fileAsString).getAsJsonObject();
		JsonObject response = new JsonObject();

		ChgHelper.verificaCampoObrigatorio(request.get("CODPARC"), "CODPARC");
		ChgHelper.verificaCampoObrigatorio(request.get("CODCIDDESPACHO"), "CODCIDDESPACHO");
		ChgHelper.verificaCampoObrigatorio(request.get("CODCIDDESTINO"), "CODCIDDESTINO");
		ChgHelper.verificaCampoObrigatorio(request.get("VLRNOTA"), "VLRNOTA");

		final JapeWrapper notaDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);

		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();

			BigDecimal nunotaModelo = (BigDecimal) MGECoreParameter.getParameter("mge.acompfrete", "br.com.sankhya.acompfrete.parametro.chgnotamodfrete");

			final DynamicVO cabModVO = notaDAO.findByPK(nunotaModelo);

			if (cabModVO == null)
				throw new MGEModelException("O parametro: CHGNOTAMODFRETE (nota modelo) deve ser preenchido com um valor de nota modelo valido (TGFCAB)");
			
			//insert do cabeçalho
			
			cabModVO.setProperty("NUNOTA", null);
			cabModVO.setProperty("CODPARC", request.get("CODPARC").getAsBigDecimal());
			cabModVO.setProperty("CODCIDDESTINO", request.get("CODCIDDESPACHO").getAsBigDecimal());
			cabModVO.setProperty("CODCIDENTREGA", request.get("CODCIDDESTINO").getAsBigDecimal());
			cabModVO.setProperty("VLRNOTA", request.get("VLRNOTA").getAsBigDecimal());
			cabModVO.setProperty("DTNEG", TimeUtils.getNow());
			cabModVO.setProperty("TIPMOV", cabModVO.asString("TipoOperacao.TIPMOV"));
			cabModVO.setProperty("DHTIPOPER", null);
			cabModVO.setProperty("DHTIPVENDA", null);
			
			//abrir transacao

			hnd.execWithTX(new JapeSession.TXBlock() {
				public void doWithTx() throws Exception {
					
					System.out.println("Gerando....");
					
					DynamicVO notaGeradaVO = ChgHelper.duplicar(cabModVO, notaDAO.getEntityName());
					
					System.out.println("Nota Gerada: " + notaGeradaVO.asBigDecimal("NUNOTA"));
					
					JsonArray itens = request.get("ITENS").getAsJsonArray();
					
					Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();
					
					EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
					
					CACHelper cac = new CACHelper();
					for (JsonElement itemElem : itens) {
						JsonObject item = itemElem.getAsJsonObject();
						
						ChgHelper.verificaCampoObrigatorio(item.get("CODPROD"), "CODPROD");
						ChgHelper.verificaCampoObrigatorio(item.get("QTD"), "QTD");
						ChgHelper.verificaCampoObrigatorio(item.get("PRECO"), "PRECO");
						ChgHelper.verificaCampoObrigatorio(item.get("CODLOCALORIG"), "CODLOCALORIG");
						
						DynamicVO itemVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
						
						itemVO.setProperty("NUNOTA", notaGeradaVO.asBigDecimal("NUNOTA"));
						itemVO.setProperty("CONTROLE", " ");
						itemVO.setProperty("CODLOCALORIG", item.get("CODLOCALORIG").getAsBigDecimal());
						itemVO.setProperty("CODPROD", item.get("CODPROD").getAsBigDecimal());
						itemVO.setProperty("QTDNEG", item.get("QTD").getAsBigDecimal());
						itemVO.setProperty("VLRUNIT", item.get("PRECO").getAsBigDecimal());
						
						PrePersistEntityState itemMontado = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.ITEM_NOTA, itemVO);

						itensNota.add(itemMontado);

					}

					cac.setPedidoWeb(false);
					cac.incluirAlterarItem(notaGeradaVO.asBigDecimal("NUNOTA"), 
							(AuthenticationInfo) ctx.getAutentication(), itensNota, true);
					
					System.out.println("PRODUTOS INSERIDOS!!");

				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}

	}

}
