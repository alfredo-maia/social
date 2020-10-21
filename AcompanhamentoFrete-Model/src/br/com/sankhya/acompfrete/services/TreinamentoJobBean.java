package br.com.sankhya.acompfrete.services;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.modelcore.MGEModelException;

/**
* @author wellyton rodrigues
* @ejb.bean name="TreinamentoJob" 
* jndi-name="br/com/sankhya/acompfrete/services/TreinamentoJob"
* type="Stateless" transaction-type="Container" 
* view-type="local"
* @ejb.transaction type="Supports"
* @ejb.util generate="false"
*/
public class TreinamentoJobBean implements SessionBean {

	/**
	* @ejb.interface-method
	*/
	public void onSchedule() throws Exception, MGEModelException {
		SessionHandle hnd = null;
		try {
			System.out.println("EXECUCAO JOB TreinamentoJobBean");
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}
	}

	/*
	* @ejb.interface-method
	*/
	/*public String getScheduleConfig() throws java.lang.Exception {
		return "* * * * *";
	}*/

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
