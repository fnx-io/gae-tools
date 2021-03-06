package io.fnx.backend.tools.authorization;

import io.fnx.backend.tools.auth.Principal;
import io.fnx.backend.tools.auth.PrincipalRole;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This guard allows to implement role based authorization checks.
 * <p>
 * It does not prescribe any Annotations to be inspected, rather it will just delegate to
 * the implementation to extract allowed roles for a particular call.
 *
 * @param <T> type of the annotation, this guard should be reacting to
 */
public abstract class AllowedForRolesAuthorizationGuard<T extends Annotation> implements AuthorizationGuard {

    private final List<Class<? extends Annotation>> annotations;

    public AllowedForRolesAuthorizationGuard(Class<T> annotation) {
        if (annotation == null) throw new IllegalArgumentException("Role annotation cannot be null");
        final LinkedList<Class<? extends Annotation>> l = new LinkedList<>();
        l.add(annotation);
        this.annotations = Collections.unmodifiableList(l);
    }

    @Override
    public Collection<Class<? extends Annotation>> getAnnotationClasses() {
        return annotations;
    }

    @Override
    public AuthorizationResult guardInvocation(MethodInvocation invocation, Annotation annotation, Principal principal) {
        if (annotation == null) return AuthorizationResult.SUCCESS;

        @SuppressWarnings("unchecked")
        Collection<PrincipalRole> roles = getRoles((T) annotation);
        List<? extends PrincipalRole> userRoles = principal.getUserRoles();

        return userRoles != null && !Collections.disjoint(roles, userRoles)
                ? AuthorizationResult.SUCCESS
                : AuthorizationResult.failure("User has insufficient roles " + principalRolesToString(principal)
                + ", needs: " + rolesToString(roles) + " to call " + invocation.getMethod());
    }

    public abstract Collection<PrincipalRole> getRoles(T annotation);
}
