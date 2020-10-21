package br.com.sankhya.acompfrete.helpers;

import java.util.Iterator;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.VOProperty;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.MGEModelException;

public class ChgHelper {

	public static void verificaCampoObrigatorio(Object campo, String nomeCampo) throws MGEModelException {

		if (campo == null)
			throw new MGEModelException("O campo " + nomeCampo + " deve ser enviado na requisição");

	}

	public static DynamicVO duplicar(DynamicVO modeloVO, String dao) throws Exception {
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
