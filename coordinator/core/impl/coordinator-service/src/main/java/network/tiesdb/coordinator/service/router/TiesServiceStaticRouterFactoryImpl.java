package network.tiesdb.coordinator.service.router;

import network.tiesdb.router.api.TiesRouter;
import network.tiesdb.router.api.TiesRouterFactory;
import network.tiesdb.service.api.TiesService;

public class TiesServiceStaticRouterFactoryImpl implements TiesRouterFactory {

    private final TiesServiceStaticRouterConfigImpl config;

    public TiesServiceStaticRouterFactoryImpl(TiesServiceStaticRouterConfigImpl config) {
        this.config = config;
    }

    @Override
    public TiesRouter createRouter(TiesService service) {
        return new TiesServiceStaticRouter(service, config);
    }

}
