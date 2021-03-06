package io.fnx.backend.tools.authorization;

import io.fnx.backend.tools.auth.Principal;
import io.fnx.backend.tools.auth.PrincipalRole;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AllowedForAuthenticatedAuthorizationGuard implements AuthorizationGuard {

    private final static List<Class<? extends Annotation>> annotations;

    static {
        final List<Class<? extends Annotation>> res = new LinkedList<>();
        res.add(AllowedForAuthenticated.class);
        annotations = Collections.unmodifiableList(res);
    }

    @Override
    public Collection<Class<? extends Annotation>> getAnnotationClasses() {
        return annotations;
    }

    @Override
    public AuthorizationResult guardInvocation(MethodInvocation invocation, Annotation annotation, Principal principal) {
        return isAuthenticated(principal)
                ? AuthorizationResult.SUCCESS
                : AuthorizationResult.failure("User must be authenticated to call method " + invocation.getMethod()
                + ", current roles: " + principalRolesToString(principal));
    }

    private boolean isAuthenticated(Principal principal) {
        if (principal == null) return false;

        List<? extends PrincipalRole> roles = principal.getUserRoles();
        if (roles == null) return false;

        for (PrincipalRole role : roles) {
            if (role.isAuthenticated()) return true;
        }

        return false;
    }
}
