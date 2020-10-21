package br.com.sankhya.acompfrete.services;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;

import javax.ejb.SessionBean;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.JdbcUtils;
import com.sankhya.util.JsonUtils;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.acompfrete.helpers.ChgHelper;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.BaseSPBean;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.sankhya.modulemgr.MGESession;
import br.com.sankhya.util.troubleshooting.SKError;
import br.com.sankhya.util.troubleshooting.TSLevel;
import br.com.sankhya.ws.ServiceContext;
import sun.misc.IOUtils;

/**
 * @author Wellyton
 * @ejb.bean name="LancamentoFinancFreteSP" 
 * jndi-name="br/com/sankhya/acompfrete/services/LancamentoFinancFreteSP" 
 * type="Stateless" 
 * transaction-type="Container" 
 * view-type="remote"
 * @ejb.transaction type="Supports"
 *
 * @ejb.util generate="false"
 */
public class LancamentoFinancFreteSPBean extends BaseSPBean implements SessionBean {

	/** * */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * @ejb.interface-method tview-type="remote"
	 * @ejb.transaction type="Required"
	 */
	public void importarArquivo(ServiceContext ctx) throws MGEModelException {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {
			hnd = JapeSession.open();
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

			jdbc = dwfEntityFacade.getJdbcWrapper();
			jdbc.openSession();

			JsonObject params = JsonUtils.getJsonObject(ctx.getJsonRequestBody(), "params");
			String chaveArquivo = JsonUtils.getString(params, "chaveArquivo");

			HttpSession session = MGESession.getSessaoPai(ctx.getHttpRequest().getSession());
			FileItem file = (FileItem) session.getAttribute(chaveArquivo);

			if (file == null) {
				throw (Exception) SKError.registry(TSLevel.ERROR, "FIN_E00116", new Exception("Arquivo não encontrado"));
			}

			if (!(file instanceof FileItem)) {
				throw (Exception) SKError.registry(TSLevel.ERROR, "FIN_E00117", new Exception("Erro interno: Arquivo na sessão não é um FileItem."));
			}

			System.out.println(file.getString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}
	
	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	public void lancarFinanceiro(ServiceContext ctx) throws MGEModelException {

		/*
		 {
		 "IDPAI": 0
		 "CODVEICULO":0
		 "CODPARC":0
		 "VLRDESDOB":1.00
		 "RECDESP":-1
		 "DIASPVENC":15
		 "MOTIVO":"DIARIA HOTEL PERNOITE"
		 }
		 */
		
		JsonObject request = ctx.getJsonRequestBody();
		JsonObject response = new JsonObject();
		
		ChgHelper.verificaCampoObrigatorio(request.get("IDPAI"), "IDPAI");
		ChgHelper.verificaCampoObrigatorio(request.get("CODVEICULO"), "CODVEICULO");
		ChgHelper.verificaCampoObrigatorio(request.get("CODPARC"), "CODPARC");
		ChgHelper.verificaCampoObrigatorio(request.get("VLRDESDOB"), "VLRDESDOB");
		ChgHelper.verificaCampoObrigatorio(request.get("RECDESP"), "RECDESP");
		ChgHelper.verificaCampoObrigatorio(request.get("DIASPVENC"), "DIASPVENC");
		ChgHelper.verificaCampoObrigatorio(request.get("MOTIVO"), "MOTIVO");
		
		JapeWrapper financeiroDAO = JapeFactory.dao(DynamicEntityNames.FINANCEIRO);
		
		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();

			BigDecimal nuFinModelo = (BigDecimal) MGECoreParameter.getParameter("mge.acompfrete", "br.com.sankhya.treinamento.parametro.chgfinmodfrete");
			
			DynamicVO finModVO = financeiroDAO.findByPK(nuFinModelo);
			
			if(finModVO == null)
				throw new MGEModelException("O financeiro modelo deve ser preenchido com um valor de financeiro modelo valido (TGFFIN)");

			finModVO.setProperty("NUFIN", null);
			finModVO.setProperty("CODVEICULO", request.get("CODVEICULO").getAsBigDecimal());
			finModVO.setProperty("CODPARC", request.get("CODPARC").getAsBigDecimal());
			finModVO.setProperty("VLRDESDOB", request.get("VLRDESDOB").getAsBigDecimal());
			finModVO.setProperty("RECDESP", request.get("RECDESP").getAsBigDecimal());
			finModVO.setProperty("DTNEG", TimeUtils.getNow());

			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(TimeUtils.getNow().getTime());
			c.add(Calendar.DAY_OF_MONTH, request.get("DIASPVENC").getAsInt());
			
			finModVO.setProperty("DTVENC", new Timestamp(c.getTimeInMillis()));

			DynamicVO finGeradoVO = ChgHelper.duplicar(finModVO, financeiroDAO.getEntityName());

			JapeWrapper acompFinanc = JapeFactory.dao("AcompFreteFinanceiro");
			
			BigDecimal codUsu = (BigDecimal) JapeSessionContext.getProperty("usuario_logado");

			acompFinanc.create()
				.set("IDACOMPANHAMENTO", request.get("IDPAI").getAsBigDecimal())
				.set("NUFIN", finGeradoVO.asBigDecimal("NUFIN"))
				.set("VLRDESDOB", finGeradoVO.asBigDecimal("VLRDESDOB"))
				.set("RECDESP", finGeradoVO.asBigDecimal("RECDESP"))
				.set("MOTIVO", request.get("MOTIVO").getAsString())
				.set("DTLANC", finGeradoVO.asTimestamp("DTNEG"))
				.set("CODUSU", codUsu)
				.save();
			
			response.addProperty("NUFIN", finGeradoVO.asBigDecimal("NUFIN"));

		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			JapeSession.close(hnd);
		}

		ctx.setJsonResponse(response);

	}

	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	public void executarSelect(ServiceContext ctx) throws MGEModelException {
		//quando o tipo da transação é type="Required" =>> então temos uma transação automática
		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();

			JapeWrapper acompFreteDAO = JapeFactory.dao("AcompanhamentoFreteCHG");

			System.out.println("EXECUTANDO SELECT COM PK");

			DynamicVO registroVO = acompFreteDAO.findByPK(new BigDecimal(2));

			System.out.println("Resultado\n codveiculo: " + registroVO.asBigDecimal("CODVEICULO"));
			System.out.println("nomeparceiromotorista: " + registroVO.asString("Parceiro.NOMEPARC"));

			// ***************************************************************************************

			System.out.println("SELECT USANDO WHERE");

			Collection<DynamicVO> registros = acompFreteDAO.find("codveiculo > ?", new BigDecimal(0));

			System.out.println("Resultado");
			for (DynamicVO regVO : registros) {
				System.out.println("codveiculo: " + regVO.asBigDecimal("CODVEICULO"));
				System.out.println("nomeparceiromotorista: " + regVO.asString("Parceiro.NOMEPARC"));
			}

			// ***************************************************************************************

			System.out.println("SELECT ONE");

			DynamicVO oneVO = acompFreteDAO.findOne("1 = 1");

			System.out.println("Resultado\n codveiculo: " + oneVO.asBigDecimal("CODVEICULO"));
			System.out.println("nomeparceiromotorista: " + oneVO.asString("Parceiro.NOMEPARC"));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}

		JsonObject obj = new JsonObject();

		obj.addProperty("MENSAGEM", "SUCESSO");

		ctx.setJsonResponse(obj);

	}

	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	public void executarInsert(ServiceContext ctx) throws MGEModelException {

		JsonObject request = ctx.getJsonRequestBody();
		JsonObject response = new JsonObject();

		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();

			JapeWrapper acompFreteDAO = JapeFactory.dao("AcompanhamentoFreteCHG");
			JapeWrapper parceiroDAO = JapeFactory.dao(DynamicEntityNames.PARCEIRO);

			DynamicVO newParcVO = parceiroDAO.create().set("NOMEPARC", request.get("NOMEPARC").getAsString()).set("TIPPESSOA", request.get("TIPPESSOA").getAsString()).set("CGC_CPF", request.get("CGC_CPF").getAsString()).set("IDENTINSCESTAD", request.get("IE").getAsString()).set("CODCID", request.get("CODCID").getAsBigDecimal()).set("MOTORISTA", request.get("MOTORISTA").getAsString()).save();

			response.addProperty("MENSAGEM", "Parceiro Criado:" + newParcVO.asBigDecimal("CODPARC"));

		} catch (Exception e) {
			response.addProperty("MENSAGEM", "Erro " + e.getMessage());
			e.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}

		ctx.setJsonResponse(response);

	}

	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	public void executarJsonArray(ServiceContext ctx) throws MGEModelException {

		JsonObject request = ctx.getJsonRequestBody();
		JsonArray response = new JsonArray();

		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();

			JapeWrapper acompFreteDAO = JapeFactory.dao("AcompanhamentoFreteCHG");
			JapeWrapper parceiroDAO = JapeFactory.dao(DynamicEntityNames.PARCEIRO);

			JsonArray ja = request.get("parceiros").getAsJsonArray();

			for (JsonElement jsonElement : ja) {
				JsonObject jo = jsonElement.getAsJsonObject();
				DynamicVO newParcVO = parceiroDAO.create().set("NOMEPARC", jo.get("NOMEPARC").getAsString()).set("TIPPESSOA", jo.get("TIPPESSOA").getAsString()).set("CGC_CPF", jo.get("CGC_CPF").getAsString()).set("IDENTINSCESTAD", jo.get("IE").getAsString()).set("CODCID", jo.get("CODCID").getAsBigDecimal()).set("MOTORISTA", jo.get("MOTORISTA").getAsString()).save();

				JsonObject obj = new JsonObject();
				obj.addProperty("STATUS", "Parceiro Criado:" + newParcVO.asBigDecimal("CODPARC"));

				response.add(obj);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}

		JsonObject jobj = new JsonObject();
		jobj.add("RESPONSE", response);
		ctx.setJsonResponse(jobj);

	}

	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	public void executarUpdate(ServiceContext ctx) throws MGEModelException {

		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();

			JapeWrapper acompFreteDAO = JapeFactory.dao("AcompanhamentoFreteCHG");

			acompFreteDAO.prepareToUpdateByPK(new BigDecimal(5)).set("TIPVIAGEM", "F").set("OBSERVACOES", TimeUtils.getNow().toString().toCharArray()).update();

			Collection<DynamicVO> registros = acompFreteDAO.find("codparcmoto = ?", new BigDecimal("11"));

			for (DynamicVO vo : registros) {
				acompFreteDAO.prepareToUpdate(vo).set("TIPVIAGEM", "T").set("OBSERVACOES", TimeUtils.getNow().toString().toCharArray()).update();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}

		JsonObject obj = new JsonObject();

		obj.addProperty("MENSAGEM", "SUCESSO");

		ctx.setJsonResponse(obj);

	}

	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	public void executarDelete(ServiceContext ctx) throws MGEModelException {

		StringBuilder sb = new StringBuilder();
		boolean isErro = false;

		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();

			JapeWrapper acompFreteDAO = JapeFactory.dao("AcompanhamentoFreteCHG");

			boolean deletou = acompFreteDAO.delete(BigDecimalUtil.valueOf(5));

			sb.append("O delete do registro 5 foi executado com ");
			if (deletou) {
				sb.append("sucesso\n");
			} else {
				sb.append("erro\n");
			}

			int nDelets = acompFreteDAO.deleteByCriteria("1 = 1");

			sb.append("O delete geral eliminou " + nDelets + " linhas da tabela");

		} catch (Exception e) {
			isErro = true;
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			sb.append(sw.toString());
			try {
				enviarEmail("Erro na execusão do serviço", sw.toString(), "wellyton.santos@sankhya.com.br");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} finally {
			JapeSession.close(hnd);
		}

		JsonObject obj = new JsonObject();

		String status = isErro ? "ERRO" : "SUCESSO";

		obj.addProperty(status, sb.toString());

		ctx.setJsonResponse(obj);

	}

	public static void enviarEmail(String titulo, String mensagem, String dest) throws Exception {
		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();

			JapeWrapper ordemServicoDAO = JapeFactory.dao(DynamicEntityNames.FILA_MSG);
			ordemServicoDAO.create().set("EMAIL", dest.trim()).set("CODCON", BigDecimal.ZERO).set("CODMSG", null).set("STATUS", "Pendente").set("TIPOENVIO", "E").set("MAXTENTENVIO", BigDecimalUtil.valueOf(3)).set("ASSUNTO", titulo).set("MENSAGEM", mensagem.toCharArray()).save();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}
	}

	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	public void executarNativeSql(ServiceContext ctx) throws MGEModelException {

		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;
		NativeSql sql = null;
		ResultSet rset = null;
		try {
			hnd = JapeSession.open();
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			jdbc.openSession();

			sql = new NativeSql(jdbc);

			rset = sql.executeQuery("SELECT CODVEICULO FROM TGFVEI");

			System.out.println("Veiculos:");
			while (rset.next()) {
				System.out.println(rset.getBigDecimal(1));
			}

			sql.resetSqlBuf();
			sql.loadSql(LancamentoFinancFreteSPBean.class, "sqlAllRegFrete.sql");
			rset = sql.executeQuery();

			System.out.println("Fretes:");
			while (rset.next()) {
				System.out.println("ID: " + rset.getBigDecimal("IDACOMPANHAMENTO"));
				System.out.println("Tipo Viagem" + rset.getString("TIPVIAGEM"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtils.closeResultSet(rset);
			NativeSql.releaseResources(sql);
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}
	

}