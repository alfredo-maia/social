package br.com.asm.custompanel;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.custompanel.CustomPanel;
import br.com.sankhya.modelcore.custompanel.CustomPanelMetadados;
import br.com.sankhya.modelcore.custompanel.CustomPanelResult;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class CustomPanelImplExternal implements CustomPanel {
  public CustomPanelResult buildResult(Map<String, Object> parameters) throws SQLException {
    CustomPanelResult customPanelResult = new CustomPanelResult();
    if (parameters != null && !parameters.isEmpty()) {
      JdbcWrapper jdbcWrapper = null;
      try {
        List<Map<String, Object>> resultado = new ArrayList<>();
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        jdbcWrapper = dwfFacade.getJdbcWrapper();
        jdbcWrapper.openSession();
        NativeSql sqlProduto = montarConsultaProduto(jdbcWrapper, parameters);
        NativeSql sqlEstoque = montarConsultaEstoque(jdbcWrapper);
        sqlProduto.setNamedParameter("CODPROD", BigDecimalUtil.getValueOrZero((BigDecimal)parameters.get("CODPROD")));
        ResultSet rsetProduto = sqlProduto.executeQuery();
        ResultSetMetaData rsetMetadaProduto = rsetProduto.getMetaData();
        Connection conn = jdbcWrapper.getConnection();
        String cacheKey = String.valueOf(CustomPanelImplExternal.class.getName()) + "@chamaProcedureObtemPreco3.cachekey";
        CallableStatement cstmt = (CallableStatement)((ReusableConnection)conn).getStatementFromCache(cacheKey);
        if (cstmt == null) {
          cstmt = conn.prepareCall("{call STP_OBTEM_PRECO3(?,?,?,?,?,?)}");
          ((ReusableConnection)conn).addStatementToCache(cacheKey, (AutoCloseableStatement)cstmt);
        } 
        try {
          BigDecimal preco = BigDecimal.ZERO;
          while (rsetProduto.next()) {
            sqlEstoque.setNamedParameter("CODPROD", rsetProduto.getBigDecimal("CODPROD"));
            ResultSet rsetEstoque = null;
            try {
              rsetEstoque = sqlEstoque.executeQuery();
              ResultSetMetaData rsetMetadaEstoque = rsetEstoque.getMetaData();
              while (rsetEstoque.next()) {
                Map<String, Object> linha = new HashMap<>();
                int i;
                for (i = 1; i <= rsetMetadaProduto.getColumnCount(); i++) {
                  String nomeColuna = StringUtils.removePontuacao(StringUtils.replaceAccentuatedChars(StringUtils.replaceString(rsetMetadaProduto.getColumnName(i), " ", "_"))).toUpperCase();
                  linha.put(nomeColuna, JdbcUtils.getTypedFieldFromResultSet(nomeColuna, rsetProduto));
                } 
                for (i = 1; i <= rsetMetadaEstoque.getColumnCount(); i++) {
                  String nomeColuna = StringUtils.removePontuacao(StringUtils.replaceAccentuatedChars(StringUtils.replaceString(rsetMetadaEstoque.getColumnName(i), " ", "_"))).toUpperCase();
                  linha.put(nomeColuna, JdbcUtils.getTypedFieldFromResultSet(nomeColuna, rsetEstoque));
                } 
                parameters.put("CODPROD", rsetProduto.getBigDecimal("CODPROD"));
                preco = getPreco(cstmt, parameters);
                linha.put("Pre, preco);
                resultado.add(linha);
              } 
            } finally {
              JdbcUtils.closeResultSet(rsetEstoque);
            } 
          } 
          customPanelResult.getResult().addAll(resultado);
        } finally {
          cstmt.close();
        } 
      } catch (Exception var36) {
        IllegalStateException error = new IllegalStateException(var36);
        throw error;
      } finally {
        JdbcWrapper.closeSession(jdbcWrapper);
      } 
    } 
    return customPanelResult;
  }
  
  public NativeSql montarConsultaProduto(JdbcWrapper jdbcWrapper, Map<String, Object> parameters) {
    NativeSql sql = new NativeSql(jdbcWrapper);
    sql.appendSql(" SELECT X.* FROM ");
    sql.appendSql(" ( ");
    sql.appendSql(" SELECT PRO.CODPROD,\tPRO.DESCRPROD, PRO.REFERENCIA, PRO.MARCA, PRO.CODVOL, PRO.ATIVO, PRO.CODGRUPOPROD, PRO.DESCMAX,\tPRO.DECQTD,\tPRO.DECVLR,\tPRO.PESOLIQ, ");
    sql.appendSql(" PRO.PESOBRUTO, PRO.PROMOCAO, PRO.CODMOEDA, PRO.LOCALIZACAO,\tVOL.DESCRVOL, MOE.NOMEMOEDA, GRU.DESCRGRUPOPROD, GRU.VALEST ");
    sql.appendSql(" FROM ");
    sql.appendSql(" TGFPAL PAI, TGFPRO PRO ");
    sql.appendSql(" LEFT JOIN TSIMOE MOE ON MOE.CODMOEDA = PRO.CODMOEDA ");
    sql.appendSql(" LEFT JOIN TGFVOL VOL ON VOL.CODVOL = PRO.CODVOL ");
    sql.appendSql(" INNER JOIN TGFGRU GRU ON GRU.CODGRUPOPROD = PRO.CODGRUPOPROD ");
    sql.appendSql(" WHERE ");
    sql.appendSql(" PAI.CODPROD = :CODPROD ");
    sql.appendSql(" AND PAI.CODPRODALT = PRO.CODPROD ");
    if ("S".equals(parameters.get("TIPDIRPROALT"))) {
      sql.appendSql(" UNION ");
      sql.appendSql(" SELECT PRO.CODPROD,\tPRO.DESCRPROD, PRO.REFERENCIA, PRO.MARCA, PRO.CODVOL, PRO.ATIVO, PRO.CODGRUPOPROD, PRO.DESCMAX,\tPRO.DECQTD,\tPRO.DECVLR,\tPRO.PESOLIQ, ");
      sql.appendSql(" PRO.PESOBRUTO, PRO.PROMOCAO, PRO.CODMOEDA, PRO.LOCALIZACAO,\tVOL.DESCRVOL, MOE.NOMEMOEDA, GRU.DESCRGRUPOPROD, GRU.VALEST ");
      sql.appendSql(" FROM ");
      sql.appendSql(" TGFPAL FILHO, TGFPRO PRO ");
      sql.appendSql(" LEFT JOIN TSIMOE MOE ON MOE.CODMOEDA = PRO.CODMOEDA ");
      sql.appendSql(" LEFT JOIN TGFVOL VOL ON VOL.CODVOL = PRO.CODVOL ");
      sql.appendSql(" INNER JOIN TGFGRU GRU ON GRU.CODGRUPOPROD = PRO.CODGRUPOPROD ");
      sql.appendSql(" WHERE ");
      sql.appendSql(" FILHO.CODPRODALT = :CODPROD ");
      sql.appendSql(" AND FILHO.CODPRODALT = PRO.CODPROD ");
    } 
    if ("S".equals(parameters.get("PERCADPRODSUBST"))) {
      sql.appendSql(" UNION ");
      sql.appendSql(" SELECT PRO.CODPROD,\tPRO.DESCRPROD, PRO.REFERENCIA, PRO.MARCA, PRO.CODVOL, PRO.ATIVO, PRO.CODGRUPOPROD, PRO.DESCMAX,\tPRO.DECQTD,\tPRO.DECVLR,\tPRO.PESOLIQ, ");
      sql.appendSql(" PRO.PESOBRUTO, PRO.PROMOCAO, PRO.CODMOEDA, PRO.LOCALIZACAO,\tVOL.DESCRVOL, MOE.NOMEMOEDA, GRU.DESCRGRUPOPROD, GRU.VALEST ");
      sql.appendSql(" FROM ");
      sql.appendSql(" TGFPAL FILHO, TGFPAL PAI, TGFPRO PRO ");
      sql.appendSql(" LEFT JOIN TSIMOE MOE ON MOE.CODMOEDA = PRO.CODMOEDA ");
      sql.appendSql(" LEFT JOIN TGFVOL VOL ON VOL.CODVOL = PRO.CODVOL ");
      sql.appendSql(" INNER JOIN TGFGRU GRU ON GRU.CODGRUPOPROD = PRO.CODGRUPOPROD ");
      sql.appendSql(" WHERE ");
      sql.appendSql(" FILHO.CODPRODALT = :CODPROD ");
      sql.appendSql(" AND PRO.CODPROD <> :CODPROD ");
      sql.appendSql(" AND FILHO.CODPROD = PAI.CODPROD ");
      sql.appendSql(" AND PAI.CODPRODALT = PRO.CODPROD ");
    } 
    sql.appendSql(" ) X ");
    sql.appendSql(" ORDER BY X.DESCRPROD ");
    return sql;
  }
  
  public NativeSql montarConsultaEstoque(JdbcWrapper jdbcWrapper) {
    NativeSql sql = new NativeSql(jdbcWrapper);
    sql.setReuseStatements(true);
    sql.appendSql(" SELECT CODEMP, CODLOCAL, CONTROLE, ESTOQUE, RESERVADO, CODBARRA, DTVAL, DTFABRICACAO, (ESTOQUE - RESERVADO) AS DISPONIVEL FROM TGFEST E ");
    sql.appendSql(" WHERE ");
    sql.appendSql(" \tE.CODPROD = :CODPROD ");
    return sql;
  }
  
  public BigDecimal getPreco(CallableStatement cstmt, Map<String, Object> parameters) throws SQLException {
    BigDecimal preco = BigDecimal.ZERO;
    try {
      cstmt.setBigDecimal(1, BigDecimalUtil.getValueOrZero((BigDecimal)parameters.get("NUTAB")));
      cstmt.setBigDecimal(2, BigDecimalUtil.getValueOrZero((BigDecimal)parameters.get("CODPROD")));
      cstmt.setBigDecimal(3, BigDecimalUtil.getValueOrZero((BigDecimal)parameters.get("CODLOCAL")));
      if (parameters.containsKey("CONTROLE") && parameters.get("CONTROLE") != null) {
        cstmt.setString(4, parameters.get("CONTROLE").toString());
      } else {
        cstmt.setString(4, " ");
      } 
      if (parameters.containsKey("DTNEG") && parameters.get("DTNEG") != null) {
        cstmt.setTimestamp(5, (Timestamp)parameters.get("DTNEG"));
      } else {
        cstmt.setTimestamp(5, new Timestamp(TimeUtils.getToday()));
      } 
      cstmt.registerOutParameter(6, 8);
      cstmt.execute();
      preco = BigDecimalUtil.getValueOrZero(cstmt.getBigDecimal(6));
      cstmt.clearParameters();
      return preco;
    } catch (Exception var6) {
      IllegalStateException error = new IllegalStateException(var6);
      throw error;
    } 
  }
  
  public CustomPanelResult getMetadados(Map<String, Object> parameters) {
    List<CustomPanelMetadados> metadados = new LinkedList<>();
    CustomPanelResult customPanel = new CustomPanelResult();
    JdbcWrapper jdbcWrapper = null;
    try {
      EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
      jdbcWrapper = dwfFacade.getJdbcWrapper();
      jdbcWrapper.openSession();
      NativeSql sql = montarConsultaProduto(jdbcWrapper, parameters);
      sql.setNamedParameter("CODPROD", BigDecimal.ZERO);
      ResultSet rset = sql.executeQuery();
      ResultSetMetaData rsetMetada = rset.getMetaData();
      int indice = 0;
      int indice2 = 0;
      for (indice = indice2 + 1; indice <= rsetMetada.getColumnCount(); indice++)
        metadados.add(getMetaDadosProdutoEstoque(customPanel, rsetMetada, indice)); 
      indice = 0;
      sql = montarConsultaEstoque(jdbcWrapper);
      sql.setNamedParameter("CODPROD", BigDecimal.ZERO);
      rset = sql.executeQuery();
      rsetMetada = rset.getMetaData();
      for (; ++indice <= rsetMetada.getColumnCount(); indice++)
        metadados.add(getMetaDadosProdutoEstoque(customPanel, rsetMetada, indice)); 
      metadados.add(getMetaDadosProcedurePreco(indice));
      customPanel.setMetadados(metadados);
      return customPanel;
    } catch (Exception var13) {
      IllegalStateException error = new IllegalStateException(var13);
      throw error;
    } finally {
      JdbcWrapper.closeSession(jdbcWrapper);
    } 
  }
  
  public CustomPanelMetadados getMetaDadosProdutoEstoque(CustomPanelResult customPanel, ResultSetMetaData rsetMetada, int indice) throws SQLException, ClassNotFoundException {
    CustomPanelMetadados metadados = new CustomPanelMetadados();
    metadados.setIndice(indice);
    metadados.setNomeColuna(customPanel.buildNomeCampo(rsetMetada.getColumnName(indice)));
    if (CustomPanelResult.PRODUTO_COLUMN_DESCRIPTION.containsKey(rsetMetada.getColumnLabel(indice))) {
      metadados.setDescricao((String)CustomPanelResult.PRODUTO_COLUMN_DESCRIPTION.get(rsetMetada.getColumnLabel(indice)));
    } else if (CustomPanelResult.ESTOQUE_COLUMN_DESCRIPTION.containsKey(rsetMetada.getColumnLabel(indice))) {
      metadados.setDescricao((String)CustomPanelResult.ESTOQUE_COLUMN_DESCRIPTION.get(rsetMetada.getColumnLabel(indice)));
    } else {
      metadados.setDescricao(rsetMetada.getColumnLabel(indice));
    } 
    metadados.setTipoColuna(metadados.getDataType(Class.forName(rsetMetada.getColumnClassName(indice)), rsetMetada.getColumnType(indice), rsetMetada.getScale(indice)));
    return metadados;
  }
  
  public CustomPanelMetadados getMetaDadosProcedurePreco(int indice) {
    CustomPanelMetadados metadados = new CustomPanelMetadados();
    metadados.setIndice(indice);
    metadados.setNomeColuna("PRECO");
    metadados.setDescricao("Pre);
    metadados.setTipoColuna(CustomPanelDataType.DECIMAL);
    return metadados;
  }
  
  public Class<?> getInterfaceToSearch() {
    return CustomPanel.class;
  }
}
