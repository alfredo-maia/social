package br.com.sankhya.treina.services;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;

import com.google.gson.JsonObject;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.JsonUtils;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.vo.VOProperty;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.BaseSPBean;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.sankhya.modulemgr.MGESession;
import br.com.sankhya.util.troubleshooting.SKError;
import br.com.sankhya.util.troubleshooting.TSLevel;
import br.com.sankhya.ws.ServiceContext;

/**
 * @author Wellyton
 * @ejb.bean name="TreinamentoSP" 
 * jndi-name="br/com/sankhya/treina/services/TreinamentoSP" 
 * type="Stateless" 
 * transaction-type="Container" 
 * view-type="remote"
 * 
 * @ejb.transaction type="Supports"
 *
 * @ejb.util generate="false"
 */
public class TreinamentoSPBean extends BaseSPBean implements SessionBean {

	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	public void importarArquivo(ServiceContext ctx) {
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

			String arquivo = file.getString();
			
			JapeWrapper impgooDAO = JapeFactory.dao("ImportacaoGoogle");
			
			for (String linha : arquivo.split("\n")) {
				
				String[] colunas = linha.split(",");
				impgooDAO.create()
				.set("NOME", colunas[0])
				.set("TELEFONE", colunas[1])
				.set("SEXO", colunas[2])
				.set("OBS", colunas[3].toCharArray())
				.save();
				
				
			}
			
			

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
	public void inserirFinanceiro(ServiceContext ctx) {

		JsonObject req = ctx.getJsonRequestBody();

		BigDecimal codparc = req.get("CODPARC").getAsBigDecimal();
		BigDecimal recdesp = new BigDecimal(req.get("RECDESP").getAsString().replace("'", ""));
		BigDecimal vlrfin = new BigDecimal(req.get("VLRFIN").getAsString());
		String obs = req.get("OBS").getAsString();

		JsonObject resp = new JsonObject();

		SessionHandle hnd = null;
		try {

			hnd = JapeSession.open();
			
			BigDecimal finmod = (BigDecimal) MGECoreParameter.getParameter("mge.treina", "br.com.sankhya.treina.parametro.goofinancmod");

			JapeWrapper finDAO = JapeFactory.dao(DynamicEntityNames.FINANCEIRO);

			DynamicVO finmodeloVO = finDAO.findByPK(finmod);

			finmodeloVO.setProperty("NUFIN", null);
			finmodeloVO.setProperty("CODPARC", codparc);
			finmodeloVO.setProperty("VLRDESDOB", vlrfin);
			finmodeloVO.setProperty("RECDESP", recdesp);
			finmodeloVO.setProperty("DTNEG", TimeUtils.getNow());
			finmodeloVO.setProperty("DTVENC", new Timestamp(TimeUtils.getNextMonthStart(TimeUtils.getNow().getTime())));
			
			DynamicVO novofinanceiro = duplicar(finmodeloVO, "Financeiro");
			
			JapeWrapper gerenciaFinanceiroDAO = JapeFactory.dao("GerenciaFinanceiroMotorista");
			
			BigDecimal codUsu = (BigDecimal) JapeSessionContext.getProperty("usuario_logado");

			gerenciaFinanceiroDAO.create()
			.set("CODPARC", codparc)
			.set("NUFIN", novofinanceiro.asBigDecimal("NUFIN"))
			.set("RECDESP", novofinanceiro.asBigDecimal("RECDESP"))
			.set("DESCRICAO", obs)
			.set("VLRFIN", novofinanceiro.asBigDecimal("VLRDESDOB"))
			.set("DTALTER", TimeUtils.getNow())
			.set("CODUSU", codUsu)
			.save();
			
			resp.addProperty("response", "Financeiro inserido com sucesso!");

		} catch (Exception e) {
			resp.addProperty("response", "Erro ao inserir financeiro. Verificar log! </br>" + e.getMessage());

			e.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}

		ctx.setJsonResponse(resp);
	}

	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	public void inserirNota(ServiceContext ctx) {

		JsonObject req = ctx.getJsonRequestBody();

		BigDecimal codparc = req.get("CODPARC").getAsBigDecimal();

		System.out.println(codparc);

		JsonObject resp = new JsonObject();

		SessionHandle hnd = null;
		try {

			hnd = JapeSession.open();

			BigDecimal nunota = (BigDecimal) MGECoreParameter.getParameter("mge.treina", "br.com.sankhya.treina.parametro.goonotamod");

			JapeWrapper notaDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);

			DynamicVO notamodelo = notaDAO.findByPK(nunota);

			notamodelo.setProperty("NUNOTA", null);
			notamodelo.setProperty("CODPARC", codparc);
			notamodelo.setProperty("DTNEG", TimeUtils.getNow());
			notamodelo.setProperty("DTENTSAI", TimeUtils.getNow());
			notamodelo.setProperty("DHTIPOPER", null);

			DynamicVO novaNota = duplicar(notamodelo, "CabecalhoNota");

			JapeWrapper gerenciaNotasDAO = JapeFactory.dao("GerenciaNotasMotorista");

			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
			Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();

			DynamicVO itemVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);

			itemVO.setProperty("NUNOTA", novaNota.asBigDecimal("NUNOTA"));
			itemVO.setProperty("CONTROLE", " ");
			itemVO.setProperty("CODLOCALORIG", BigDecimalUtil.valueOf(1000));
			itemVO.setProperty("CODPROD", BigDecimalUtil.valueOf(1));
			itemVO.setProperty("QTDNEG", BigDecimalUtil.valueOf(5));
			itemVO.setProperty("VLRUNIT", BigDecimalUtil.valueOf(15));

			PrePersistEntityState itemMontado = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.ITEM_NOTA, itemVO);

			itensNota.add(itemMontado);

			CACHelper cac = new CACHelper();
			cac.setPedidoWeb(false);
			cac.incluirAlterarItem(novaNota.asBigDecimal("NUNOTA"), (AuthenticationInfo) ctx.getAutentication(), itensNota, true);

			BigDecimal codUsu = (BigDecimal) JapeSessionContext.getProperty("usuario_logado");

			gerenciaNotasDAO.create()
			.set("CODPARC", codparc)
			.set("NUNOTA", novaNota.asBigDecimal("NUNOTA"))
			.set("VLRNOTA", novaNota.asBigDecimal("VLRNOTA"))
			.set("DESCRICAO", "Nota de evento inserida!")
			.set("DTALTER", TimeUtils.getNow())
			.set("CODUSU", codUsu)
			.save();

			resp.addProperty("response", "Nota inserida com sucesso!");

		} catch (Exception e) {
			resp.addProperty("response", "Erro ao inserir nota. Verificar log! </br>" + e.getMessage());

			e.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}

		ctx.setJsonResponse(resp);

	}

	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="Required"
	 */
	public void testeTxAut(ServiceContext ctx) throws Exception {
		System.out.println("testeTxAut");

		JsonObject jo = new JsonObject();

		jo.addProperty("Response", "Sucesso!");

		ctx.setJsonResponse(jo);

	}

	/**
	 * @ejb.interface-method tview-tipe="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public void testeTxManual(ServiceContext ctx) throws Exception {

		JsonObject req = ctx.getJsonRequestBody();

		System.out.println(req);

		System.out.println("testeTxManual");

		JsonObject jo = new JsonObject();

		jo.addProperty("Response", "testeTxManual chamado com Sucesso!");

		ctx.setJsonResponse(jo);
	}

	@Override
	public void ejbActivate() throws EJBException, RemoteException {
	}

	@Override
	public void ejbPassivate() throws EJBException, RemoteException {
	}

	@Override
	public void ejbRemove() throws EJBException, RemoteException {
	}

	@Override
	public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
	}

	private static DynamicVO duplicar(DynamicVO modeloVO, String dao) throws Exception {
		try {
			JapeWrapper japeDao = JapeFactory.dao(dao);

			FluidCreateVO fluidCreateVO = japeDao.create();

			Iterator<VOProperty> iterator = modeloVO.iterator();

			while (iterator.hasNext()) {
				VOProperty property = iterator.next();
				fluidCreateVO.set(property.getName(), property.getValue());
			}

			DynamicVO saved = fluidCreateVO.save();

			return saved;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}

	}
}
