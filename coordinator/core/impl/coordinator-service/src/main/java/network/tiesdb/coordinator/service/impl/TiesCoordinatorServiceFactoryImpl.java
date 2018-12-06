package network.tiesdb.coordinator.service.impl;

import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.service.api.TiesServiceDaemon;
import network.tiesdb.service.api.TiesServiceFactory;

public class TiesCoordinatorServiceFactoryImpl implements TiesServiceFactory {

    private final TiesCoordinatorServiceConfigImpl config;

    public TiesCoordinatorServiceFactoryImpl(TiesCoordinatorServiceConfigImpl config) {
        this.config = config;
    }

    @Override
    public TiesServiceDaemon createServiceDaemon(String name) throws TiesConfigurationException {
        return new TiesCoordinatorServiceDaemonImpl(name, new TiesCoordinatorServiceImpl(config));
    }

}
