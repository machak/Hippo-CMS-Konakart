package org.onehippo.forge.konakart.cms.valuelistprovider;

import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.Plugin;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.onehippo.forge.selection.frontend.model.ListItem;
import org.onehippo.forge.selection.frontend.model.ValueList;
import org.onehippo.forge.selection.frontend.provider.IValueListProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

public class ProductTypeProvider extends Plugin implements IValueListProvider {

    static final Logger log = LoggerFactory.getLogger(ProductTypeProvider.class);

    public ProductTypeProvider(IPluginContext context, IPluginConfig config) {
        super(context, config);

        context.registerService(this, config.getString(IValueListProvider.SERVICE));

        if (log.isDebugEnabled()) {
            log.debug(this.getClass().getName() + " registered under " + IValueListProvider.SERVICE);
        }

    }

    @Override
    public ValueList getValueList(IPluginConfig config) {
        return getValueList(config.getString("source", "values"));
    }

    @Override
    public ValueList getValueList(String name) {
        return getValueList(name, null/*locale*/);
    }

    @Override
    public ValueList getValueList(String name, Locale locale) {

        ValueList valueList = new ValueList();
        if ((locale != null) && "en".contains(locale.getLanguage())) {
            valueList.add(new ListItem("0", "Physical Product"));
            valueList.add(new ListItem("1", "Digital Download"));
            valueList.add(new ListItem("2", "Physical Prod-Free Shipping"));
            valueList.add(new ListItem("3", "Physical Prod-Free Shipping"));

        }


        return valueList;
    }

    @Override
    public List<String> getValueListNames() {
        return null;
    }
}
