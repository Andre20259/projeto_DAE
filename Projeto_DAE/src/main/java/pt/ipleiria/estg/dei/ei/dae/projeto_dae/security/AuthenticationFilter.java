package pt.ipleiria.estg.dei.ei.dae.projeto_dae.security;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.Priority;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.UserBean;

import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;

@Provider
@Authenticated
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Response ACCESS_DENIED = Response.status(401)
            .entity("Access denied for this resource").build();
    private static final Response ACCESS_FORBIDDEN = Response.status(403)
            .entity("Access forbidden for this resource").build();
    @Context
    private SecurityContext securityContext;
    @EJB
    private UserBean userBean;
    @Context
    private UriInfo uriInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header must be provided");
        }
        // Get token from the HTTP Authorization header
        String token = header.substring("Bearer".length()).trim();
        var user = userBean.find(getUsername(token));
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return user::getUsername;
            }

            @Override
            public boolean isUserInRole(String s) {
                return org.hibernate.Hibernate.getClass(user).getSimpleName().equals(s);
            }

            @Override
            public boolean isSecure() {
                return uriInfo.getAbsolutePath().toString().startsWith("https");
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        });

        // Use the request's security context (may have been set above)
        SecurityContext sc = requestContext.getSecurityContext();

        var methodInvoker = (ResourceMethodInvoker) requestContext
                .getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
        Method method = methodInvoker.getMethod();
        var resource = method.getDeclaringClass();

        // If authenticated, access granted for all roles
        if (resource.isAnnotationPresent(PermitAll.class)) {
            if (method.isAnnotationPresent(DenyAll.class)) {
                requestContext.abortWith(ACCESS_DENIED);
                return;
            }
            // Verify user access
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAnnotation = method
                        .getAnnotation(RolesAllowed.class);
                var roles =
                        new HashSet<>(Arrays.asList(rolesAnnotation.value()));
                // Is user valid?
                if (roles.stream().anyMatch(sc::isUserInRole)) {
                    return;
                }
                requestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }
        }

        // Access denied for all
        if(resource.isAnnotationPresent(DenyAll.class)) {
            if (method.isAnnotationPresent(PermitAll.class)) {
                return;
            }
            // Verify user access
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed beanRolesAnnotation = method.getAnnotation(RolesAllowed.class);
                var roles =
                        new HashSet<>(Arrays.asList(beanRolesAnnotation.value()));
                // Is user valid?
                if(roles.stream().anyMatch(sc::isUserInRole)) {
                    return;
                }
            }
            requestContext.abortWith(ACCESS_DENIED);
            return;
        }

        if (resource.isAnnotationPresent(RolesAllowed.class)) {
            if (method.isAnnotationPresent(DenyAll.class)) {
                requestContext.abortWith(ACCESS_DENIED);
                return;
            }
            if (method.isAnnotationPresent(PermitAll.class)) {
                return;
            }
            RolesAllowed rolesAnnotation = resource
                    .getAnnotation(RolesAllowed.class);
            var roles = new HashSet<>(Arrays.asList(rolesAnnotation.value()));
            // Verify user access
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                roles.addAll(Arrays.asList(rolesAnnotation.value()));
            }
            // Is user valid?
            if(roles.stream().anyMatch(sc::isUserInRole)) {
                return;
            }
            requestContext.abortWith(ACCESS_FORBIDDEN);
            return;
        }
        if (method.isAnnotationPresent(DenyAll.class)) {
            requestContext.abortWith(ACCESS_DENIED);
            return;
        }
        if (method.isAnnotationPresent(PermitAll.class)) {
            return;
        }

        // Verify user access
        if (method.isAnnotationPresent(RolesAllowed.class)) {
            var roles =
                    new HashSet<>(Arrays.asList(method.getAnnotation(RolesAllowed.class).value()));
            // Is user valid?
            if (roles.stream().anyMatch(sc::isUserInRole)) {
                return;
            }
            requestContext.abortWith(ACCESS_FORBIDDEN);
        }
    }

    private String getUsername(String token) {
        var key = new SecretKeySpec(TokenIssuer.SECRET_KEY,
                TokenIssuer.ALGORITHM);
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
        } catch (Exception e) {
            throw new NotAuthorizedException("Invalid JWT");
        }
    }

}
