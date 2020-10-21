package br.com.sankhya.treina.jobs;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.sankhya.util.BigDecimalUtil;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

/**
* @author Wellyton
* @ejb.bean name="AcaoImportanteJob" 
* jndi-name="br/com/sankhya/treina/jobs/AcaoImportanteJob"
* type="Stateless" transaction-type="Container" 
* view-type="local"
* @ejb.transaction type="Supports"
* @ejb.util generate="false"
*/
public class AcaoImportanteJobBean implements SessionBean {

	/**
	* @ejb.interface-method
	*/
	public void onSchedule() throws Exception, MGEModelException {

		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;
		try {
			System.out.println("Inicio");

			hnd = JapeSession.open();
			hnd.setCanTimeout(false);

			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			jdbc.openSession();
			
			final NativeSql nsql = new NativeSql(jdbc);
			
			hnd.execWithTX(new JapeSession.TXBlock() {
				public void doWithTx() throws Exception {

					
					
				}
			});
			
			NativeSql.releaseResources(nsql);
			
			
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}


	
	private void nativesql() throws Exception{ 
		
		SessionHandle hnd = null;
		JdbcWrapper jdbc = null;
		try {
			System.out.println("Inicio");

			hnd = JapeSession.open();
			hnd.setCanTimeout(false);

			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			jdbc.openSession();
			
			final NativeSql nsql = new NativeSql(jdbc);
			
			hnd.execWithTX(new JapeSession.TXBlock() {
				public void doWithTx() throws Exception {

					nsql.appendSql("MERGE INTO GOOTID G \r\n" + 
							"   USING (SELECT CODIGO FROM GOOTID WHERE SEXO = 'M') S \r\n" + 
							"   ON (G.CODIGO = S.CODIGO) \r\n" + 
							"   WHEN MATCHED THEN UPDATE SET SEXO = 'F'\r\n" + 
							"   WHEN NOT MATCHED THEN INSERT (G.NOME, G.ANALISADO, G.SEXO) \r\n" + 
							"     VALUES ('TESTE', 'S', 'M')");
					
					nsql.executeUpdate();
					
				}
			});
			
			NativeSql.releaseResources(nsql);
			
			
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}
	
	
	private void insertorupdateJape() throws Exception{ 
		JapeWrapper impGoo = JapeFactory.dao("ImportacaoGoogle");
		
		Collection<DynamicVO> registros = impGoo.find("this.SEXO = 'M'");
		
		if(registros.size() > 0) {
			for (DynamicVO reg : registros) {
				impGoo.prepareToUpdate(reg).set("SEXO", "F").update();
			}
		}else {
			impGoo.create().set("NOME", "Masculino")
				.set("OBS", "Observação".toCharArray())
				.set("ANALISADO", "S").set("SEXO", "M").save();
		}
	}
	
	private void removeJape() throws Exception{
		
		JapeWrapper impGoo = JapeFactory.dao("ImportacaoGoogle");
		
		boolean removido = impGoo.delete(BigDecimalUtil.valueOf(8));
		
		String res = removido ? "removido com sucesso" : " não foi removido";
		
		System.out.println("o codigo 8 " +  res);
		
		int rem = impGoo.deleteByCriteria("this.CODIGO > ?", BigDecimalUtil.valueOf(9));
		
		System.out.println("Foram removidos " + rem + " itens com sucesso");
		
	}
	
	private void updateJape() throws Exception{
		
		JapeWrapper ImpGoo = JapeFactory.dao("ImportacaoGoogle");

		ImpGoo.prepareToUpdateByPK(BigDecimalUtil.valueOf(8))
			.set("SEXO", "M")
			.set("TELEFONE", "08005554000")
			.update();
		
		

		Collection<DynamicVO> importador = ImpGoo.find("this.CODIGO > 0");

		for (DynamicVO importVO : importador) {
			ImpGoo.prepareToUpdate(importVO)
			.set("ANALISADO", "N")
			.set("OBS", "Não Analisados".toCharArray())
			.update();
			
		}
		
	}
	
	private void insertJape() throws Exception {
		
		JapeWrapper ImpGoo = JapeFactory.dao("ImportacaoGoogle");

		DynamicVO ImpVO = ImpGoo.create()
				.set("NOME", "Jape")
				.set("OBS", "Observação".toCharArray())
				.set("ANALISADO", "S")
				.save();

		System.out.println("o registro: " + ImpVO.asBigDecimal("CODIGO") + " foi inserido com sucesso!");
		
	}
	
	private void selectJape() throws Exception {
		JapeWrapper usuarioDAO = JapeFactory.dao("Usuario");

		DynamicVO usuarioVO = usuarioDAO.findByPK(new BigDecimal(2));

		System.out.println("------------------------------------");
		System.out.println(usuarioVO.asString("NOMEUSU"));
		System.out.println(usuarioVO.asString("EMAIL"));
		System.out.println(usuarioVO.asBigDecimal("CODGRUPO"));
		System.out.println(usuarioVO.asString("GrupoUsuario.NOMEGRUPO"));
		System.out.println(usuarioVO.asString("Empresa.RAZAOSOCIAL"));
		System.out.println("------------------------------------");

		JapeWrapper veiculoDAO = JapeFactory.dao(DynamicEntityNames.VEICULO);

		Collection<DynamicVO> veiculos = veiculoDAO.find("this.CODVEICULO > ? AND CODPARC > ?", BigDecimal.ZERO, BigDecimalUtil.valueOf(0));

		for (DynamicVO veiculoVO : veiculos) {
			System.out.println("------------------------------------");
			System.out.println(veiculoVO.asString("PLACA"));
			System.out.println(veiculoVO.asBigDecimal("CODMOTORISTA"));
			System.out.println(veiculoVO.asString("Motorista.NOMEPARC"));
			System.out.println("------------------------------------");
		}

		JapeWrapper empresaDAO = JapeFactory.dao(DynamicEntityNames.EMPRESA);

		DynamicVO empresaVO = empresaDAO.findOne("this.CGC = '99999999000191' ");
		System.out.println("------------------------------------");
		System.out.println(empresaVO.asString("NOMEFANTASIA"));
		System.out.println(empresaVO.asBigDecimal("CODEND"));
		System.out.println(empresaVO.asString("Endereco.NOMEEND"));
		System.out.println("------------------------------------");
	}

	/**
	* @ejb.interface-method
	*/
	public String getScheduleConfig() throws Exception {

		StringBuilder sb = new StringBuilder();

		String vir = "";
		for (int i = 0; i < 58; i = i + 2) {
			sb.append(vir);
			sb.append(i);
			vir = ",";
		}

		return "* 11 * * *";
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

}
