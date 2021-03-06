package org.onehippo.forge.konakart.cms.deriveddatafunction;

import com.konakartadmin.app.AdminProduct;
import com.konakartadmin.appif.KKAdminIf;
import org.hippoecm.repository.ext.DerivedDataFunction;
import org.onehippo.forge.konakart.common.engine.KKAdminEngine;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Map;

public class SpecialPriceExTaxDerivedDataFunction extends DerivedDataFunction {

    @Override
    public Map<String, Value[]> compute(Map<String, Value[]> parameters) {
        if (!parameters.containsKey("ppid")) {
            parameters.clear();
            return parameters;
        }

        try {
            int ppid = (int) parameters.get("ppid")[0].getLong();

            KKAdminIf engine = KKAdminEngine.getInstance().getEngine();

            if(engine != null) {
                AdminProduct adminProduct = engine.getProduct(KKAdminEngine.getInstance().getSession(), ppid);

                if (adminProduct.getSpecialPriceExTax() != null) {
                    parameters.put("specialprice", new Value[]{getValueFactory().createValue(adminProduct.getSpecialPriceExTax().doubleValue())});
                }
            }
        } catch (RepositoryException e) {
            parameters.clear();
        } catch (Exception e) {
            parameters.clear();
        }


        return parameters;
    }
}
