package org.apereo.portal.fbms.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Component
public class FilterChainBuilder {

    @Autowired(required = false)
    private List<ExtensionFilter> filters;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        // Record filter settings in the log
        logger.info("Found the following FbmsFilter beans:  {}", filters);

        // Prevent NPE if no filters are configured
        if (filters == null) {
            filters = Collections.emptyList();
        }
    }

    /**
     * Builds a filter chain based on a <code>Supplier</code>.
     */
    public <E extends FbmsEntity> Supplier<E> fromSupplier(HttpServletRequest request,
            HttpServletResponse response, final Supplier<E> callback) {

        /*
         * Wrap the callback in a FbmsFilterChain at the center of the "onion."
         */
        ExtensionFilterChain<E> chain = entity1 -> callback.get();

        /*
         * Add layers of the onion
         */
        for (ExtensionFilter filter : filters) {
            final boolean applies = filter.appliesTo(null, request);
            logger.debug("FbmsFilter bean {} {} apply to request with URI='{}' and method='{}'",
                    filter,
                    applies ? "DOES" : "DOES NOT",
                    request.getRequestURI(),
                    request.getMethod());
            if (applies) {
                chain = new ExtensionFilterChainImpl(filter, request, response, chain);
            }
        }

        /*
         * Decorate the whole business in a Supplier
         */
        final ExtensionFilterChain<E> rslt = chain;
        return () -> rslt.doFilter(null);

    }

    public <E extends FbmsEntity> Supplier<E> fromUnaryOperator(E entity, HttpServletRequest request, HttpServletResponse response, final UnaryOperator<E> callback) {

        /*
         * Wrap the callback in a FbmsFilterChain at the center of the "onion."
         */
        ExtensionFilterChain<E> chain = entity1 -> callback.apply(entity);

        /*
         * Add layers of the onion
         */
        for (ExtensionFilter filter : filters) {
            final boolean applies = filter.appliesTo(entity, request);
            logger.debug("FbmsFilter bean {} {} apply to request with URI='{}', method='{}', and entity='{}'",
                    filter,
                    applies ? "DOES" : "DOES NOT",
                    request.getRequestURI(),
                    request.getMethod(),
                    entity);
            if (applies) {
                chain = new ExtensionFilterChainImpl(filter, request, response, chain);
            }
        }

        /*
         * Decorate the whole business in a Supplier
         */
        final ExtensionFilterChain<E> rslt = chain;
        return () -> rslt.doFilter(entity);

    }

    /*
     * Nested Types
     */

    private static final class ExtensionFilterChainImpl<E extends FbmsEntity> implements ExtensionFilterChain<E> {

        private final ExtensionFilter<E> enclosed;
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final ExtensionFilterChain<E> nextLink;

        /* package-private */ ExtensionFilterChainImpl(ExtensionFilter<E> enclosed,
                HttpServletRequest request, HttpServletResponse response, ExtensionFilterChain<E> nextLink) {

            this.enclosed = enclosed;
            this.request = request;
            this.response = response;
            this.nextLink = nextLink;

        }

        @Override
        public final E doFilter(E entity) {
            return enclosed.doFilter(entity, request, response, nextLink);
        }

    }

}
