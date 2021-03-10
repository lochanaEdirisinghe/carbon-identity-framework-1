package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

/**
 * Abstract Javascript wrapper for Java level AuthenticatedUser.
 * This provides controlled access to AuthenticatedUser object via provided javascript native syntax.
 * e.g
 * var userName = context.lastAuthenticatedUser.username
 * <p>
 * instead of
 * var userName = context.getLastAuthenticatedUser().getUserName()
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime
 * AuthenticatedUser.
 *
 * @see AuthenticatedUser
 */
public abstract class JsBaseAuthenticatedUser extends AbstractJSObjectWrapper<AuthenticatedUser>
        implements JsAuthenticatedUser {

    private static final Log LOG = LogFactory.getLog(JsBaseAuthenticatedUser.class);
    protected int step;
    protected String idp;

    /**
     * Constructor to be used when required to access step specific user details.
     *
     * @param context Authentication context
     * @param wrappedUser Authenticated user
     * @param step        Authentication step
     * @param idp         Authenticated Idp
     */
    public JsBaseAuthenticatedUser(AuthenticationContext context,
                                      AuthenticatedUser wrappedUser, int step, String idp) {

        this(wrappedUser, step, idp);
        initializeContext(context);
    }

    /**
     * Constructor to be used when required to access step specific user details.
     *
     * @param wrappedUser Authenticated user
     * @param step        Authentication step
     * @param idp         Authenticated Idp
     */
    public JsBaseAuthenticatedUser(AuthenticatedUser wrappedUser, int step, String idp) {

        super(wrappedUser);
        this.step = step;
        this.idp = idp;
    }

    /**
     * Constructor to be used when required to access step independent user.
     *
     * @param wrappedUser Authenticated user
     */
    public JsBaseAuthenticatedUser(AuthenticatedUser wrappedUser) {

        super(wrappedUser);
    }

    public JsBaseAuthenticatedUser(AuthenticationContext context, AuthenticatedUser wrappedUser) {

        this(wrappedUser);
        initializeContext(context);
    }

    public void setMember(String name, String value) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_USERNAME:
                getWrapped().setUserName(value);
                break;
            case FrameworkConstants.JSAttributes.JS_USER_STORE_DOMAIN:
                getWrapped().setUserStoreDomain(value);
                break;
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                getWrapped().setTenantDomain(value);
                break;
            default:
                super.setMember(name, value);
        }
    }

    @Override
    public boolean hasMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT_IDENTIFIER:
                return getWrapped().getAuthenticatedSubjectIdentifier() != null;
            case FrameworkConstants.JSAttributes.JS_USERNAME:
                return getWrapped().getUserName() != null;
            case FrameworkConstants.JSAttributes.JS_USER_STORE_DOMAIN:
                return getWrapped().getUserStoreDomain() != null;
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                return getWrapped().getTenantDomain() != null;
            case FrameworkConstants.JSAttributes.JS_LOCAL_CLAIMS:
                return idp != null;
            case FrameworkConstants.JSAttributes.JS_REMOTE_CLAIMS:
                return idp != null && !FrameworkConstants.LOCAL.equals(idp);
            default:
                return super.hasMember(name);
        }
    }

    protected String[] getLocalRoles() {

        if (idp == null || FrameworkConstants.LOCAL.equals(idp)) {
            RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
            int usersTenantId = IdentityTenantUtil.getTenantId(getWrapped().getTenantDomain());

            try {
                String usernameWithDomain = UserCoreUtil.addDomainToName(getWrapped().getUserName(), getWrapped()
                        .getUserStoreDomain());
                UserRealm userRealm = realmService.getTenantUserRealm(usersTenantId);
                return userRealm.getUserStoreManager().getRoleListOfUser(usernameWithDomain);
            } catch (UserStoreException e) {
                LOG.error("Error when getting role list of user: " + getWrapped(), e);
            }
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
