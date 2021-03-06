package org.onehippo.forge.konakart.cms.replication.synchronization.job;


import org.hippoecm.repository.quartz.JCRSchedulingContext;
import org.onehippo.forge.konakart.cms.replication.jcr.GalleryProcesssorConfig;
import org.onehippo.forge.konakart.cms.replication.service.KonakartSynchronizationService;
import org.onehippo.forge.konakart.cms.replication.synchronization.KonakartResourceScheduler;
import org.onehippo.forge.konakart.common.engine.KKEngine;
import org.onehippo.forge.konakart.common.engine.KKStoreConfig;
import org.onehippo.forge.konakart.common.jcr.HippoModuleConfig;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.List;

import static org.hippoecm.frontend.translation.ILocaleProvider.HippoLocale;

public class KonakartSyncJob implements Job {

    private static Logger log = LoggerFactory.getLogger(KonakartSyncJob.class);


    private Session jcrSession;

    /**
     * Start the replication
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("Executing Konakart Products Replicator ...");

        // Set the JcrSession
        KonakartResourceScheduler scheduler = (KonakartResourceScheduler) context.getScheduler();
        jcrSession = ((JCRSchedulingContext) scheduler.getCtx()).getSession();
        String storeId = context.getJobDetail().getJobDataMap().getString(KonakartSynchronizationService.KK_STORE_ID);

        @SuppressWarnings("unchecked")
        List<? extends HippoLocale> locales =
                (List<? extends HippoLocale>) context.getJobDetail().getJobDataMap().get(KonakartSynchronizationService.LOCALES);

        KKStoreConfig kkStoreConfig = HippoModuleConfig.getConfig().getStoresConfig().get(storeId);


        if ((kkStoreConfig == null) || !kkStoreConfig.isInitialized()) {
            log.error("The Konakart synchronization service has not well be initialized. Please check the log.");
            return;
        }

        // Load the gallery processor service
        GalleryProcesssorConfig.load(jcrSession);

        try {
            // Initialize the Konakart engine
            KKEngine.init(jcrSession);


            try {
                // Synchronize konakart information
                syncRepositoryToKonakart(kkStoreConfig, locales);
                kkStoreConfig.updateLastUpdatedTimeRepositoryToKonakart(jcrSession);
            } catch (Exception e) {
                log.warn("Failed to update Repository to Konakart. ", e);
            }

            // Synchronize hippo product
            try {
                syncKonakartToRepository(kkStoreConfig);
                kkStoreConfig.updateLastUpdatedTimeKonakartToRepository(jcrSession);
            } catch (Exception e) {
                log.warn("Failed to update Konakart to Repository. ", e);
            }

        } catch (Exception e) {
            log.warn("Failed to initialize Konakart engine. ", e);
        }
    }

    /**
     * Synchronize Konakart information to Hippo
     *
     * @param kkStoreConfig the store config
     * @param locales       list of available locales
     * @throws Exception an exception
     */
    private void syncRepositoryToKonakart(KKStoreConfig kkStoreConfig,
                                          List<? extends HippoLocale> locales) throws Exception {

        // Synchronize products
        KonakartSyncProducts.updateRepositoryToKonakart(kkStoreConfig, locales, jcrSession);

        // Synchronize the customer
        KonakartSyncCustomers.updateRepositoryToKonakart(jcrSession);
    }

    /**
     * Synchronize Konakart information to Hippo
     *
     * @param kkStoreConfig the store config
     * @throws Exception an exception
     */
    private void syncKonakartToRepository(KKStoreConfig kkStoreConfig) throws Exception {

        // Synchronize products
        KonakartSyncProducts.updateKonakartToRepository(kkStoreConfig, jcrSession);
    }
}
