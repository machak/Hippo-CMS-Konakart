package org.onehippo.forge.konakart.replication;


import com.konakart.al.KKAppEng;
import com.konakart.app.*;
import com.konakart.appif.DataDescriptorIf;
import com.konakart.appif.LanguageIf;
import com.konakart.appif.ProductIf;
import com.konakart.appif.ProductSearchIf;
import com.konakart.util.KKConstants;
import org.apache.commons.lang.StringUtils;
import org.onehippo.forge.konakart.common.KKCndConstants;
import org.onehippo.forge.konakart.common.bl.CustomProductMgr;
import org.onehippo.forge.konakart.common.engine.KKEngine;
import org.onehippo.forge.konakart.common.jcr.HippoModuleConfig;
import org.onehippo.forge.konakart.replication.config.HippoKonakartMapping;
import org.onehippo.forge.konakart.replication.config.HippoRepoConfig;
import org.onehippo.forge.konakart.replication.factory.DefaultProductFactory;
import org.onehippo.forge.konakart.replication.factory.ProductFactory;
import org.onehippo.forge.konakart.replication.jcr.GalleryProcesssorConfig;
import org.onehippo.forge.konakart.replication.utils.Codecs;
import org.onehippo.forge.konakart.replication.utils.NodeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class KonakartProductReplicator {

    private static Logger log = LoggerFactory.getLogger(KonakartProductReplicator.class);

    private javax.jcr.Session jcrSession;
    private HippoRepoConfig hippoRepoConfig;
    private String productFactoryClassName;

    private HippoModuleConfig config;

    /**
     * the hippo document type which defined a product. This document must contain the konakart:konakart compound
     */
    private String productDocType;

    /**
     * The name of the property associated to the konakart:konaker document.
     */
    private String konakartProductPropertyName;

    /**
     * @param jcrSession the JCR session
     */
    public void setJcrSession(javax.jcr.Session jcrSession) {
        this.jcrSession = jcrSession;
    }

    /**
     * @param hippoRepoConfig some hippo configuration
     */
    public void setHippoRepoConfig(HippoRepoConfig hippoRepoConfig) {
        this.hippoRepoConfig = hippoRepoConfig;
    }


    /**
     * By default the DefaultProductFactory will be used if no factory is set
     *
     * @param productFactoryClassName the product factory class name.
     */
    public void setProductFactory(String productFactoryClassName) {
        this.productFactoryClassName = productFactoryClassName;
    }

    /**
     * @param productDocType the product document type
     */
    public void setProductDocType(String productDocType) {
        this.productDocType = productDocType;
    }

    /**
     * @param konakartProductPropertyName .
     */
    public void setKonakartProductPropertyName(String konakartProductPropertyName) {
        this.konakartProductPropertyName = konakartProductPropertyName;
    }

    /**
     * Start the replication
     */
    public void execute() {
        log.debug("Executing Konakart Products Replicator ...");

        // load the konakart module config.
        config = HippoModuleConfig.load(jcrSession);

        if (!config.isIntialized()) {
            log.info("Failed to read the configuration from Konakart config module.");
        }

        if (!config.isEnabled()) {
            log.info("The Konakart replicator is disabled. No replication will be operated.");
            return;
        }

        // Load the gallery processor service
        GalleryProcesssorConfig.load(jcrSession);

        try {
            // Initialize the Konakart engine
            KKEngine.init(config.getEngineConfig().getEngineMode(), config.getEngineConfig().isCustomersShared(),
                    config.getEngineConfig().isProductsShared());

            // Update konakart information
            try {
                if (config.getEngineConfig().isUpdateRepositoryToKonakart()) {
                    updateRepositoryToKonakart();
                    config.setLastUpdatedTimeRepositoryToKonakart(jcrSession);
                }
            } catch (Exception e) {
                log.warn("Failed to update Repository to Konakart. ", e);
            }

            // Update hippo product
            try {
                if (config.getEngineConfig().isUpdateKonakartToRepository()) {
                    updateKonakartToRepository();
                    config.setLastUpdatedTimeKonakartToRepository(jcrSession);
                }
            } catch (Exception e) {
                log.warn("Failed to update Konakart to Repository. ", e);
            }

        } catch (Exception e) {
            log.warn("Failed to initialize Konakart engine. ", e);
        }
    }

    /**
     * Copy products from Konakart to Hippo
     *
     * @throws Exception an exception
     */
    private void updateKonakartToRepository() throws Exception {

        KKAppEng kkengine = KKEngine.get(KKConstants.KONAKART_DEFAULT_STORE_ID);

        // Retrieve the list of languages defined into konakart.
        LanguageIf[] languages = kkengine.getEng().getAllLanguages();

        // For each language defined into Konakart we need to add the product under Hippo
        for (LanguageIf language : languages) {

            List<HippoKonakartMapping.MappingByProductType> mapping = hippoRepoConfig.getMapping(language.getLocale()).getByProductTypeList();

            // Synchronized by product types.
            for (HippoKonakartMapping.MappingByProductType mappingByProductType : mapping) {

                String storeId = mappingByProductType.getStoreId();

                // Initialize the KKEngine
                kkengine = KKEngine.get(storeId);

                // Retrieve the product factory
                CustomProductMgr productMgr = new CustomProductMgr(kkengine.getEng(), config.getLastUpdatedTimeKonakartToRepository());

                // Get only visible products
                DataDescriptorIf dataDescriptorIf = new DataDescriptor();
                dataDescriptorIf.setShowInvisible(true);
                dataDescriptorIf.setFillDescription(true);

                // Search products from all stores
                ProductSearchIf productSearchIf = new ProductSearch();

                // Used default products options
                FetchProductOptions fetchProductOptions = new FetchProductOptions();

                if (StringUtils.isNotEmpty(mappingByProductType.getCatalogId())) {
                    fetchProductOptions.setCatalogId(mappingByProductType.getCatalogId());
                }

                // Retrieve the path where the images are saved
                String baseImagePath = kkengine.getEng().getConfiguration("IMG_BASE_PATH").getValue();

                String globalProductTypeName = null;
                
                // Retrieve the list of products by product's type
                if (mappingByProductType.isProductTypeSet()) {
                    productSearchIf.setProductType(mappingByProductType.getProductType());
                    globalProductTypeName = KKCndConstants.PRODUCT_TYPE.findByType(mappingByProductType.getProductType()).getName();
                }

                // Search
                Products products = productMgr.searchForProductsWithOptions(kkengine.getSessionId(), dataDescriptorIf,
                        productSearchIf, language.getId(), fetchProductOptions);

                // Get the Hippo product folder name
                String productFolder = mappingByProductType.getProductFolder();

                if (productFolder == null) {
                    continue;
                }

                // Insert products into konakart
                for (Product product : products.getProductArray()) {
                    ProductFactory productFactory = createProductFactory();

                    if (productFactory == null) {
                        continue;
                    }

                    String productTypeName = globalProductTypeName;
                    
                    /**
                     * No product type has been set. Used the product type associated with the product
                     */
                    if (globalProductTypeName == null) {
                        productTypeName = KKCndConstants.PRODUCT_TYPE.findByType(product.getType()).getName();
                    }

                    productFactory.setSession(jcrSession);
                    productFactory.setContentRoot(mappingByProductType.getContentRoot());
                    productFactory.setGalleryRoot(mappingByProductType.getGalleryRoot());
                    productFactory.setProductFolder(productFolder);
                    productFactory.setProductDocType(productDocType);
                    productFactory.setKKProductTypeName(productTypeName);
                    productFactory.setKonakartProductPropertyName(konakartProductPropertyName);

                    // Create the reviews' folder if not exists
                    String reviewName = productFactory.createReviewFolder(mappingByProductType.getReviewFolder());

                    // Create the product
                    product.setStoreId(storeId);
                    productFactory.add(product, language, baseImagePath);

                    // Set the Review folder
                    productMgr.synchronizeHippoKK(product.getId(), reviewName);
                }
            }
        }
    }


    /**
     * Synchronize produts status updates and multi-store update

     * @throws Exception .
     */
    private void updateRepositoryToKonakart() throws Exception {

        NodeHelper nodeHelper = new NodeHelper(jcrSession);

        KKAppEng kkengine = KKEngine.get(KKConstants.KONAKART_DEFAULT_STORE_ID);

        // Retrieve the list of languages defined into konakart.
        LanguageIf[] languages = kkengine.getEng().getAllLanguages();

        // For each language defined into Konakart we need to add the product under Hippo
        for (LanguageIf language : languages) {

            List<HippoKonakartMapping.MappingByProductType> mapping = hippoRepoConfig.getMapping(language.getLocale()).getByProductTypeList();

            // Synchronized by product types.
            for (HippoKonakartMapping.MappingByProductType mappingByProductType : mapping) {

                // Try to retrieve the product by id
                CustomProductMgr productMgr = new CustomProductMgr(kkengine.getEng());

                Node seed = null;

                String productRoot = mappingByProductType.getContentRoot() + "/" + Codecs.encodeNode(mappingByProductType.getProductFolder());
                
                if (jcrSession.itemExists(productRoot)) {
                    seed = jcrSession.getNode(productRoot);
                }

                if (seed != null) {
                    List<SyncProduct> syncProducts = new LinkedList<SyncProduct>();
                    findAllProductIdsFromRepository(seed, syncProducts);

                    if (syncProducts.size() > 0) {
                        if (log.isInfoEnabled()) {
                            log.info(syncProducts.size() + " Hippo product(s) will be synchronized to Konakart");
                        }
                    }

                    for (SyncProduct syncProduct : syncProducts) {

                        String storeId = mappingByProductType.getStoreId();

                        // Initialize the KKEngine
                        kkengine = KKEngine.get(storeId);

                        // Retrieve the product from Konakart
                        ProductIf productIf = kkengine.getEng().getProduct(kkengine.getSessionId(), syncProduct.getkProductId(),
                                language.getId());

                        // Retrieve the hippo node
                        Node node = jcrSession.getNodeByIdentifier(syncProduct.getHippoUuid());

                        // If the product is null, it means that the product has been removed from the store.
                        // So the Hippo document should be unpublished.
                        if (productIf == null) {
                            nodeHelper.updateState(node.getParent(), NodeHelper.UNPUBLISHED_STATE);
                        }  else {
                            // Synchronize the state of a product on konakart according to the state of the node,
                            // is only available when Konakart does not shared products over Stores.
                            if (!KKAppEng.getEngConf().isProductsShared()) {
                                Date lastUpdatedTimeRepositoryToKonakart = config.getLastUpdatedTimeRepositoryToKonakart();

                                // Update only newer updated products
                                if (lastUpdatedTimeRepositoryToKonakart == null || syncProduct.getLastModificationDate().after(lastUpdatedTimeRepositoryToKonakart)) {
                                    String nodeState = nodeHelper.getNodeState(node);

                                    if (nodeState != null) {
                                        if (nodeState.equalsIgnoreCase(NodeHelper.UNPUBLISHED_STATE)) {
                                            productMgr.updateStatus(productIf.getId(), false);
                                        } else {
                                            productMgr.updateStatus(productIf.getId(), true);
                                        }
                                    }

                                    // Update the description
                                    if (node.hasNode(KKCndConstants.PRODUCT_DESCRIPTION)) {
                                        Node descriptionNode = node.getNode(KKCndConstants.PRODUCT_DESCRIPTION);

                                        String htmlDescription = descriptionNode.getProperty("hippostd:content").getString();

                                        // remove the <html><body> at the beginning and the </body></html> at the end
                                        htmlDescription = StringUtils.remove(htmlDescription, "<html>");
                                        htmlDescription = StringUtils.remove(htmlDescription, "<body>");
                                        htmlDescription = StringUtils.remove(htmlDescription, "</body>");
                                        htmlDescription = StringUtils.remove(htmlDescription, "</html>");

                                        productMgr.updateDescription(syncProduct.getkProductId(), syncProduct.getkLanguageId(),
                                                htmlDescription);
                                    }
                                }
                            }


                        }
                    }
                }

            }

        }
    }

    private void findAllProductIdsFromRepository(Node seed, List<SyncProduct> syncProducts) throws RepositoryException {
        if (seed.isNodeType("hippo:handle")) {
            seed = seed.getNode(seed.getName());
        }

        if (seed.isNodeType(KKCndConstants.PRODUCT_DOC_TYPE)) {

            Date lastModificationDate = seed.getParent().getProperty("hippostdpubwf:lastModificationDate").getDate().getTime();

            SyncProduct syncProduct = new SyncProduct();
            syncProduct.setHippoUuid(seed.getIdentifier());
            syncProduct.setkProductId((int) seed.getProperty(KKCndConstants.PRODUCT_ID).getLong());
            syncProduct.setkLanguageId((int) seed.getProperty(KKCndConstants.PRODUCT_LANGUAGE_ID).getLong());
            syncProduct.setLastModificationDate(lastModificationDate);

            syncProducts.add(syncProduct);

        } else if (seed.isNodeType("hippostd:folder") || seed.isNodeType(productDocType)) {
            for (NodeIterator nodeIt = seed.getNodes(); nodeIt.hasNext();) {
                Node child = nodeIt.nextNode();

                if (child != null) {
                    findAllProductIdsFromRepository(child, syncProducts);
                }
            }
        }
    }


    private ProductFactory createProductFactory() {
        if (StringUtils.isNotBlank(productFactoryClassName)) {
            try {
                return (ProductFactory) Class.forName(productFactoryClassName).newInstance();

            } catch (InstantiationException e) {
                log.error("Unable to find the extension class: " + e.toString());
            } catch (IllegalAccessException e) {
                log.error("Unable to find the extension class: " + e.toString());
            } catch (ClassNotFoundException e) {
                log.error("Unable to find the extension class: " + e.toString());
            }
        }

        return new DefaultProductFactory();
    }

    public static class SyncProduct {
        private int kProductId;
        private int kLanguageId;
        private String hippoUuid;
        private Date lastModificationDate;

        public int getkProductId() {
            return kProductId;
        }

        public void setkProductId(int kProductId) {
            this.kProductId = kProductId;
        }

        public int getkLanguageId() {
            return kLanguageId;
        }

        public void setkLanguageId(int kLanguageId) {
            this.kLanguageId = kLanguageId;
        }

        public String getHippoUuid() {
            return hippoUuid;
        }

        public void setHippoUuid(String hippoUuid) {
            this.hippoUuid = hippoUuid;
        }

        public Date getLastModificationDate() {
            return lastModificationDate;
        }

        public void setLastModificationDate(Date lastModificationDate) {
            this.lastModificationDate = lastModificationDate;
        }
    }
}
