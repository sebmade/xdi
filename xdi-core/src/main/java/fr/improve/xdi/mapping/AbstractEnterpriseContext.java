package fr.improve.xdi.mapping;


/**
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public abstract class AbstractEnterpriseContext implements EnterpriseContext {
    protected EnterpriseContextHandler enterpriseContextHandler = null;

    /* 
     * @see fr.improve.xdi.mapping.EnterpriseContext#setEnterpriseContextHandler(fr.improve.xdi.mapping.EnterpriseContextHandler)
     */    
    @Override
    public void setEnterpriseContextHandler(EnterpriseContextHandler in_handler) {
        enterpriseContextHandler = in_handler;
    }

    /* 
     * @see fr.improve.xdi.mapping.EnterpriseContext#enterpriseContextHandler()
     */    
    @Override
    public EnterpriseContextHandler enterpriseContextHandler() {
        return enterpriseContextHandler;
    }
}
