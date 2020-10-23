package br.com.sankhya.modelcore.comercial;
import br.com.sankhya.util.troubleshooting.TSLevel;

import br.com.sankhya.util.troubleshooting.SKError;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.FinderException;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.DBColumnMetadata;
import br.com.sankhya.jape.dao.EntityDAO;
import br.com.sankhya.jape.dao.EntityPropertyDescriptor;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.metadata.EntityField;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.ProcedureCaller;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.sankhya.parameter.client.ListBuilder;

import com.google.gson.Gson;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

/**
 * Classe responsável pelo cálculo de preço dinâmico e apresentação de impostos na Consulta de Produtos.
 * @author dalmi.ferreira
 *
 */
public class CalculoPrecoDinamico {

	// ATRIBUTOS DE CLASSE
	private DynamicVO 					modeloCabecalhoVO;
	private Map<String, Object> 		parametrosDeCalculoDePreco;
	private EntityFacade				dwfEntityFacade;
	private ImpostosHelpper 			impostosHelpper;

	// PARÂMETROS
	private BigDecimal					paramModeloCabecalho;
	private String						paramNomeProcedureParaCalcularPreco;
	private String                      ultimoProdutoProcessado;
	private Boolean						paramUsaImpostosPrecoDinamico;

	public CalculoPrecoDinamico() throws Exception {
		inicializarParametrosVariaveis();
	}

	/**
	 *  Informa se o sistema deve utilizar a procedure para cálculo de preço dinâmico.
	 * @return
	 * @throws Exception
	 */
	public static boolean isCalcularPrecoDinamico() throws Exception {
		String param = (String) MGECoreParameter.getParameter("mgecom.nome.procedure.para.calculo.preco.dinamico");
		return StringUtils.getEmptyAsNull(param) != null;
	}

	/**
	 *  Informa se o sistema deve utilizar a procedure para cálculo de preço dinâmico.
	 * @return
	 * @throws Exception
	 */
	public static boolean isCalcularPrecoDinamico(DynamicVO notaVO) throws Exception {
		return isCalcularPrecoDinamico() && ComercialUtils.ehVenda(notaVO.asString("TIPMOV")) && "P".equals(notaVO.asString("TipoOperacao.USARPRECOCUSTO"));
	}

	/**
	 *  Informa se o sistema NÃO deve utilizar a procedure para cálculo de preço dinâmico para empresas específicas.
	 * @return
	 * @throws Exception
	 */
	public static boolean isEmpresaUsaCalculoDinamico(BigDecimal codEmp) throws Exception {
		String paramListaEmpSemPrecoDin = StringUtils.getEmptyAsNull(MGECoreParameter.getParameterAsString("mgecom.lista.empresas.sem.calculo.dinamico"));
		List listaEmpresasSemCalculoDinamico = ListBuilder.buildNumericListFromParameter(paramListaEmpSemPrecoDin);

		return (!listaEmpresasSemCalculoDinamico.contains(codEmp));
	}

	/**
	 * Informa se o sistema deve mostrar os impostos na Consulta de Produtos.
	 * @return
	 * @throws Exception
	 */
	public static boolean isMostrarImpostosNaConsultaProdutos() throws Exception {
		return MGECoreParameter.getParameterAsBoolean("mgecom.mostrar.impostos.na.consulta.de.produtos");
	}

	/**
	 * Verifica se o sistema deve utilizar os impostos de ST e IPI somados a base de cálculo de desconto.
	 * @return
	 * @throws Exception
	 */
	public static boolean isCalcularDescontoSemImpostos() throws Exception {
		return MGECoreParameter.getParameterAsBoolean("com.calc.perc.desc.sem.impostos");
	}

	/**
	 * Efetua o cálculo de preço dinâmico.
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public CalculoPrecoDinamicoResult calcularPreco(CalculoPrecoDinamicoParam param) throws Exception {
		DynamicVO cabVO = param.getCabVO();

		if ((cabVO != null && !isEmpresaUsaCalculoDinamico(cabVO.asBigDecimal("CODEMP"))) || (param.getCodEmp() != null && !isEmpresaUsaCalculoDinamico(param.getCodEmp()))) {
			CalculoPrecoDinamicoResult result = new CalculoPrecoDinamicoResult();
			result.precoDinamico = param.vlrUnit;
			return result;
		}

		if (cabVO == null) {
			cabVO = gerarCabecalhoNotaVO(param);
		}

		DynamicVO itemNotaVO = gerarItemNotaVO(cabVO, param);

		if (paramUsaImpostosPrecoDinamico) {
			calcularImpostosItem(itemNotaVO);
		}
		
		inicializarDadosParaCalculoPreco(cabVO, itemNotaVO, param);

		CalculoPrecoDinamicoResult result = calcularPrecoPelaProcedure();
		result.vlrIcmsSt = itemNotaVO.asBigDecimalOrZero("VLRSUBST");
		result.vlrIpi = itemNotaVO.asBigDecimalOrZero("VLRIPI");

		return result;
	}

	/**
	 * Calcula os impostos para um ItemNota transient.
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public CalculoPrecoDinamicoResult calcularImpostos(CalculoPrecoDinamicoParam param) throws Exception {
		DynamicVO cabVO = param.getCabVO();
		DynamicVO itemNotaVO = gerarItemNotaVO(cabVO, param);

		calcularImpostosItem(itemNotaVO);

		CalculoPrecoDinamicoResult result = new CalculoPrecoDinamicoResult();

		result.vlrIcmsSt = itemNotaVO.asBigDecimalOrZero("VLRSUBST");
		result.vlrIpi = itemNotaVO.asBigDecimalOrZero("VLRIPI");

		return result;
	}

	/*
	 * Efetua o cálculo de impostos do ItemNota.
	*/
	private void calcularImpostosItem(DynamicVO itemNotaVO) throws Exception {
		if (impostosHelpper == null) {
			impostosHelpper = new ImpostosHelpper();

			impostosHelpper.setAtualizaImpostos(false);
			impostosHelpper.setCalcularTudo(false);
			impostosHelpper.setGravarImpostosParaCalculoDinamico(true);
		}
		
		impostosHelpper.setReutilizarAliquota(isUltimoProdutoProcessado(itemNotaVO));

		impostosHelpper.calcularImpostosItem((ItemNotaVO) itemNotaVO.wrapInterface(ItemNotaVO.class), BigDecimal.ZERO);

		itemNotaVO.setProperty("TGFICM_IDALIQ", impostosHelpper.getInfoAliquota().idAliq);
	}

	/*
	 * Verifica se estamos processando o mesmo produto. 
	 */
	private boolean isUltimoProdutoProcessado(DynamicVO itemNotaVO) {
		String produtoAtual = itemNotaVO.asInt("NUNOTA")+":"+itemNotaVO.asInt("CODEMP")+":"+itemNotaVO.asInt("CODPROD");

		if(ultimoProdutoProcessado == null || !ultimoProdutoProcessado.equals(produtoAtual)){
			ultimoProdutoProcessado = produtoAtual;
			return false;
		}

		return true;
	}

	/*
	 * Inicializa os parâmetros do sistema (TSIPAR) e variáveis de classe.
	*/
	private void inicializarParametrosVariaveis() throws Exception {
		dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

		paramModeloCabecalho = (BigDecimal) MGECoreParameter.getParameter("mgecom.modelo.cabecalho.para.calculo.preco.dinamico");
		paramNomeProcedureParaCalcularPreco = (String) MGECoreParameter.getParameter("mgecom.nome.procedure.para.calculo.preco.dinamico");
		paramUsaImpostosPrecoDinamico = (Boolean) MGECoreParameter.getParameter("mgecom.usa.impostos.calculo.preco.dinamico");
	}

	/*
	 * Inicializa os dados necessários para o cálculo de preço da procedure. 
	*/
	private void inicializarDadosParaCalculoPreco(DynamicVO cabVO, DynamicVO itemVO, CalculoPrecoDinamicoParam param) {
		parametrosDeCalculoDePreco = new HashMap<String, Object>();

		BigDecimal codUsuLogado = BigDecimal.ZERO;
		AuthenticationInfo auth = AuthenticationInfo.getCurrentOrNull();

		if (auth != null) {
			codUsuLogado = auth.getUserID();
		}

		BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDNEG");

		if (param.getQtdDinamica() != null) {
			qtdNeg = param.getQtdDinamica();
		}

		parametrosDeCalculoDePreco.put("SEQUENCIA", BigDecimalUtil.getValueOrZero(param.getSequencia()));
		parametrosDeCalculoDePreco.put("NUNOTA", itemVO.asBigDecimal("NUNOTA"));
		parametrosDeCalculoDePreco.put("IDALIQ", itemVO.containsProperty("TGFICM_IDALIQ") ? itemVO.asBigDecimal("TGFICM_IDALIQ") : BigDecimal.ZERO);
		parametrosDeCalculoDePreco.put("CODPROD", itemVO.asBigDecimal("CODPROD"));
		parametrosDeCalculoDePreco.put("CONTROLE", param.getControle());
		parametrosDeCalculoDePreco.put("CODVOL", param.getCodVol());
		parametrosDeCalculoDePreco.put("QTD", qtdNeg);
		parametrosDeCalculoDePreco.put("CODTAB", param.getCodTab());
		parametrosDeCalculoDePreco.put("CODPARC", cabVO.asBigDecimal("CODPARC"));
		parametrosDeCalculoDePreco.put("CODVEND", cabVO.asBigDecimal("CODVEND"));
		parametrosDeCalculoDePreco.put("CODTIPVENDA", cabVO.asBigDecimal("CODTIPVENDA"));
		parametrosDeCalculoDePreco.put("CODTIPOPER", cabVO.asBigDecimal("CODTIPOPER"));
		parametrosDeCalculoDePreco.put("CODEMP", cabVO.asBigDecimal("CODEMP"));
		parametrosDeCalculoDePreco.put("CODUSULOGADO", codUsuLogado);
		parametrosDeCalculoDePreco.put("VLRUNIT", itemVO.asDouble("VLRUNITMOE") > 0 ? itemVO.asBigDecimalOrZero("VLRUNITMOE") : itemVO.asBigDecimalOrZero("VLRUNIT"));
		parametrosDeCalculoDePreco.put("BASEICMS", itemVO.asBigDecimalOrZero("BASEICMS"));
		parametrosDeCalculoDePreco.put("BASESUBST", itemVO.asBigDecimalOrZero("BASESUBSTIT"));
		parametrosDeCalculoDePreco.put("VLRSUBST", itemVO.asBigDecimalOrZero("VLRSUBST"));
		parametrosDeCalculoDePreco.put("VLRICMS", itemVO.asBigDecimalOrZero("VLRICMS"));
		parametrosDeCalculoDePreco.put("VLRIPI", itemVO.asBigDecimalOrZero("VLRIPI"));
		parametrosDeCalculoDePreco.put("ICMSPRO_VLRDIFALDEST", itemVO.containsProperty("ICMSPRO_VLRDIFALDEST") ? itemVO.asBigDecimalOrZero("ICMSPRO_VLRDIFALDEST") : BigDecimal.ZERO);
		parametrosDeCalculoDePreco.put("ICMSPRO_VLRDIFALREM", itemVO.containsProperty("ICMSPRO_VLRDIFALREM") ? itemVO.asBigDecimalOrZero("ICMSPRO_VLRDIFALREM") : BigDecimal.ZERO);
		parametrosDeCalculoDePreco.put("ICMSPRO_VLRFCP", itemVO.containsProperty("ICMSPRO_VLRFCP") ? itemVO.asBigDecimalOrZero("ICMSPRO_VLRFCP") : BigDecimal.ZERO);
		parametrosDeCalculoDePreco.put("IPIPRO_PERCIPI", itemVO.containsProperty("IPIPRO_PERCIPI") ? itemVO.asBigDecimalOrZero("IPIPRO_PERCIPI") : BigDecimal.ZERO);
	}

	/*
	 * Gera um cabecalho temporário para cálculo de impostos e outras informações que necessitam de um VO persistente.
	 * Esse cabeçalho é gerado baseado em um modelo, o qual deve ser definido no parâmetro MODCALCPRECDIN.
	*/
	private DynamicVO gerarCabecalhoNotaVO(CalculoPrecoDinamicoParam param) throws Exception {
		String cacheKey = this.getClass().getName()+":gerarCabecalhoNotaVO_cache@";
		
		if (JapeSession.hasCurrentSession() && JapeSession.getProperty(cacheKey) != null) {
			return (DynamicVO) JapeSession.getProperty(cacheKey);
		}

		DynamicVO modeloCabecalhoVO = getModeloNotaVO();
		DynamicVO topVO = getTopVO(modeloCabecalhoVO.asBigDecimal("CODTIPOPER"));
		DynamicVO tpvVO = getTipoNegociacaoVO(modeloCabecalhoVO.asBigDecimal("CODTIPVENDA"));
		DynamicVO cabecalhoVO = null;
		BigDecimal codEmp =  (param.getCodEmp() != null && !BigDecimal.ZERO.equals(param.getCodEmp())) ? param.getCodEmp() : getCodEmp(modeloCabecalhoVO);

		if (param.getCodParc() == null && (modeloCabecalhoVO.getProperty("CODPARC") == null || BigDecimal.ZERO.equals(modeloCabecalhoVO.asBigDecimal("CODPARC")))) {
			throw (Exception) SKError.registry(TSLevel.ERROR, "CORE_E02293", new Exception("Falha no cálculo de preço dinâmico: Para o modelo de nota " + String.valueOf(paramModeloCabecalho) + " deve ser informado um parceiro."));
		}

		JdbcWrapper jdbcWrapper = null;
		NativeSql insertTGFCAB = null;

		try {
			jdbcWrapper = dwfEntityFacade.getJdbcWrapper();
			jdbcWrapper.openSession();
			
			cabecalhoVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.CABECALHO_NOTA);
			EntityDAO cabDAO = dwfEntityFacade.getDAOInstance(DynamicEntityNames.CABECALHO_NOTA);
			cabDAO.runKeyGenerationTask((EntityVO) cabecalhoVO, jdbcWrapper);
			
			insertTGFCAB = new NativeSql(jdbcWrapper);
			insertTGFCAB.loadSql(this.getClass(), "CalculoPrecoDinamico_insertTGFCAB.sql");

			insertTGFCAB.setNamedParameter("NUNOTA", cabecalhoVO.asBigDecimal("NUNOTA"));
			insertTGFCAB.setNamedParameter("CODPARC",  (param.getCodParc() != null && !BigDecimal.ZERO.equals(param.getCodParc())) ? param.getCodParc() : modeloCabecalhoVO.asBigDecimal("CODPARC"));
			insertTGFCAB.setNamedParameter("CODEMP", codEmp);
			insertTGFCAB.setNamedParameter("CODEMPNEGOC", modeloCabecalhoVO.asBigDecimal("CODEMPNEGOC"));
			insertTGFCAB.setNamedParameter("CODVEND", modeloCabecalhoVO.asBigDecimal("CODVEND"));
			insertTGFCAB.setNamedParameter("CODTIPOPER", topVO.asBigDecimal("CODTIPOPER"));
			insertTGFCAB.setNamedParameter("DHTIPOPER", topVO.asTimestamp("DHALTER"));
			insertTGFCAB.setNamedParameter("CODTIPVENDA", tpvVO != null ? tpvVO.asBigDecimal("CODTIPVENDA") : null);
			insertTGFCAB.setNamedParameter("DHTIPVENDA", tpvVO != null ? tpvVO.asTimestamp("DHALTER") : null);
			insertTGFCAB.setNamedParameter("NUMNOTA", modeloCabecalhoVO.asBigDecimal("NUMNOTA"));
			insertTGFCAB.setNamedParameter("DTNEG", TimeUtils.getNow());
			insertTGFCAB.setNamedParameter("DTALTER", TimeUtils.getNow());
			insertTGFCAB.setNamedParameter("TIPMOV", topVO.asString("TIPMOV"));

			insertTGFCAB.executeUpdate();
			
			cabecalhoVO = (DynamicVO) dwfEntityFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, new Object[]{ cabecalhoVO.asBigDecimal("NUNOTA") });
			
			if (JapeSession.hasCurrentSession()) {
				JapeSession.putProperty(cacheKey, cabecalhoVO);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw (Exception) SKError.registry(TSLevel.ERROR, "CORE_E02294", new Exception("Falha no cálculo de preço dinâmico: Erro ao gerar cabeçalho de nota/pedido.\n" + e.getMessage()));
		} finally {
			NativeSql.releaseResources(insertTGFCAB);
			JdbcWrapper.closeSession(jdbcWrapper);
		}

		return cabecalhoVO;
	}

	/*
	 * Cria um ItemNotaVO transient. Diferentemente do CabecalhoNota, o cálculo de impostos não necessita de
	 * um VO persistente para ItemNota.
	*/
	private DynamicVO gerarItemNotaVO(DynamicVO cabVO, CalculoPrecoDinamicoParam param) throws Exception {
		ItemNotaVO itemVO = (ItemNotaVO) ((DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA)).wrapInterface(ItemNotaVO.class);

		itemVO.setNUNOTA(cabVO.asBigDecimal("NUNOTA"));
		itemVO.setCODEMP(cabVO.asBigDecimal("CODEMP"));
		itemVO.setCODPROD(param.getCodProd());
		itemVO.setVLRUNIT(param.getVlrUnit());
		itemVO.setVLRTOT(param.getVlrTot());
		itemVO.setQTDNEG(param.getQtdNeg());
		itemVO.setVLRDESC(param.getVlrDesc());
		itemVO.setProperty("VLRUNITMOE", param.getVlrUnitMoe());
		itemVO.setProperty("CabecalhoNota", cabVO);

		itemVO.setAceptTransientProperties(true);

		return itemVO;
	}

	/*
	 * Busca o modelo e nota/pedido definido no parâmetro MODCALCPRECDIN.
	 */
	private DynamicVO getModeloNotaVO() throws Exception {
		if (modeloCabecalhoVO == null) {
			try {
				modeloCabecalhoVO = (DynamicVO) dwfEntityFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, new Object[]{paramModeloCabecalho});
			} catch(FinderException e) {
				e.printStackTrace();
				throw (Exception) SKError.registry(TSLevel.ERROR, "CORE_E02295", new Exception("Falha no cálculo de preço dinâmico: O modelo de nota/pedido informado no parâmetro MODCALCPRECDIN não existe.\n" + e.getMessage()));
			}
		}

		return modeloCabecalhoVO;
	}

	/*
	 * Define o código da empresa. Retorna a empresa do modelo caso esse possua empresa informada. Caso contrário retorna a 
	 * empresa do usuário logado. 
	*/
	private BigDecimal getCodEmp(DynamicVO modeloCabecalhoVO) throws Exception {
		BigDecimal codEmp = BigDecimal.ZERO;

		AuthenticationInfo auth = AuthenticationInfo.getCurrentOrNull();

		if (auth != null) {
			DynamicVO usuarioVO = (DynamicVO) dwfEntityFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.USUARIO, new Object[]{ BigDecimalUtil.getValueOrZero(auth.getUserID()) });

			codEmp = usuarioVO.asBigDecimalOrZero("CODEMP");
		}

		if (codEmp.compareTo(BigDecimal.ZERO) == 0) {
			codEmp = modeloCabecalhoVO.asBigDecimalOrZero("CODEMP");
		}

		if (codEmp.compareTo(BigDecimal.ZERO) == 0) {
			throw (Exception) SKError.registry(TSLevel.ERROR, "CORE_E02296", new Exception("Falha no cálculo de preço dinâmico: Nenhuma empresa foi defina no modelo de nota/pedido e o usuário logado não está vinculado a uma empresa."));
		}

		return codEmp;
	}

	/*
	 * Executa a procedure responsável por calcular o preço dinâmico. 
	 */
	private CalculoPrecoDinamicoResult calcularPrecoPelaProcedure() throws Exception {
		CalculoPrecoDinamicoResult result = null;

		JdbcWrapper jdbcWrapper = null;

		try {
			jdbcWrapper = dwfEntityFacade.getJdbcWrapper();
			jdbcWrapper.openSession();

			ProcedureCaller caller = new ProcedureCaller(paramNomeProcedureParaCalcularPreco);
			caller.setBatchUpdateSize(100);

			// Injeta os parâmetros nessários para o cálculo de preço da procedure.
			for (Iterator<Entry<String, Object>> it = parametrosDeCalculoDePreco.entrySet().iterator(); it.hasNext();) {
				Entry<String, Object> entry = it.next();

				String type = null;
				String name = entry.getKey();
				Object value = entry.getValue();

				if (value instanceof Integer) {
					type = ProcedureCaller.DB_PARAM_NUMINT;
				} else if (value instanceof Number) {
					type = ProcedureCaller.DB_PARAM_NUMDEC;
				} else if (value instanceof Timestamp) {
					type = ProcedureCaller.DB_PARAM_DATE;
				} else {
					type = ProcedureCaller.DB_PARAM_TEXT;
				}

				caller.addDBInputParameter(type, name, value);
			}

			// A procedure recebe um único parâmetro de entrada (IDSESSAO).
			caller.addInputParameter(caller.getExecutionID());

			// O retorno da procedure é um Json do tipo chave-valor, que no caso é o nome do campo e o valor desse campo.
			caller.addOutputParameter(Types.VARCHAR, "result");

			caller.execute(jdbcWrapper.getConnection());

			result = processarResultadoProcedure(caller.resultAsString("result"));

		} catch (Exception e) {
			e.printStackTrace();
			throw (Exception) SKError.registry(TSLevel.ERROR, "CORE_E02297", new Exception("Falha no cálculo de preço dinâmico: Erro ao executar a procedure " + paramNomeProcedureParaCalcularPreco + "\n" + e.getMessage()));
		} finally {
			JdbcWrapper.closeSession(jdbcWrapper);
		}

		return result;
	}

	private CalculoPrecoDinamicoResult processarResultadoProcedure(String value) throws Exception {
		Gson gson = new Gson();

		Map<String, String> campos = (Map<String, String>) gson.fromJson(value, Object.class);

		CalculoPrecoDinamicoResult result = new CalculoPrecoDinamicoResult();

		result.precoDinamico = BigDecimalUtil.valueOf(campos.get("PRECO").replace(",", "."));

		campos.remove("PRECO");

		EntityDAO itemDAO = EntityFacadeFactory.getDWFFacade().getDAOInstance(DynamicEntityNames.ITEM_NOTA);
        Map<String, EntityPropertyDescriptor> descriptors = itemDAO.getSQLProvider().getAllFieldsByName();

		for (Iterator<Entry<String, String>> it = campos.entrySet().iterator(); it.hasNext();) {
			Entry<String, String> entry = it.next();

			String fieldName = entry.getKey();
			String fieldValue = entry.getValue();

			EntityPropertyDescriptor descriptor = descriptors.get(fieldName);

			if (descriptor != null) {
				DBColumnMetadata cmd = descriptor.getColumnMetadata();

				if (cmd.getJapeType() == EntityField.NUMBER && StringUtils.getEmptyAsNull(fieldValue) != null) {
					fieldValue = fieldValue.replace(",", ".");
				}

				Object newValue = EntityDAO.convertToPrimitive(descriptor, fieldValue, false);
				
				if (cmd.getJapeType() == EntityField.NUMBER && newValue != null) {
                    BigDecimal bd = BigDecimalUtil.valueOf(newValue.toString());
                    if(bd.longValue() == bd.floatValue()){
                        bd.setScale(0);
                    }    
                    newValue = bd;
                }

				result.camposPorValor.put(fieldName, newValue);
			}
		}

		return result;
	}
	
	private DynamicVO getTopVO(BigDecimal codTipOper) throws Exception {
		JdbcWrapper jdbcWrapper = null;
		NativeSql queTop = null;
		Collection<DynamicVO> topsVO = new ArrayList<DynamicVO>();
		
		try {
			jdbcWrapper = dwfEntityFacade.getJdbcWrapper();
			jdbcWrapper.openSession();
		
			String camposTOP = "CODTIPOPER,DHALTER,TIPMOV";

			queTop = new NativeSql(jdbcWrapper);
			queTop.appendSql(" SELECT ");
			queTop.appendSql(camposTOP);
			queTop.appendSql(" FROM TGFTOP T WHERE T.CODTIPOPER = :CODTIPOPER ");
			queTop.appendSql(" AND T.DHALTER = (SELECT MAX(TPO.DHALTER) FROM TGFTOP TPO WHERE TPO.CODTIPOPER = T.CODTIPOPER) ");

			queTop.setNamedParameter("CODTIPOPER", codTipOper);
			topsVO = queTop.asVOCollection(DynamicEntityNames.TIPO_OPERACAO, camposTOP);

			if (topsVO.iterator().hasNext()) {
				return (DynamicVO) topsVO.iterator().next();
			} else {
				throw (IllegalStateException) SKError.registry(TSLevel.ERROR, "CORE_E02298", new IllegalStateException("TOP não encontrada."));
			}
		} finally {
			NativeSql.releaseResources(queTop);
			JdbcWrapper.closeSession(jdbcWrapper);
		}
	}
	
	private DynamicVO getTipoNegociacaoVO(BigDecimal codTipVenda) throws Exception {
		if (BigDecimalUtil.getValueOrZero(codTipVenda).intValue() == 0) {
			return null;
		}
		
		FinderWrapper finder = new FinderWrapper(DynamicEntityNames.TIPO_NEGOCIACAO, "this.CODTIPVENDA = ? AND this.DHALTER = (SELECT MAX(TPV.DHALTER) FROM TGFTPV TPV WHERE TPV.CODTIPVENDA = this.CODTIPVENDA)", new Object[] { codTipVenda });
		
		Collection tiposNegociacao = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(finder);
		
		if (tiposNegociacao.isEmpty()) {
			throw (IllegalStateException) SKError.registry(TSLevel.ERROR, "CORE_E02299", new IllegalStateException("Tipo de Negociação não encontrada."));
		}
		
		return (DynamicVO) tiposNegociacao.iterator().next();
	}

	public static class CalculoPrecoDinamicoParam {
		private DynamicVO cabVO;
		private BigDecimal codProd;
		private BigDecimal vlrUnit;
		private BigDecimal vlrUnitMoe;
		private BigDecimal vlrTot;
		private BigDecimal vlrTotMoe;
		private BigDecimal vlrDesc;
		private BigDecimal qtdNeg;
		private BigDecimal qtdDinamica;
		private BigDecimal sequencia;
		private BigDecimal codParc;
		private BigDecimal codEmp;
		private String controle;
		private String codVol;
		private BigDecimal codTab;

		public DynamicVO getCabVO() {
			return cabVO;
		}

		public void setCabVO(DynamicVO cabVO) {
			this.cabVO = cabVO;
		}

		public BigDecimal getCodProd() {
			return codProd;
		}

		public void setCodProd(BigDecimal codProd) {
			this.codProd = codProd;
		}

		public BigDecimal getVlrUnit() {
			return vlrUnit;
		}

		public void setVlrUnit(BigDecimal vlrUnit) {
			this.vlrUnit = vlrUnit;
		}

		public BigDecimal getVlrUnitMoe() {
			return vlrUnitMoe;
		}

		public void setVlrUnitMoe(BigDecimal vlrUnitMoe) {
			this.vlrUnitMoe = vlrUnitMoe;
		}

		public BigDecimal getVlrTot() {
			return vlrTot;
		}

		public void setVlrTot(BigDecimal vlrTot) {
			this.vlrTot = vlrTot;
		}

		public BigDecimal getVlrTotMoe() {
			return vlrTotMoe;
		}

		public void setVlrTotMoe(BigDecimal vlrTotMoe) {
			this.vlrTotMoe = vlrTotMoe;
		}

		public BigDecimal getVlrDesc() {
			return vlrDesc;
		}

		public void setVlrDesc(BigDecimal vlrDesc) {
			this.vlrDesc = vlrDesc;
		}

		public BigDecimal getQtdNeg() {
			return qtdNeg;
		}

		public void setQtdNeg(BigDecimal qtdNeg) {
			this.qtdNeg = qtdNeg;
		}

		public BigDecimal getQtdDinamica() {
			return qtdDinamica;
		}

		public void setQtdDinamica(BigDecimal qtdDinamica) {
			this.qtdDinamica = qtdDinamica;
		}

		public BigDecimal getSequencia() {
			return sequencia;
		}

		public void setSequencia(BigDecimal sequencia) {
			this.sequencia = sequencia;
		}
		
		public BigDecimal getCodParc() {
			return codParc;
		}

		public void setCodParc(BigDecimal codParc) {
			this.codParc = codParc;
		}
		
		public BigDecimal getCodEmp() {
			return codEmp;
		}

		public void setCodEmp(BigDecimal codEmp) {
			this.codEmp = codEmp;
		}

		public String getControle() {
			return controle;
		}

		public void setControle(String controle) {
			this.controle = controle;
		}

		public String getCodVol() {
			return codVol;
		}

		public void setCodVol(String codVol) {
			this.codVol = codVol;
		}
		
		public BigDecimal getCodTab() {
			return codTab;
		}
		
		public void setCodTab(BigDecimal codTab) {
			this.codTab = codTab;
		}
	}

	public static class CalculoPrecoDinamicoResult {
		private BigDecimal precoDinamico;
		private BigDecimal vlrIpi;
		private BigDecimal vlrIcmsSt;
		private Map<String, Object> camposPorValor;

		public CalculoPrecoDinamicoResult() {
			camposPorValor = new HashMap<String, Object>();
		}

		public BigDecimal getPrecoDinamico() {
			return precoDinamico;
		}

		public BigDecimal getVlrIpi() {
			return vlrIpi;
		}

		public BigDecimal getVlrIcmsSt() {
			return vlrIcmsSt;
		}

		public Map<String, Object> getCamposPorValor() {
			return camposPorValor;
		}
	}
}
