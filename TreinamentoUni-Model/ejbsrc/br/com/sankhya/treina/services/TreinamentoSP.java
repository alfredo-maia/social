/*
 * Generated by XDoclet - Do not edit!
 */
package br.com.sankhya.treina.services;

/**
 * Remote interface for TreinamentoSP.
 * @xdoclet-generated at ${TODAY}
 * @copyright The XDoclet Team
 * @author XDoclet
 * @version ${version}
 */
public interface TreinamentoSP
   extends javax.ejb.EJBObject
{

   public void importarArquivo( br.com.sankhya.ws.ServiceContext ctx )
      throws java.rmi.RemoteException;

   public void inserirFinanceiro( br.com.sankhya.ws.ServiceContext ctx )
      throws java.rmi.RemoteException;

   public void inserirNota( br.com.sankhya.ws.ServiceContext ctx )
      throws java.rmi.RemoteException;

   public void testeTxAut( br.com.sankhya.ws.ServiceContext ctx )
      throws java.lang.Exception, java.rmi.RemoteException;

   public void testeTxManual( br.com.sankhya.ws.ServiceContext ctx )
      throws java.lang.Exception, java.rmi.RemoteException;

}
