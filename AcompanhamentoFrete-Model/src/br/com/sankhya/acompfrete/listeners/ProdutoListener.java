package br.com.sankhya.acompfrete.listeners;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;

public class ProdutoListener extends PersistenceEventAdapter {

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
		System.out.println("afterInsert");
	}

	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		System.out.println("afterUpdate");
	}

}
