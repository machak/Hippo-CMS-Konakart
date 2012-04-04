package org.onehippo.forge.konakart.hst.wizard.checkout.activity;

import com.konakart.app.KKException;
import org.onehippo.forge.konakart.hst.utils.KKUtil;
import org.onehippo.forge.konakart.hst.wizard.ActivityException;
import org.onehippo.forge.konakart.hst.wizard.checkout.CheckoutProcessContext;
import org.onehippo.forge.konakart.hst.wizard.checkout.CheckoutSeedData;

public class BillingAddressActivity extends BaseAddressActivity {

    public static final String SHIPPING_ADDRESS = "shippingAddress";
    public static final String SELECT_SAME_SHIPPING_ADDRESS = "same";


    @Override
    public void doAction() throws ActivityException {
        CheckoutProcessContext checkoutProcessContext = (CheckoutProcessContext) processorContext;
        CheckoutSeedData seedData = checkoutProcessContext.getSeedData();


        if (seedData.getAction().equals(ACTIONS.SELECT.name())) {
            Integer addressId = Integer.valueOf(KKUtil.getEscapedParameter(seedData.getRequest(), ADDRESS));
            String shippingAddress = KKUtil.getEscapedParameter(seedData.getRequest(), SHIPPING_ADDRESS);

            // Create a new address
            if (addressId == -1) {
                try {
                    addressId = seedData.getKkHstComponent().getKkAppEng().getCustomerMgr().addAddressToCustomer(createAddressForCustomer());
                } catch (Exception e) {
                    setNextLoggedState(STATES.INITIAL.name());
                    addMessage(GLOBALMESSAGE, seedData.getBundle().getString("checkout.failed.create.address"));
                    return;
                }
            }

            seedData.getKkHstComponent().getKkAppEng().getOrderMgr().setCheckoutOrderBillingAddress(addressId);

            if (shippingAddress.equals(SELECT_SAME_SHIPPING_ADDRESS)) {
                try {
                    seedData.getKkHstComponent().getKkAppEng().getOrderMgr().setCheckoutOrderShippingAddress(addressId);

                    // Skip the SHIPPING ADDRESS step because the customer has decided to use the
                    // same billing address
                    setNextLoggedState(STATES.SHIPPING_ADDRESS.name());
                } catch (KKException e) {
                    log.error("Failed to set the shipping address", e);
                }
            }
        }
    }

}