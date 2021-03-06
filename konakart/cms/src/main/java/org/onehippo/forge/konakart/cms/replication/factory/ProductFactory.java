package org.onehippo.forge.konakart.cms.replication.factory;

import com.konakart.app.Product;
import com.konakart.appif.LanguageIf;
import org.onehippo.forge.konakart.cms.replication.utils.NodeHelper;
import org.onehippo.forge.konakart.cms.replication.utils.NodeImagesHelper;
import org.onehippo.forge.konakart.common.engine.KKStoreConfig;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface ProductFactory {

    /**
     * @param jcrSession set the jcr session
     * @throws javax.jcr.RepositoryException .
     */
    void setSession(Session jcrSession) throws RepositoryException;

    /**
     * Set the Konakart config class. Contains the related information used to add a new product
     */
    void setKKStoreConfig(final KKStoreConfig kkStoreConfig);

    /**
     * Add a product to hippo
     *
     * @param storeId       the store id associated with this product
     * @param product       the product to add
     * @param language      the language associated to this product
     * @param baseImagePath the path where the konakart images are located
     * @throws Exception if any exceptions occurs
     */
    void add(final String storeId, final Product product, final LanguageIf language, final String baseImagePath) throws Exception;


}
